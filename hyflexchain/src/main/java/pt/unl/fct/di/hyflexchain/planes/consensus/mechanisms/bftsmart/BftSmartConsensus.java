package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.unl.fct.di.blockmess.wrapper.client.tcp.BlockmessWrapperClientTCP;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockBody;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockMetaHeader;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

/**
 * An implementation of a consensus mechanism based on the
 * BFT-SMaRt system. For this purpose we use temporary committees
 * of nodes for the consensus of blocks of transactions.
 * For the security of the system, these committees are elected
 * with sybil resistant guarantees.
 */
public class BftSmartConsensus extends ConsensusInterface
{
    protected static final Logger LOG = LoggerFactory.getLogger(BftSmartConsensus.class);

    protected BlockmessConnector blockmess;

    /**
     * @param consensus
     * @param lvi
     */
    public BftSmartConsensus(LedgerViewInterface lvi) {
        super(ConsensusMechanism.BFT_SMaRt, lvi);
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'init'");
    }



    /* @Override
    public void orderBlock(HyFlexChainBlock block) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'orderBlock'");
    } */

    



    public class BlockmessConnector extends BlockmessWrapperClientTCP {

        public BlockmessConnector(String host, int port) throws UnknownHostException, IOException {
            super(host, port);
        }

        @Override
        public void processOperation(byte[] operation) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'processOperation'");
        }
        
    }



    @Override
    public void orderTxs(BlockBody blockBody) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'orderTxs'");
    }

    



    
}
