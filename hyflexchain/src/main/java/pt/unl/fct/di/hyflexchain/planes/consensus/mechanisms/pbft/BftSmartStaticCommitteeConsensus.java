package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.pbft;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultReplier;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import pt.unl.fct.di.blockmess.wrapper.client.tcp.BlockmessWrapperClientTCP;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.pbft.config.BFT_SMaRtConfig;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockMetaHeader;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;

public class BftSmartStaticCommitteeConsensus extends ConsensusInterface
{
    protected static final Logger LOG = LoggerFactory.getLogger(BftSmartStaticCommitteeConsensus.class);

    protected BFT_SMaRtConfig config;

    protected BFT_SMaRtServiceReplica bftSmartReplica;

    protected BlockmessConnector blockmess;

    /**
     * @param consensus
     * @param lvi
     */
    public BftSmartStaticCommitteeConsensus(LedgerViewInterface lvi) {
        super(ConsensusMechanism.BFT_SMaRt, lvi);
        
        this.config =
            new BFT_SMaRtConfig(MultiLedgerConfig.getInstance().getLedgerConfig(this.consensus));
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub
        
        // init bft smart
        this.bftSmartReplica = new BFT_SMaRtServiceReplica();
        
        // init blockmess connector
        initBlockmess();
    }

    protected void initBlockmess()
    {
        try {
            this.blockmess = new BlockmessConnector();
        } catch (IOException e) {
            throw new Error(e.getMessage(), e);
        }
    }



    @Override
    public void orderBlock(HyFlexChainBlock block) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'orderBlock'");
    }



    @Override
    protected HyFlexChainBlock createBlock(LinkedHashMap<String, HyFlexChainTransaction> txs) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createBlock'");
    }

    @Override
	protected boolean verifyMetaHeader(BlockMetaHeader metaHeader)
    {
        // TODO:
        return super.verifyMetaHeader(metaHeader);
    }

    protected class BFT_SMaRtServiceReplica extends DefaultSingleRecoverable
    {
        protected static final Logger LOG = LoggerFactory.getLogger(BFT_SMaRtServiceReplica.class);

        protected final byte[] EMPTY = new byte[0];

        protected ServiceReplica replica;

        protected BFT_SMaRtServiceReplica()
        {
            var config = BftSmartStaticCommitteeConsensus.this.config;
            this.replica = new ServiceReplica(
                config.getBftSmartReplicaId(),
                config.getBftSmartConfigFolder(),
                this, this, null, new DefaultReplier(), null);
        }

        @Override
        public byte[] appExecuteOrdered(byte[] arg0, MessageContext arg1) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'appExecuteOrdered'");
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
        public void installSnapshot(byte[] arg0) {}
    } 



    public class BlockmessConnector extends BlockmessWrapperClientTCP {

        public BlockmessConnector() throws UnknownHostException, IOException {
            super(
                BftSmartStaticCommitteeConsensus.this.config.getBlockmessConnectorHost(),
                BftSmartStaticCommitteeConsensus.this.config.getBlockmessConnectorPort()
            );
        }

        @Override
        public void processOperation(byte[] operation) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'processOperation'");
        }
        
    }
}
