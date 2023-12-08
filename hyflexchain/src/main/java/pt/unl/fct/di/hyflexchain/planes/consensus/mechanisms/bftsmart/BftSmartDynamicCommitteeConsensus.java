package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.SignatureException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.reconfiguration.CommitteeReconfUtils;
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
import pt.unl.fct.di.hyflexchain.planes.txmanagement.txpool.TxPool;
import pt.unl.fct.di.hyflexchain.util.BytesOps;
import pt.unl.fct.di.hyflexchain.util.ResetInterface;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;
import pt.unl.fct.di.hyflexchain.util.crypto.Crypto;
import pt.unl.fct.di.hyflexchain.util.crypto.HashedObject;
import pt.unl.fct.di.hyflexchain.util.crypto.HyFlexChainSignature;
import pt.unl.fct.di.hyflexchain.util.crypto.SignatureAlgorithm;
import pt.unl.fct.di.hyflexchain.util.stats.BlockStats;

/**
 * Dynamic committees:
 * -> 1 static committee;
 * -> when the committee ends the current committee = None;
 * -> if the node was in the committee then it knows immediatly that committee = None;
 * -> if the node was not in the committee then it discovers that the committee = None when
 *    it receives the last finalized block of that committee;
 * -> if submit thread finds committee = None then it waits;
 * -> when the new committee realizes that the old one ended (when committee = None)
 *    then it performs a consensus to initialize;
 * -> when the initialization succeeds then the committee is ready (committee = new committee);
 * -> all other nodes know that committee = new committee when receive the first block finalized
 *    by that new committee (the init block)
 */
public final class BftSmartDynamicCommitteeConsensus extends ConsensusInterface {
    protected static final Logger LOG = LoggerFactory.getLogger(BftSmartDynamicCommitteeConsensus.class);

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

    protected Address selfAddress;

    protected Optional<Triple<CommitteeId, BftCommittee, Map<Address, Host>>> currentCommittee;
    protected int committeeValidity;
    protected long successfullOrderedBlocks; 
    protected boolean isInCommittee;
    protected boolean acceptProposals;

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
     * Used for storing blocks that are waiting for finalization.
     * This is useful for committee reconfigurations as it allows to
     * propose all pending blocks when the committee changes.
     */
    protected Map<UUID, SerializedBlock> waitingForFinalization;

    /**
     * @param lvi
     */
    public BftSmartDynamicCommitteeConsensus(LedgerViewInterface lvi) {
        super(ConsensusMechanism.BFT_SMaRt, lvi);

        this.bftLVI = (BftSmartLVI) lvi.getLVI(consensus);

        this.config = new BFT_SMaRtConfig(MultiLedgerConfig.getInstance().getLedgerConfig(this.consensus));
        this.selfAddress = config.getLedgerConfig().getMultiLedgerConfig().getSelfAddress();
        this.blockProcessor = new VerifiedBlockProcessor(lvi, DataPlane.getInstance(), consensus
        , (block) -> {
            return block.header().getNonce() == committeeValidity - 1;
        }
        , (committeeId) -> processNextCommittee());
        this.waitingForFinalization = new ConcurrentHashMap<>(100);
    }

    @Override
    public void init() {
        this.currentCommittee = Optional.of(advanceToNextCommittee());
        this.committeeValidity = this.currentCommittee.get().getMiddle().getBftCriteria().getValidity().blocks();
        this.successfullOrderedBlocks = 0;
        this.isInCommittee = false;
        this.acceptProposals = false;

        // check if this node is in the committee
        if (this.currentCommittee.get().getMiddle().getCommittee().contains(selfAddress)) {
            // init bft smart
            this.bftSmartReplica = new BFT_SMaRtServiceReplica(this.currentCommittee.get(),
                config.getBftSmartReplicaId(), false);
            this.isInCommittee = true;
            this.acceptProposals = true;
        }

        // init blockmess connector
        initBlockmess();

        new Thread(
                new BftSmartConsensusThread(this, config.getLedgerConfig(),
                        this::getCurrentCommittee),
                "BFT-SMaRt-Consensus-Thread")
                .start();
    }

    private Optional<Triple<CommitteeId, BftCommittee, Map<Address, Host>>> getCurrentCommittee()
    {
        return this.acceptProposals ? this.currentCommittee : Optional.empty();
    }

