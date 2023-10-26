package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultReplier;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import pt.unl.fct.di.blockmess.wrapper.client.tcp.BlockmessWrapperClientTCP;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.consensus.BftSmartLVI;
import pt.unl.fct.di.hyflexchain.planes.application.ti.TransactionInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeId;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft.BftCommittee;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.config.BFT_SMaRtConfig;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.replylistener.BftSmartReplyListener;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.replylistener.BftsmartConsensusResult;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.replylistener.VerifiedTransactionsReply;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.submit.BftSmartConsensusThread;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.utils.VerifiedBlockProcessor;
import pt.unl.fct.di.hyflexchain.planes.data.DataPlane;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockBody;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockHeader;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockMetaHeader;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.block.MerkleTree;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.InvalidAddressException;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.SerializedTx;
import pt.unl.fct.di.hyflexchain.planes.network.Host;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.TransactionManagement;
import pt.unl.fct.di.hyflexchain.util.BytesOps;
import pt.unl.fct.di.hyflexchain.util.ResetInterface;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;
import pt.unl.fct.di.hyflexchain.util.crypto.Crypto;
import pt.unl.fct.di.hyflexchain.util.crypto.HashedObject;
import pt.unl.fct.di.hyflexchain.util.crypto.HyFlexChainSignature;
import pt.unl.fct.di.hyflexchain.util.crypto.SignatureAlgorithm;
import pt.unl.fct.di.hyflexchain.util.stats.BlockStats;

public final class BftSmartStaticCommitteeConsensus extends ConsensusInterface {
    protected static final Logger LOG = LoggerFactory.getLogger(BftSmartStaticCommitteeConsensus.class);

    protected static final byte[] FALSE = new byte[] { 0 };

    protected static final int DIFF_TARGET = 0;

    protected static final BlockMetaHeader PROPOSAL_BLOCK_META_HEADER =
		new BlockMetaHeader(ConsensusMechanism.BFT_SMaRt, DIFF_TARGET, new HyFlexChainSignature[0],
            CommitteeId.FIRST_COMMITTEE_ID);

    protected static final byte[] PROPOSAL_BLOCK_META_HEADER_SERIALIZED =
        new byte[PROPOSAL_BLOCK_META_HEADER.serializedSize()];

