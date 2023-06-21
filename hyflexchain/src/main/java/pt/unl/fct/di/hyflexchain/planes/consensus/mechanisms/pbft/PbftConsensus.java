package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.pbft;

import java.io.IOException;
import java.net.UnknownHostException;

import pt.unl.fct.di.blockmess.wrapper.client.tcp.BlockmessWrapperClientTCP;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusInterface;

public class PbftConsensus extends ConsensusInterface {
    

    public class BlockmessConnector extends BlockmessWrapperClientTCP {

        public BlockmessConnector(String host, int port) throws UnknownHostException, IOException {
            super(host, port);
            //TODO Auto-generated constructor stub
        }

        @Override
        public void processOperation(byte[] operation) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'processOperation'");
        }
        
    }
}
