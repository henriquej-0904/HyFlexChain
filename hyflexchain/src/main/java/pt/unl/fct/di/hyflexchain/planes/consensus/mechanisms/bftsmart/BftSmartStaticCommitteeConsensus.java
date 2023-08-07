package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.SignatureException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultReplier;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import pt.unl.fct.di.blockmess.wrapper.client.tcp.BlockmessWrapperClientTCP;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.application.ti.TransactionInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft.BftCommittee;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft.SybilResistantBftCommitteeElection;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.config.BFT_SMaRtConfig;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.election.StaticElection;
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
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.InvalidAddressException;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionType;
import pt.unl.fct.di.hyflexchain.planes.network.Host;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.TransactionManagement;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;
import pt.unl.fct.di.hyflexchain.util.crypto.Crypto;
import pt.unl.fct.di.hyflexchain.util.crypto.HyflexchainSignature;
import pt.unl.fct.di.hyflexchain.util.crypto.SignatureAlgorithm;

public final class BftSmartStaticCommitteeConsensus extends ConsensusInterface {
    protected static final Logger LOG = LoggerFactory.getLogger(BftSmartStaticCommitteeConsensus.class);

    protected BFT_SMaRtConfig config;

    protected SybilResistantBftCommitteeElection committeeElection;

    protected BftCommittee staticCommittee;

    protected BFT_SMaRtServiceReplica bftSmartReplica;
    protected AsynchServiceProxy bftSmartClientAsync;

    protected BlockmessConnector blockmess;

    /**
     * A blockchain structure to temporary save the ordered and valid
     * blocks received via blockmess.
     * When there are consecutive valid blocks,
     * then they are appendend to the blockchain and removed from
     * this collection.
     */
    protected VerifiedBlockProcessor blockProcessor;

    private static final byte[] FALSE = new byte[] { 0 };

    private static final int DIFF_TARGET = 0;
    private static final int COMMITTEE_ID = 0;
    private static final String COMMITTEE_BLOCK_HASH = "";

    /**
     * @param consensus
     * @param lvi
     */
    public BftSmartStaticCommitteeConsensus(LedgerViewInterface lvi) {
        super(ConsensusMechanism.BFT_SMaRt, lvi);

        this.config = new BFT_SMaRtConfig(MultiLedgerConfig.getInstance().getLedgerConfig(this.consensus));

        this.committeeElection = new StaticElection(this.config);

        this.blockProcessor = new VerifiedBlockProcessor(lvi, DataPlane.getInstance(), consensus);
    }