        static {
            try {
                BlockMetaHeader.SERIALIZER.serialize(PROPOSAL_BLOCK_META_HEADER,
                    Unpooled.wrappedBuffer(PROPOSAL_BLOCK_META_HEADER_SERIALIZED).setIndex(0, 0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    protected BFT_SMaRtConfig config;

    protected Triple<CommitteeId, BftCommittee, Map<Address, Host>> staticCommittee;

    protected BFT_SMaRtServiceReplica bftSmartReplica;
    protected AsynchServiceProxy bftSmartClientAsync;

    protected BlockmessConnector blockmess;

    protected final BftSmartLVI bftLVI;

    /**
     * A blockchain structure to temporary save the ordered and valid
     * blocks received via blockmess.
     * When there are consecutive valid blocks,
     * then they are appendend to the blockchain and removed from
     * this collection.
     */
    protected VerifiedBlockProcessor blockProcessor;

    

    /**
     * @param consensus
     * @param lvi
     */
    public BftSmartStaticCommitteeConsensus(LedgerViewInterface lvi) {
        super(ConsensusMechanism.BFT_SMaRt, lvi);

        this.bftLVI = (BftSmartLVI) lvi.getLVI(consensus);

        this.config = new BFT_SMaRtConfig(MultiLedgerConfig.getInstance().getLedgerConfig(this.consensus));
        this.blockProcessor = new VerifiedBlockProcessor(lvi, DataPlane.getInstance(), consensus);
    }

    @Override
    public void init() {

        var selfAddress = config.getLedgerConfig().getMultiLedgerConfig().getSelfAddress();
        this.staticCommittee = getStaticCommittee();

        // check if this node is in the committee
        if (this.staticCommittee.getMiddle().getCommittee().contains(selfAddress)) {
            // init bft smart
            this.bftSmartReplica = new BFT_SMaRtServiceReplica();
        }

        // init blockmess connector
        initBlockmess();

        new Thread(
                new BftSmartConsensusThread(this, config.getLedgerConfig(),
                        () -> Optional.of(this.staticCommittee)),
                "BFT-SMaRt-Consensus-Thread")
                .start();
    }

    @Override
    public void reset() {
        ((ResetInterface) this.blockProcessor).reset();
        this.staticCommittee = getStaticCommittee();

        var selfAddress = config.getLedgerConfig().getMultiLedgerConfig().getSelfAddress();
        // check if this node is in the committee
        if (this.staticCommittee.getMiddle().getCommittee().contains(selfAddress)) {
            // reset bft smart
            ((ResetInterface) this.bftSmartReplica).reset();
        }
    }

    protected Triple<CommitteeId, BftCommittee, Map<Address, Host>> getStaticCommittee() {
        final var committee = this.bftLVI.advanceToNextCommittee().get();

        var directoryService = this.config.getLedgerConfig().getMultiLedgerConfig().getDirectoryService();
        var staticCommitteeHosts = directoryService.lookup(
                committee.getValue().getCommittee().toArray(Address[]::new));

        LOG.info("Static committee: {}", staticCommitteeHosts);

        if (staticCommitteeHosts.size() < committee.getValue().size())
            LOG.warn(
                    "Cannot get some Hosts from static committee addresses. Committee size: {}, Resolved Hosts size: {}",
                    committee.getValue().size(), staticCommitteeHosts.size());

        return Triple.of(committee.getKey(), committee.getValue(),
                staticCommitteeHosts);
    }

    protected void initBlockmess() {
        try {
            this.blockmess = new BlockmessConnector();
            this.blockmess.init();
        } catch (IOException e) {
            throw new Error(e.getMessage(), e);
        }
    }

    protected AsynchServiceProxy getBftSmartClientAsync() {
        if (this.bftSmartClientAsync != null)
            return this.bftSmartClientAsync;

        synchronized (this) {
            if (this.bftSmartClientAsync != null)
                return this.bftSmartClientAsync;

            this.bftSmartClientAsync = new AsynchServiceProxy(
                    (config.getBftSmartReplicaId() + 1) * 3256,
                    "", null, null, null);

            return this.bftSmartClientAsync;
        }
    }

    // @Override
    // public void orderTxs(BlockBody blockBody) {
    //     try {
    //         byte[] operation = Utils.json.writeValueAsBytes(blockBody);

    //         var listener = createReplyListener();
    //         listener.submitAsyncOrderedRequest(operation, (reply) -> {
    //             try {
    //                 BftsmartConsensusResult result = BftsmartConsensusResult.SERIALIZER.deserialize(
    //                         ByteBuffer.wrap(reply.getReplyBytes()));

    //                 boolean res = result.isOk();

    //                 Map<String, Boolean> mapTxRes = blockBody.findTransactions().keySet().stream()
    //                         .collect(Collectors.toUnmodifiableMap(
    //                                 (tx) -> tx, (tx) -> res));

    //                 TransactionManagement.getInstance().getTxPool(this.consensus)
    //                         .removePendingTxsAndNotify(mapTxRes);

    //                 if (!res) {
    //                     LOG.info("BFT-SMART: Invalided block from committee with {} signatures",
    //                             reply.getSignaturesCount());

    //                     return;
    //                 }

    //                 // txs ordered and valid
    //                 // disseminate through blockmess the created block

    //                 var okReply = result.getOkResult();
    //                 String previous = Bytes.wrap(okReply.prevBlockMerkleRootHash())
    //                         .toHexString();

    //                 var block = createBlock(reply.getSignatures().values()
    //                         .toArray(HyFlexChainSignature[]::new), okReply.nonce(), previous, blockBody);

    //                 LOG.info("BFT-SMART: Validated block from committee with {} signatures: {}",
    //                         reply.getSignaturesCount(),
    //                         block.header().getMetaHeader().getHash());

    //                 // invoke blockmess
    //                 this.blockmess.invokeAsync(Utils.json.writeValueAsBytes(block));

    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //             }
    //         });

    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }

    // }

    @Override
    public void orderTxs(Collection<SerializedTx> txs) {

        List<Bytes> txHashes = txs.stream()
			.map(SerializedTx::hash)
			.map(Bytes::wrap)
			.collect(Collectors.toCollection(() -> new ArrayList<>(txs.size())));
		
		MerkleTree merkleTree = MerkleTree.createMerkleTree(txHashes);

        try {
            SerializedBlock proposedBlock = createSerializedBlockProposal(txs, merkleTree);

            LOG.info("Propose block {} bytes w/ {} txs -> merkle tree: {}",
                proposedBlock.block.length, txs.size(), merkleTree.getMerkleRootHash());

            var listener = createReplyListener();

            final long before = System.currentTimeMillis();

            listener.submitAsyncOrderedRequest(proposedBlock.block, (reply) -> {
                try {
                    final long after = System.currentTimeMillis();

                    BftsmartConsensusResult result = BftsmartConsensusResult.SERIALIZER.deserialize(
                            Unpooled.wrappedBuffer(reply.getReplyBytes()));

                    boolean res = result.isOk();

                    // notify the result of these transactions
                    TransactionManagement.getInstance().getTxPool(this.consensus)
                            .removePendingTxsAndNotify(txHashes, res);

                    if (res)
                    {
                        BlockStats.addLatency(consensus, (int) (after - before));
                        LOG.info("[{}] Block latency (ms): {}", consensus, after - before);
                    }
                    else
                    {
                        LOG.info("[{}] Invalided block from committee with {} signatures",
                                consensus, reply.getSignaturesCount());

                        return;
                    }

                    // txs ordered and valid
                    // disseminate through blockmess the created block

                    var okReply = result.getOkResult();
                    Bytes previous = Bytes.wrap(okReply.prevBlockMerkleRootHash());

                    SerializedBlock block = createSerializedBlock(reply.getSignatures().values()
                            .toArray(HyFlexChainSignature[]::new), okReply.nonce(), previous,
                            merkleTree.getMerkleRootHash(), proposedBlock);

                    LOG.info("BFT-SMART: Validated block from committee with {} signatures & merkle hash: {}",
                            reply.getSignaturesCount(),
                            merkleTree.getMerkleRootHash());

                    // invoke blockmess
                    this.blockmess.invokeAsync(block.block);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected SerializedBlock createSerializedBlockProposal(Collection<SerializedTx> txs, MerkleTree merkleTree)
        throws IOException
	{
		var header = BlockHeader.create(PROPOSAL_BLOCK_META_HEADER, Bytes.EMPTY, merkleTree.getMerkleRootHash(), 0);

		int headerSize = header.serializedSize(PROPOSAL_BLOCK_META_HEADER_SERIALIZED.length);
		int bodySize = BlockBody.serializedSize(txs);

		int size = HyFlexChainBlock.serializedSize(headerSize, bodySize);
		
		byte[] block = new byte[size];
		var buff = Unpooled.wrappedBuffer(block).setIndex(0, 0);

		buff.writeInt(headerSize + bodySize);
		buff.writeBytes(PROPOSAL_BLOCK_META_HEADER_SERIALIZED);
		BlockHeader.SERIALIZER.serializeAllButMetaHeader(header, buff);

        int bodyIndex = buff.writerIndex();
		BlockBody.SERIALIZER.serialize(txs, buff);

		return new SerializedBlock(block, bodyIndex);
	}

    /**
     * Create a reply listener
     * 
     * @return The created reply listener
     */
    private BftSmartReplyListener createReplyListener() {
        return new BftSmartReplyListener(
                getBftSmartClientAsync(),
                this.staticCommittee.getMiddle().getCommittee(),
                this.staticCommittee.getMiddle().getBftCriteria().consensusQuorum());
    }

    protected SerializedBlock createSerializedBlock(HyFlexChainSignature[] validators, long nonce,
            Bytes previous, Bytes merkleHash, SerializedBlock proposedBlock) throws IOException 
    {
        BlockMetaHeader metaHeader = new BlockMetaHeader(this.consensus, DIFF_TARGET, validators,
                this.staticCommittee.getLeft());

        /**
         * Due to the fact that bft-smart does not provide a method
         * for all replicas to receive the result of all we use
         * the hash and previous fields as the merkle hashes of the current
         * block and the previous block. Since it is impossible for a
         * replica to create a block because it need the signatures of all
         * the other replicas in the committee.
         */
        // metaHeader.setHash(body.getMerkleTree().getRoot().hash());

        BlockHeader header = BlockHeader.create(metaHeader, previous, merkleHash, nonce);

        int headerSize = header.serializedSize();
		int bodySize = proposedBlock.bodySize();

		int size = HyFlexChainBlock.serializedSize(headerSize, bodySize);
		
		byte[] block = new byte[size];
		var buff = Unpooled.wrappedBuffer(block).setIndex(0, 0);

		buff.writeInt(headerSize + bodySize);
        BlockHeader.SERIALIZER.serialize(header, buff);

        int bodyIndex = buff.writerIndex();
        proposedBlock.copyBodyTo(buff);

        return new SerializedBlock(block, bodyIndex);
    }

    @Override
    protected boolean verifyMetaHeader(HyFlexChainBlock block) {
        var metaHeader = block.header().getMetaHeader();
        return block.header().getMetaHeader().getConsensus() == this.consensus &&
                metaHeader.getDifficultyTarget() == DIFF_TARGET &&
                metaHeader.getCommitteeId().equals(this.staticCommittee.getLeft()) &&
                verifyValidators(block);
    }

    protected boolean verifyValidators(HyFlexChainBlock block) {
        var validators = block.header().getMetaHeader().getValidators();
        var previous = block.header().getPrevHash();
        var hash = block.header().getMerkleRoot();

        if (validators.length < this.staticCommittee.getMiddle().getBftCriteria().consensusQuorum()) {
            LOG.info("Invalid block meta header: not enough validators - " + validators.length);
            return false;
        }

        if (!this.staticCommittee.getMiddle().getCommittee()
                .containsAll(Stream.of(validators)
                        .map(HyFlexChainSignature::address)
                        .toList())) {
            LOG.info("Invalid block meta header: some validators do not belong in the current committee");
            return false;
        }

        var signedDataResult = BftsmartConsensusResult.ok(
            new VerifiedTransactionsReply(block.header().getNonce(),
                previous.toArrayUnsafe(), hash.toArrayUnsafe())
        );
        
        byte[] signedData;
        try {
            signedData = BytesOps.serialize(signedDataResult, BftsmartConsensusResult.SERIALIZER);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return false;
        }

        for (HyFlexChainSignature sig : validators) {
            try {
                if (!sig.verify(signedData)) {
                    LOG.info("Invalid block meta header: invalid validator signature");
                    return false;
                }

            } catch (InvalidKeyException | SignatureException | InvalidAddressException e) {
                LOG.info("Invalid block meta header: invalid validator signature", e);
                return false;
            }
        }

        return true;
    }

    @Override
    protected boolean verifyHeader(HyFlexChainBlock block, MerkleTree merkleTree) {
        var header = block.header();

        if (!header.getMerkleRoot().equals(merkleTree.getMerkleRootHash())) {
            LOG.info("Invalid block header: invalid  merkle root");
            return false;
        }

        return true;
    }

    @Override
    protected boolean verifyBody(BlockBody body) {
        var ti = TransactionInterface.getInstance();
        var txmanagement = TransactionManagement.getInstance();

        try {
            for (var tx : body.findTransactions().values()) {
                ti.verifyTx(tx);
                txmanagement.verifyTx(tx);
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * An implementation of the BFT-SMaRT replica.
     */
    protected class BFT_SMaRtServiceReplica extends DefaultSingleRecoverable implements ResetInterface {
        protected static final Logger LOG = LoggerFactory.getLogger(BFT_SMaRtServiceReplica.class);

        protected final byte[] EMPTY = new byte[0];

        protected final ServiceReplica replica;

        protected BftCommittee committee;
        protected final long nBlocksValidity;
        protected long successfullOrderedBlocks;

        protected final Address selfAddress;
        protected final KeyPair selfKeyPair;
        protected final SignatureAlgorithm sigAlg;

        protected Bytes lastBlockHash;

        protected BFT_SMaRtServiceReplica() {
            var config = BftSmartStaticCommitteeConsensus.this.config;
            this.replica = new ServiceReplica(
                    config.getBftSmartReplicaId(),
                    "",
                    // config.getBftSmartConfigFolder().getAbsolutePath(),
                    this, this, null, new DefaultReplier(), null);

            this.committee = staticCommittee.getMiddle();
            this.nBlocksValidity = this.committee.getBftCriteria().getValidity().blocks();
            this.successfullOrderedBlocks = 0;

            var multiledgerConfig = config.getLedgerConfig().getMultiLedgerConfig();
            this.selfAddress = multiledgerConfig.getSelfAddress();
            this.selfKeyPair = multiledgerConfig.getSelfKeyPair();
            this.sigAlg = Crypto.DEFAULT_SIGNATURE_TRANSFORMATION;
            this.lastBlockHash = lvi.getLastBlockHash(consensus);
        }

        @Override
        public void reset() {
            this.committee = staticCommittee.getMiddle();
            this.successfullOrderedBlocks = 0;
            this.lastBlockHash = lvi.getLastBlockHash(consensus);
        }

        /* protected void restartReplica() {
            this.replica.restart();
        } */

        /**
         * Verify the body of a block for integrity
         * and the validity of all transactions (including smart-contracts)
         * 
         * @param body
         * @return true if verified
         */
        protected boolean verifyBody(BlockBody body) {
            return BftSmartStaticCommitteeConsensus.super.verifyBody(body);
        }

        protected boolean isCommitteeActive() {
            return this.nBlocksValidity == -1 ||
                    this.nBlocksValidity > this.successfullOrderedBlocks;
        }

        @Override
        public byte[] appExecuteOrdered(byte[] arg0, MessageContext arg1) {

            if (!isCommitteeActive()) {
                LOG.info("BFT_SMART: Committee is no longer valid");
                return FALSE;
            }

            try {
                final HyFlexChainBlock block =
                    HyFlexChainBlock.SERIALIZER.deserialize(Unpooled.wrappedBuffer(arg0)); 

                /* if (blockBody.findTransactions().size() == 1
                        && blockBody.findTransactions().values().iterator()
                                .next().getTransactionType() == TransactionType.COMMITTEE_ROTATION) {
                    LOG.info("Restarting BFT-SMART replica");
                    restartReplica();
                    return EMPTY;
                } */

                /**
                 * Process an ordered operation through BFT-SMaRt:
                 * 1) verify the proposed transactions.
                 * 2) if valid, set the previous merkle tree hash to the current one
                 * 3) reply to the client, signed by this replica
                 */

                return processNormalBlockBody(block);

            } catch (Exception e) {
                Utils.logError(e, LOG);
                return FALSE;
            }
        }

        protected byte[] processNormalBlockBody(HyFlexChainBlock block) throws InvalidKeyException, SignatureException {

            MerkleTree merkleTree = MerkleTree.createMerkleTree(block.body().findTransactions().keySet());

			if (!merkleTree.getMerkleRootHash().equals(block.header().getMerkleRoot()))
			{
				LOG.info("Invalid block merkle root, provided={}, computed={}",
                    block.header().getMerkleRoot(), merkleTree.getMerkleRootHash());
                
				return BftsmartConsensusResult.failed(block.header().getMerkleRoot().toArrayUnsafe())
                        .signReply(this.selfAddress, this.selfKeyPair.getPrivate(), this.sigAlg)
                        .serialize();
			}

            if (!verifyBody(block.body()))
            {
                LOG.info("Invalid block body -> merkle root: " + merkleTree.getMerkleRootHash());

                return BftsmartConsensusResult.failed(merkleTree.getMerkleRootHash().toArrayUnsafe())
                        .signReply(this.selfAddress, this.selfKeyPair.getPrivate(), this.sigAlg)
                        .serialize();
            }

            /**
             * The merkle hash of the previous block
             */
            final Bytes previousMerkleHash = this.lastBlockHash;

            // Update previous merkle hash with the current one
            this.lastBlockHash = merkleTree.getMerkleRootHash();

            var reply = BftsmartConsensusResult.ok(new VerifiedTransactionsReply(
                    this.successfullOrderedBlocks,
                    previousMerkleHash.toArrayUnsafe(),
                    merkleTree.getMerkleRootHash().toArrayUnsafe()));

            var signedReply = reply.signReply(
                    this.selfAddress, this.selfKeyPair.getPrivate(), this.sigAlg);

            LOG.info("[{}] Committee: Accepted block with hash: {}", consensus, merkleTree.getMerkleRootHash());

            var replyBytes = signedReply.serialize();

            // increment n blocks
            this.successfullOrderedBlocks++;

            return replyBytes;
        }

        @Override
        public byte[] appExecuteUnordered(byte[] arg0, MessageContext arg1) {
            LOG.warn("Called execute unordered");
            return EMPTY;
        }

        @Override
        public byte[] getSnapshot() {
            return EMPTY;
        }

        @Override
        public void installSnapshot(byte[] arg0) {
        }
    }

    /**
     * An implementation of the Blockmess replica.
     */
    public class BlockmessConnector extends BlockmessWrapperClientTCP {

        public BlockmessConnector() throws UnknownHostException, IOException {
            super(
                    BftSmartStaticCommitteeConsensus.this.config.getBlockmessConnectorHost(),
                    BftSmartStaticCommitteeConsensus.this.config.getBlockmessConnectorPort());
        }

        /**
         * Process the received block.
         * Verifies if it was already processed and
         * verifies it (integrity and validity of the committee that approved it).
         * Then, delivers the verified block to the block processor.
         * 
         * @param data The serialized block
         */
        protected void processReceivedBlock(byte[] data) {
            try {
                final HyFlexChainBlock block =
                    HyFlexChainBlock.SERIALIZER.deserialize(Unpooled.wrappedBuffer(data));

                final HashedObject<HyFlexChainBlock> hashedBlock =
                    new HashedObject<>(block.header().getMerkleRoot(), block);

                if (blockProcessor.alreadyProcessed(hashedBlock)) {
                    LOG.info("Received already processed block: " + hashedBlock.hash());
                    return;
                }

                MerkleTree merkleTree = MerkleTree.createMerkleTree(block.body().findTransactions().keySet());

                if (!verifyBlock(block, merkleTree)) {
                    LOG.info("Received invalid block with hash: " + hashedBlock.hash());
                    return;
                }

                var nonce = block.header().getNonce();
                var committeeValidity = staticCommittee.getMiddle().getBftCriteria().getValidity();
                if (!committeeValidity.infiniteValidity() && nonce >= committeeValidity.blocks()) {
                    LOG.info(
                            "Received invalid block from committee with invalid validity with hash {}, nonce {}, validity {}",
                            hashedBlock.hash(), nonce, committeeValidity.blocks());
                    return;
                }

                LOG.info("[{}] Appended valid block with size={} & hash={}", consensus,
					data.length, hashedBlock.hash());

                // LOG.info("BFT-SMART (Blockmess): Received valid block from committee with hash: " +
                //         hashedBlock.hash());

                blockProcessor.processBlock(hashedBlock);

            } catch (Exception e) {
                LOG.info(e.getMessage());
            }
        }

        @Override
        public void processOperation(byte[] operation) {
            processReceivedBlock(operation);
        }
    }

    /**
     * SerializedBlock
     */
    public record SerializedBlock(byte[] block, int bodyIndex) {

        public int bodySize()
        {
            return block.length - bodyIndex;
        }

        public void copyBodyTo(ByteBuf buff)
        {
            buff.writeBytes(block, bodyIndex, bodySize());
        }
    }
}