    @Override
    public void reset() {
        ((ResetInterface) this.blockProcessor).reset();
        this.currentCommittee = Optional.of(advanceToNextCommittee());
        this.committeeValidity = this.currentCommittee.get().getMiddle().getBftCriteria().getValidity().blocks();
        this.successfullOrderedBlocks = 0;
        this.isInCommittee = false;
        this.acceptProposals = false;

        var selfAddress = config.getLedgerConfig().getMultiLedgerConfig().getSelfAddress();
        // check if this node is in the committee
        if (this.currentCommittee.get().getMiddle().getCommittee().contains(selfAddress)) {
            // reset bft smart
            ((ResetInterface) this.bftSmartReplica).reset();
            this.isInCommittee = true;
            this.acceptProposals = true;
        }
    }

    protected Triple<CommitteeId, BftCommittee, Map<Address, Host>> advanceToNextCommittee() {
        final var committee = this.bftLVI.advanceToNextCommittee().get();

        var directoryService = this.config.getLedgerConfig().getMultiLedgerConfig().getDirectoryService();
        var staticCommitteeHosts = directoryService.lookup(
                committee.getValue().getCommittee().toArray(Address[]::new));

        LOG.info("Committee: {}", staticCommitteeHosts);

        if (staticCommitteeHosts.size() < committee.getValue().size())
            LOG.warn(
                    "Cannot get some Hosts from committee addresses. Committee size: {}, Resolved Hosts size: {}",
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

    protected void processNextCommittee() {

        // set the new committee and initialize bft smart if the case
        this.committeeValidity = this.currentCommittee
                .get().getMiddle().getBftCriteria().getValidity().blocks();
        this.successfullOrderedBlocks = 0;
        this.isInCommittee = false;
        this.acceptProposals = false;

        var committee = advanceToNextCommittee();

        LOG.info("Committee switch: " + committee.getRight());

        // kill old bft smart replica
        if (this.bftSmartReplica != null)
        {
            this.bftSmartReplica.kill();
            this.bftSmartReplica = null;
        }

        // kill old bft smart client
        if (this.bftSmartClientAsync != null)
        {
            this.bftSmartClientAsync.close();
            this.bftSmartClientAsync = null;
        }

        this.currentCommittee = Optional.of(committee);

        // check if this node is in the committee
        if (this.currentCommittee.get().getMiddle().getCommittee()
                .contains(selfAddress)) {
            
            try {
                CommitteeReconfUtils.createBftsmartConfigFiles(committee.getMiddle(), committee.getRight());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new Error(e.getMessage(), e);
            }
            
            // init new bft smart replica
            this.bftSmartReplica = new BFT_SMaRtServiceReplica(
                committee, committee.getMiddle().getReplicaId(selfAddress), true
            );
            
            this.isInCommittee = true;
        } else
            this.acceptProposals = true;

        // propose again the pending blocks that were proposed when the previous committee finished.
		TxPool txPool = TransactionManagement.getInstance().getTxPool(consensus);
        txPool.addTxsIfAbsent(
            this.waitingForFinalization.values().stream()
                .map(SerializedBlock::txs)
                .flatMap(Collection::stream)
        );
        this.waitingForFinalization.clear();

        // set the new committee so the thread that create blocks of transactions continue to submit
        this.currentCommittee = Optional.of(committee);
    }

    @Override
    public void orderTxs(Collection<SerializedTx> txs) {

        List<Bytes> txHashes = txs.stream()
			.map(SerializedTx::hash)
			.map(Bytes::wrap)
			.collect(Collectors.toCollection(() -> new ArrayList<>(txs.size())));
		
		MerkleTree merkleTree = MerkleTree.createMerkleTree(txHashes);
        UUID id = UUID.randomUUID();

        try {

            SerializedBlock proposedBlock = createSerializedBlockProposal(txs, merkleTree);

            LOG.info("Propose block {} bytes w/ {} txs -> merkle tree: {}",
                proposedBlock.block.length, txs.size(), merkleTree.getMerkleRootHash());

            // put block in waiting for finalization collection
            this.waitingForFinalization.put(id, proposedBlock);

            if (!acceptProposals)
                return;
            
            var committee = this.currentCommittee.get();
            var listener = createReplyListener();

            final long before = System.currentTimeMillis();

            listener.submitAsyncOrderedRequest(proposedBlock.block, (reply) -> {
                try {
                    final long after = System.currentTimeMillis();

                    // remove block from waiting for finalization collection
                    this.waitingForFinalization.remove(id);

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

                    SerializedBlock block = createSerializedBlock(committee.getLeft(), reply.getSignatures().values()
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
            LOG.info("There is no current committee...");
            // e.printStackTrace();
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

		return new SerializedBlock(block, bodyIndex, txs);
	}

    /**
     * Create a reply listener
     * 
     * @return The created reply listener
     */
    private BftSmartReplyListener createReplyListener() {

        var committee = this.currentCommittee.get();

        return new BftSmartReplyListener(
                getBftSmartClientAsync(),
                committee.getMiddle().getCommittee(),
                committee.getMiddle().getBftCriteria().consensusQuorum());
    }

    protected SerializedBlock createSerializedBlock(CommitteeId committeeId, HyFlexChainSignature[] validators, long nonce,
            Bytes previous, Bytes merkleHash, SerializedBlock proposedBlock) throws IOException 
    {
        BlockMetaHeader metaHeader = new BlockMetaHeader(this.consensus, DIFF_TARGET, validators,
                committeeId);

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

        return new SerializedBlock(block, bodyIndex, proposedBlock.txs());
    }

    @Override
    protected boolean verifyMetaHeader(HyFlexChainBlock block) {
        var metaHeader = block.header().getMetaHeader();
        return block.header().getMetaHeader().getConsensus() == this.consensus &&
                metaHeader.getDifficultyTarget() == DIFF_TARGET &&
                metaHeader.getCommitteeId().equals(this.currentCommittee.get().getLeft()) &&
                verifyValidators(block);
    }

    protected boolean verifyValidators(HyFlexChainBlock block) {
        var validators = block.header().getMetaHeader().getValidators();
        var previous = block.header().getPrevHash();
        var hash = block.header().getMerkleRoot();

        if (validators.length < this.currentCommittee.get().getMiddle().getBftCriteria().consensusQuorum()) {
            LOG.info("Invalid block meta header: not enough validators - " + validators.length);
            return false;
        }

        if (!this.currentCommittee.get().getMiddle().getCommittee()
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
        
        protected final byte[] INIT_COMMITTEE_MSG = "INIT_COMMITTEE_MSG".getBytes();

        protected final ServiceReplica replica;

        protected BftCommittee committee;
        protected int committeeValidity;
        protected long successfullOrderedBlocks; 

        protected final Address selfAddress;
        protected final KeyPair selfKeyPair;
        protected final SignatureAlgorithm sigAlg;

        protected Bytes lastBlockHash;

        protected boolean shutdown;

        protected final byte[] initConsensusReply;

        protected BFT_SMaRtServiceReplica(Triple<CommitteeId, BftCommittee, Map<Address, Host>> committee,
            int replicaId, boolean performCommitteeInit) {
            var config = BftSmartDynamicCommitteeConsensus.this.config;

            this.replica = new ServiceReplica(
                    replicaId,
                    "",
                    // config.getBftSmartConfigFolder().getAbsolutePath(),
                    this, this, null, new DefaultReplier(), null);

            this.committee = committee.getMiddle();
            this.committeeValidity = BftSmartDynamicCommitteeConsensus.this.committeeValidity;
            this.successfullOrderedBlocks = BftSmartDynamicCommitteeConsensus.this.successfullOrderedBlocks;


            var multiledgerConfig = config.getLedgerConfig().getMultiLedgerConfig();
            this.selfAddress = multiledgerConfig.getSelfAddress();
            this.selfKeyPair = multiledgerConfig.getSelfKeyPair();
            this.sigAlg = Crypto.DEFAULT_SIGNATURE_TRANSFORMATION;
            this.lastBlockHash = lvi.getLastBlockHash(consensus);

            this.initConsensusReply = Utils.toBytes(this.committee.getId()).array();

            this.shutdown = false;

            if (performCommitteeInit)
                startThreadInitCommittee();
        }

        @Override
        public void reset() {
            this.committee = currentCommittee.get().getMiddle();
            this.committeeValidity = BftSmartDynamicCommitteeConsensus.this.committeeValidity;
            this.successfullOrderedBlocks = BftSmartDynamicCommitteeConsensus.this.successfullOrderedBlocks;
            this.lastBlockHash = lvi.getLastBlockHash(consensus);
        }

        /**
         * Verify the body of a block for integrity
         * and the validity of all transactions (including smart-contracts)
         * 
         * @param body
         * @return true if verified
         */
        protected boolean verifyBody(BlockBody body) {
            return BftSmartDynamicCommitteeConsensus.super.verifyBody(body);
        }

        protected boolean isCommitteeActive() {
            return this.committeeValidity == -1 ||
                    this.committeeValidity > this.successfullOrderedBlocks;
        }

        @Override
        public byte[] appExecuteOrdered(byte[] arg0, MessageContext arg1) {

            if (!isCommitteeActive()) {
                acceptProposals = false;
                LOG.info("BFT_SMART: Committee is no longer valid");
                return FALSE;
            }

            if (Arrays.equals(arg0, INIT_COMMITTEE_MSG))
            {
                LOG.info("Received Init Consensus Request!");
                return this.initConsensusReply;
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

        public void kill()
        {
            this.shutdown = true;
            this.replica.kill();
        }

        private void startThreadInitCommittee()
        {
            new Thread(this::initCommittee, "Init Committee " + committee.getId())
                .start();
        }

        private void initCommittee()
        {
            // while the committee is not initialized, i.e. does not accept proposals

            final var client = getBftSmartClientAsync();
            while (!acceptProposals && !this.shutdown)
            {
                try {
                    LOG.info("Submit consensus init request...");

                    final byte[] reply = client.invokeOrdered(INIT_COMMITTEE_MSG);

                    if (reply != null && Arrays.equals(this.initConsensusReply, reply))
                    {
                        LOG.info("Consensus init completed successfully, committee with id {} is ready!", committee.getId());
                        acceptProposals = true;
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(Duration.ofSeconds(1));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * An implementation of the Blockmess replica.
     */
    public class BlockmessConnector extends BlockmessWrapperClientTCP {

        public BlockmessConnector() throws UnknownHostException, IOException {
            super(
                    BftSmartDynamicCommitteeConsensus.this.config.getBlockmessConnectorHost(),
                    BftSmartDynamicCommitteeConsensus.this.config.getBlockmessConnectorPort());
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

                var currentCommittee = BftSmartDynamicCommitteeConsensus.this.currentCommittee;

                var blockCommittee = block.header().getMetaHeader().getCommitteeId();

                if (currentCommittee.isPresent()) {
                    if (currentCommittee.get().getLeft().equals(blockCommittee)) {
                        var nonce = block.header().getNonce();
                        if (nonce >= committeeValidity) {
                            LOG.info(
                                    "Received invalid block from committee with invalid validity with hash {}, nonce {}, validity {}",
                                    hashedBlock.hash(), nonce, committeeValidity);
                            return;
                        }

                        LOG.info("[{}] Appended valid block with size={} & hash={}", consensus,
                                data.length, hashedBlock.hash());

                        // LOG.info("BFT-SMART (Blockmess): Received valid block from committee with
                        // hash: " +
                        // hashedBlock.hash());

                        blockProcessor.processBlock(hashedBlock);
                    } else {
                        LOG.info("[{}] Received block from unknown or invalid committee.", consensus);
                    }
                } else {
                    var nextCommittee = bftLVI.getNextBftCommittee();

                    if (nextCommittee.isEmpty() || !nextCommittee.get().getKey().equals(blockCommittee))
                    {
                        LOG.info("[{}] The received block refers to a committee that is not the next one.", consensus);
                        return;
                    }

                    var nonce = block.header().getNonce();
                    if (nonce >= committeeValidity) {
                        LOG.info(
                                "Received invalid block from committee with invalid validity with hash {}, nonce {}, validity {}",
                                hashedBlock.hash(), nonce, committeeValidity);
                        return;
                    }

                    LOG.info("[{}] Appended valid first block for a new committee. Block with size={} & hash={}", consensus,
                            data.length, hashedBlock.hash());

                    // LOG.info("BFT-SMART (Blockmess): Received valid block from committee with
                    // hash: " +
                    // hashedBlock.hash());

                    blockProcessor.processBlock(hashedBlock);

                    
                }
                

                

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
    public record SerializedBlock(byte[] block, int bodyIndex, Collection<SerializedTx> txs) {

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
