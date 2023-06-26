package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceProxy;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultReplier;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import pt.unl.fct.di.blockmess.wrapper.client.tcp.BlockmessWrapperClientTCP;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.SybilResistantCommitteeElection;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.config.BFT_SMaRtConfig;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.election.StaticElection;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.pow.PowConsensusThread;
import pt.unl.fct.di.hyflexchain.planes.data.DataPlane;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockBody;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockHeader;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockMetaHeader;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.TransactionManagement;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;

public final class BftSmartStaticCommitteeConsensus extends ConsensusInterface {
    protected static final Logger LOG = LoggerFactory.getLogger(BftSmartStaticCommitteeConsensus.class);

    protected BFT_SMaRtConfig config;

    protected SybilResistantCommitteeElection committeeElection;

    protected Committee staticCommittee;

    protected BFT_SMaRtServiceReplica bftSmartReplica;
    protected AsynchServiceProxy bftSmartClientAsync;

    protected BlockmessConnector blockmess;

    private static final byte[] TRUE = new byte[] { 1 };
    private static final byte[] FALSE = new byte[] { 0 };

    private static final int DIFF_TARGET = 0;
    private String[] VALIDATORS;
    private static final String COMMITTEE_ID = "0";
    private static final String COMMITTEE_BLOCK_HASH = "";

    /**
     * @param consensus
     * @param lvi
     */
    public BftSmartStaticCommitteeConsensus(LedgerViewInterface lvi) {
        super(ConsensusMechanism.BFT_SMaRt, lvi);

        this.config = new BFT_SMaRtConfig(MultiLedgerConfig.getInstance().getLedgerConfig(this.consensus));

        this.committeeElection = new StaticElection(this.config);
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub

        this.staticCommittee = this.committeeElection.performCommitteeElection(this.lvi, null);
        this.VALIDATORS = this.staticCommittee.getCommitteeAddresses()
                .stream().map(Address::address).toArray(String[]::new);

        // init bft smart
        this.bftSmartReplica = new BFT_SMaRtServiceReplica();

        // init blockmess connector
        // initBlockmess();

        new Thread(
			new BftSmartConsensusThread(this, config.getLedgerConfig()),
			"BFT-SMaRt-Consensus-Thread")
		.start();
    }

    protected void initBlockmess() {
        try {
            this.blockmess = new BlockmessConnector();
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

            this.bftSmartClientAsync = new AsynchServiceProxy(config.getBftSmartReplicaId() * 3256,
                    config.getBftSmartConfigFolder().getAbsolutePath(), null, null, null);

            return this.bftSmartClientAsync;
        }
    }

    @Override
    public void orderTxs(BlockBody blockBody) {
        try {
            byte[] operation = Utils.json.writeValueAsBytes(blockBody);
            byte[] reply = getBftSmartClientAsync().invokeOrdered(operation);

            boolean res = Arrays.equals(TRUE, reply);

            Map<String, Boolean> mapTxRes = blockBody.getTransactions().keySet().stream()
                    .collect(Collectors.toUnmodifiableMap(
                            (tx) -> tx, (tx) -> res));

            TransactionManagement.getInstance().getTxPool(this.consensus)
                    .removePendingTxsAndNotify(mapTxRes);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // @Override
    // public void orderBlock(HyFlexChainBlock block) {
    // LOG.info("Order block: " + block.header().getMetaHeader().getHash());

    // try {
    // byte[] requestBytes = Utils.json.writeValueAsBytes(block);

    // //TODO: blockmess

    // /* blockmess.invokeAsyncOperation(requestBytes,
    // (reply) -> {

    // boolean res = Arrays.equals(TRUE, reply.getLeft());

    // Map<String, Boolean> mapTxRes =
    // block.body().getTransactions().keySet().stream()
    // .collect(Collectors.toUnmodifiableMap(
    // (tx) -> tx, (tx) -> res)
    // );

    // try {
    // TransactionManagement.getInstance().getTxPool(POW)
    // .removePendingTxsAndNotify(mapTxRes);
    // } catch (InvalidTransactionException e) {
    // LOG.info(e.getMessage());
    // }

    // // LOG.info("blockmess reply: {}", reply);
    // }); */
    // } catch (JsonProcessingException e) {
    // e.printStackTrace();
    // }
    // }

    @Override
    protected HyFlexChainBlock createBlock(BlockBody body) {
        BlockMetaHeader metaHeader = new BlockMetaHeader(this.consensus, DIFF_TARGET, VALIDATORS,
                COMMITTEE_ID, COMMITTEE_BLOCK_HASH);

        BlockHeader header = BlockHeader.create(metaHeader, lvi.getLastBlockHash(this.consensus),
                body.getMerkleTree().getRoot().hash(),
                lvi.getBlockchainSize(this.consensus) + 1);

        HyFlexChainBlock block = new HyFlexChainBlock(header, body);
        block.calcAndSetHash();

        return block;
    }

    @Override
    protected boolean verifyMetaHeader(BlockMetaHeader metaHeader) {
        return super.verifyMetaHeader(metaHeader) &&
                metaHeader.getDifficultyTarget() == DIFF_TARGET &&
                Arrays.equals(VALIDATORS, metaHeader.getValidators()) &&
                metaHeader.getCommitteeId().equalsIgnoreCase(COMMITTEE_ID) &&
                metaHeader.getCommitteeBlockHash().equalsIgnoreCase(COMMITTEE_BLOCK_HASH);
    }

    protected class BFT_SMaRtServiceReplica extends DefaultSingleRecoverable {
        protected static final Logger LOG = LoggerFactory.getLogger(BFT_SMaRtServiceReplica.class);

        protected final byte[] EMPTY = new byte[0];

        protected ServiceReplica replica;

        protected BFT_SMaRtServiceReplica() {
            var config = BftSmartStaticCommitteeConsensus.this.config;
            this.replica = new ServiceReplica(
                    config.getBftSmartReplicaId(),
                    config.getBftSmartConfigFolder().getAbsolutePath(),
                    this, this, null, new DefaultReplier(), null);
        }

        @Override
        public byte[] appExecuteOrdered(byte[] arg0, MessageContext arg1) {
            try {
				BlockBody blockBody = Utils.json.readValue(arg0, BlockBody.class);
				
				if (!verifyBody(blockBody))
				{
					LOG.info("Invalid block body -> merkle tree: " + blockBody.getMerkleTree().getRoot().hash());
					return FALSE;
				}

				// process new valid block
				var block = createBlock(blockBody);
				DataPlane.getInstance().writeOrderedBlock(block, consensus);
    
				LOG.info("Appended valid block body with size=" + arg0.length + " & block hash: " +
					block.header().getMetaHeader().getHash());

				return TRUE;
                
            } catch (Exception e) {
                Utils.logError(e, LOG);
                return EMPTY;
            }
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

    public class BlockmessConnector extends BlockmessWrapperClientTCP {

        public BlockmessConnector() throws UnknownHostException, IOException {
            super(
                    BftSmartStaticCommitteeConsensus.this.config.getBlockmessConnectorHost(),
                    BftSmartStaticCommitteeConsensus.this.config.getBlockmessConnectorPort());
        }

        @Override
        public void processOperation(byte[] operation) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'processOperation'");
        }

    }

}
