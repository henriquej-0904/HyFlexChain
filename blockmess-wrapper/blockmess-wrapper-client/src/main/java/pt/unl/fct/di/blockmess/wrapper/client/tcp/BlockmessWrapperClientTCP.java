package pt.unl.fct.di.blockmess.wrapper.client.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.unl.fct.di.blockmess.wrapper.client.BlockmessWrapperClient;

/**
 * A client over TCP for the blockmess wrapper server.
 */
public abstract class BlockmessWrapperClientTCP extends BlockmessWrapperClient {
    protected static final Logger LOG = LoggerFactory.getLogger(BlockmessWrapperClientTCP.class);

    protected String host;

    protected int port;

    protected Socket blockmessSocket;

    /**
     * Create a new Blockmess Wrapper Client over TCP.
     * 
     * @param host
     * @param port
     */
    public BlockmessWrapperClientTCP(String host, int port) throws UnknownHostException, IOException {
        this.host = host;
        this.port = port;

        this.blockmessSocket = connect(host, port);
        this.input = new DataInputStream(this.blockmessSocket.getInputStream());
        this.output = new DataOutputStream(this.blockmessSocket.getOutputStream());
    }

    /**
     * Connect to the Blockmess Server.
     * 
     * @param host
     * @param port
     * @throws IOException
     * @throws UnknownHostException
     */
    protected Socket connect(String host, int port) throws UnknownHostException, IOException {
        try {
            Socket blockmessSocket = new Socket(host, port);
            blockmessSocket.setKeepAlive(true);
            return blockmessSocket;
        } catch (Exception e) {
            if (blockmessSocket == null)
                throw e;

            try {
                blockmessSocket.close();
            } catch (Exception e2) {
            }

            throw e;
        }
    }

    @Override
    protected void reset() {
        while (true) {

            if (this.blockmessSocket != null) {
                try {
                    this.blockmessSocket.close();
                } catch (Exception e) {
                    this.blockmessSocket = null;
                }
            }

            try {
                this.blockmessSocket = connect(host, port);
                this.input = new DataInputStream(this.blockmessSocket.getInputStream());
                this.output = new DataOutputStream(this.blockmessSocket.getOutputStream());
            } catch (Exception e) {
                LOG.error("Failed to reconnect to blockmess server");
                e.printStackTrace();
            }
        }

    }

    public static class TestClientWrapper extends BlockmessWrapperClientTCP {
        protected static final Logger LOG = LoggerFactory.getLogger(TestClientWrapper.class);
        int iter;

        public TestClientWrapper(String host, int port) throws UnknownHostException, IOException {
            super(host, port);
            iter = 0;
        }

        @Override
        public void processOperation(byte[] operation) {
            LOG.info("Received operation " + iter + " with " + operation.length + " size");
            iter++;
        }

        public static void main(String[] args) throws UnknownHostException, IOException {

            String host = args[0];
            int port = Integer.parseInt(args[1]);

            TestClientWrapper blockmess = new TestClientWrapper(host, port);
            blockmess.init();

            try (Scanner sc = new Scanner(System.in)) {
                sc.nextLine();
            }

            Random r = new Random(0);
            byte[] b = new byte[200];

            while (true) {
                r.nextBytes(b);
                blockmess.invokeAsync(b);
            }
        }
    }

}