    @Override
    public void init() {
        this.staticCommittee = this.committeeElection
                .performCommitteeElection(this.config.getStaticElectionCriteria())
                .get();

        var directoryService = this.config.getLedgerConfig().getMultiLedgerConfig().getDirectoryService();
        var staticCommitteeHosts = directoryService.lookup(
                this.staticCommittee.getCommitteeAddresses().toArray(Address[]::new));

        LOG.info("Static committee: {}", staticCommitteeHosts);

        if (staticCommitteeHosts.size() < this.staticCommittee.size())
            LOG.warn(
                    "Cannot get some Hosts from static committee addresses. Committee size: {}, Resolved Hosts size: {}",
                    this.staticCommittee.size(), staticCommitteeHosts.size());

        final Pair<BftCommittee, Map<Address, Host>> staticCommitteePair = Pair.of(this.staticCommittee,
                staticCommitteeHosts);

        var selfAddress = config.getLedgerConfig().getMultiLedgerConfig().getSelfAddress();

        // check if this node is in the committee
        if (this.staticCommittee.getCommitteeAddresses().contains(selfAddress)) {
            // init bft smart
            this.bftSmartReplica = new BFT_SMaRtServiceReplica(this.staticCommittee);
        }

        // init blockmess connector
        initBlockmess();

        new Thread(
                new BftSmartConsensusThread(this, config.getLedgerConfig(),
                        () -> staticCommitteePair),
                "BFT-SMaRt-Consensus-Thread")
                .start();
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

    @Override
    public void orderTxs(BlockBody blockBody) {
        try {
            byte[] operation = Utils.json.writeValueAsBytes(blockBody);

            var listener = createReplyListener();
            listener.submitAsyncOrderedRequest(operation, (reply) -> {
                try {
                    BftsmartConsensusResult result = BftsmartConsensusResult.SERIALIZER.deserialize(
                            ByteBuffer.wrap(reply.getReplyBytes()));

                    boolean res = result.isOk();

                    Map<String, Boolean> mapTxRes = blockBody.findTransactions().keySet().stream()
                            .collect(Collectors.toUnmodifiableMap(
                                    (tx) -> tx, (tx) -> res));

                    TransactionManagement.getInstance().getTxPool(this.consensus)
                            .removePendingTxsAndNotify(mapTxRes);

                    if (!res) {
                        LOG.info("BFT-SMART: Invalided block from committee with {} signatures",
                                reply.getSignaturesCount());

                        return;
                    }

                    // txs ordered and valid
                    // disseminate through blockmess the created block

                    var okReply = result.getOkResult();
                    String previous = Bytes.wrap(okReply.prevBlockMerkleRootHash())
                            .toHexString();

                    var block = createBlock(reply.getSignatures().values()
                            .toArray(HyflexchainSignature[]::new), okReply.nonce(), previous, blockBody);

                    LOG.info("BFT-SMART: Validated block from committee with {} signatures: {}",
                            reply.getSignaturesCount(),
                            block.header().getMetaHeader().getHash());

                    // invoke blockmess
                    this.blockmess.invokeAsync(Utils.json.writeValueAsBytes(block));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Create a reply listener
     * 
     * @return The created reply listener
     */
    private BftSmartReplyListener createReplyListener() {
        return new BftSmartReplyListener(
                getBftSmartClientAsync(),
                this.staticCommittee.getCommitteeAddresses(),
                this.staticCommittee.getBftCriteria().consensusQuorum());
    }

    protected HyFlexChainBlock createBlock(HyflexchainSignature[] validators, long nonce,
            String previous, BlockBody body) {
        BlockMetaHeader metaHeader = new BlockMetaHeader(this.consensus, DIFF_TARGET, validators,
                COMMITTEE_ID, COMMITTEE_BLOCK_HASH);

        /**
         * Due to the fact that bft-smart does not provide a method
         * for all replicas to receive the result of all we use
         * the hash and previous fields as the merkle hashes of the current
         * block and the previous block. Since it is impossible for a
         * replica to create a block because it need the signatures of all
         * the other replicas in the committee.
         */
        metaHeader.setHash(body.getMerkleTree().getRoot().hash());

        BlockHeader header = BlockHeader.create(metaHeader, previous,
                body.getMerkleTree().getRoot().hash(),
                nonce);

        HyFlexChainBlock block = new HyFlexChainBlock(header, body);

        // block.calcAndSetHash();

        return block;
    }

    @Override
    protected boolean verifyMetaHeader(HyFlexChainBlock block) {
        var metaHeader = block.header().getMetaHeader();
        return block.header().getMetaHeader().getConsensus() == this.consensus &&
                metaHeader.getDifficultyTarget() == DIFF_TARGET &&
                verifyValidators(block) &&
                metaHeader.getCommitteeId() == COMMITTEE_ID &&
                metaHeader.getCommitteeBlockHash().equalsIgnoreCase(COMMITTEE_BLOCK_HASH);
    }

    protected boolean verifyValidators(HyFlexChainBlock block) {
        var validators = block.header().getMetaHeader().getValidators();
        var previous = block.header().getPrevHash();
        var hash = block.header().getMerkleRoot();

        if (validators.length < this.staticCommittee.getBftCriteria().consensusQuorum()) {
            LOG.info("Invalid block meta header: not enough validators - " + validators.length);
            return false;
        }

        if (!this.staticCommittee.getCommitteeAddresses()
                .containsAll(Stream.of(validators)
                        .map(HyflexchainSignature::address)
                        .toList())) {
            LOG.info("Invalid block meta header: some validators do not belong in the current committee");
            return false;
        }

        var data = BftsmartConsensusResult.SERIALIZER.serialize(
                BftsmartConsensusResult.ok(new VerifiedTransactionsReply(
                        block.header().getNonce(),
                        Bytes.fromHexString(previous).toArrayUnsafe(),
                        Bytes.fromHexString(hash).toArrayUnsafe())));

        for (HyflexchainSignature sig : validators) {
            try {
                if (!sig.verify(data)) {
                    LOG.info("Invalid block meta header: invalid validator signature");
                    return false;
                }

                data.position(0);

            } catch (InvalidKeyException | SignatureException | InvalidAddressException e) {
                LOG.info("Invalid block meta header: invalid validator signature", e);
                return false;
            }
        }

        return true;
    }

    @Override
    protected boolean verifyHeader(HyFlexChainBlock block) {
        var header = block.header();
        var body = block.body();

        if (!header.getMerkleRoot().equalsIgnoreCase(body.getMerkleTree().getRoot().hash())) {
            LOG.info("Invalid block header: invalid  merkle root");
            return false;
        }

        return true;
    }

    @Override
    protected boolean verifyBody(BlockBody body) {
        if (!body.getMerkleTree().verifyTree(
                body.findTransactions().keySet())) {
            LOG.info("Invalid Block Merkle tree.");
            return false;
        }

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
    protected class BFT_SMaRtServiceReplica extends DefaultSingleRecoverable {
        protected static final Logger LOG = LoggerFactory.getLogger(BFT_SMaRtServiceReplica.class);

        protected final byte[] EMPTY = new byte[0];

        protected final ServiceReplica replica;

        protected final BftCommittee committee;
        protected final long nBlocksValidity;
        protected long successfullOrderedBlocks;

        protected final Address selfAddress;
        protected final KeyPair selfKeyPair;
        protected final SignatureAlgorithm sigAlg;

        protected String lastBlockHash;

        protected BFT_SMaRtServiceReplica(BftCommittee committee) {
            var config = BftSmartStaticCommitteeConsensus.this.config;
            this.replica = new ServiceReplica(
                    config.getBftSmartReplicaId(),
                    "",
                    // config.getBftSmartConfigFolder().getAbsolutePath(),
                    this, this, null, new DefaultReplier(), null);

            this.committee = committee;
            this.nBlocksValidity = this.committee.getBftCriteria().getValidity().blocks();
            this.successfullOrderedBlocks = 0;

            var multiledgerConfig = config.getLedgerConfig().getMultiLedgerConfig();
            this.selfAddress = multiledgerConfig.getSelfAddress();
            this.selfKeyPair = multiledgerConfig.getSelfKeyPair();
            this.sigAlg = Crypto.DEFAULT_SIGNATURE_TRANSFORMATION;
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
                final BlockBody blockBody = Utils.json.readValue(arg0, BlockBody.class);

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

                return processNormalBlockBody(blockBody);

            } catch (Exception e) {
                Utils.logError(e, LOG);
                return FALSE;
            }
        }

        protected byte[] processNormalBlockBody(BlockBody blockBody) throws InvalidKeyException, SignatureException {
            if (!verifyBody(blockBody)) {
                final String merkleHash = blockBody.getMerkleTree().getRoot().hash();

                LOG.info("Invalid block body -> merkle tree: " + blockBody.getMerkleTree().getRoot().hash());

                return BftsmartConsensusResult.failed(
                        Bytes.fromHexString(merkleHash).toArrayUnsafe())
                        .signReply(this.selfAddress, this.selfKeyPair.getPrivate(), this.sigAlg)
                        .serialize();
            }

            final String merkleHash = blockBody.getMerkleTree().getRoot().hash();

            /**
             * The merkle hash of the previous block
             */
            final String previousMerkleHash = this.lastBlockHash;

            // Update previous merkle hash with the current one
            this.lastBlockHash = merkleHash;

            var reply = BftsmartConsensusResult.ok(new VerifiedTransactionsReply(
                    this.successfullOrderedBlocks,
                    Bytes.fromHexString(previousMerkleHash).toArrayUnsafe(),
                    Bytes.fromHexString(merkleHash).toArrayUnsafe()));

            var signedReply = reply.signReply(
                    this.selfAddress, this.selfKeyPair.getPrivate(), this.sigAlg);

            LOG.info("BFT-SMART: Accepted block with hash: " + merkleHash);

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
                final HyFlexChainBlock block = Utils.json.readValue(data, HyFlexChainBlock.class);

                if (blockProcessor.alreadyProcessed(block)) {
                    LOG.info("Received already processed block: " + block.header().getMetaHeader().getHash());
                    return;
                }

                if (!verifyBlock(block)) {
                    LOG.info("Received invalid block with hash: " + block.header().getMetaHeader().getHash());
                    return;
                }

                var nonce = block.header().getNonce();
                var committeeValidity = staticCommittee.getBftCriteria().getValidity();
                if (!committeeValidity.infiniteValidity() && nonce >= committeeValidity.blocks()) {
                    LOG.info(
                            "Received invalid block from committee with invalid validity with hash {}, nonce {}, validity {}",
                            block.header().getMetaHeader().getHash(), nonce, committeeValidity.blocks());
                    return;
                }

                LOG.info("BFT-SMART (Blockmess): Received valid block from committee with hash: " +
                        block.header().getMetaHeader().getHash());

                blockProcessor.processBlock(block);

            } catch (Exception e) {
                LOG.info(e.getMessage());
            }
        }

        @Override
        public void processOperation(byte[] operation) {
            processReceivedBlock(operation);
        }
    }

}
