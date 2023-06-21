package pt.unl.fct.di.blockmess.wrapper.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import applicationInterface.ApplicationInterface;

/**
 * A wrapper for the blockmess system.
 */
public class BlockmessWrapperServer
{
    protected static final Logger LOG = LoggerFactory.getLogger(BlockmessWrapperServer.class);

    private static final byte[] TRUE = new byte[] {1};

    protected DataInputStream input;

    protected DataOutputStream output;

    protected final Lock lock;

    protected final Condition waitForStream;

    protected final Blockmess blockmess;

    /**
     * Initialize the wrapper and the underlying Blockmess
     * @param input The stream to read input from
     * @param output The stream to write the output
     */
    public BlockmessWrapperServer(String[] blockmessProps) {
        this.lock = new ReentrantLock();
        this.waitForStream = this.lock.newCondition();
        this.blockmess = new Blockmess(blockmessProps);
    }

    /**
     * Start waiting for requests in the input stream.
     * @throws IOException
     */
    public void start(InputStream input, OutputStream output) throws IOException
    {
        // LOG.info("Before lock");

        this.lock.lock();

        this.input = new DataInputStream(input);
        this.output = new DataOutputStream(output);

        this.waitForStream.signalAll();

        // LOG.info("After signal");

        this.lock.unlock();

        // LOG.info("After lock");

        while (true) {
            waitAndSubmitOperation();
        }
    }

    /**
     * Wait for a request/operation to submit.
     * @throws IOException
     */
    protected void waitAndSubmitOperation() throws IOException
    {
        // read
        int count = this.input.readInt();

        if (count == 0)
            return;

        byte[] operation = new byte[count];
        this.input.readFully(operation);

        // submit
        this.blockmess.invokeAsyncOperation(operation, (a) -> {} );
    }

    /**
     * Process the current operation.
     * @param operation
     */
    protected void processOperation(byte[] operation)
    {
        if (operation.length == 0)
            return;

        while (true) {

            var output = this.output;

            try {
                output.writeInt(operation.length);
                output.write(operation);
                
                // LOG.info("Sent operation");
                return;
                
            } catch (Exception e) {
                e.printStackTrace();

                // failed
                this.lock.lock();

                if (output == this.output)
                {
                    try {
                        // LOG.info("Waiting for streams to become available");
                        this.waitForStream.await();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }

                LOG.info("Streams are available lock");

                this.lock.unlock();
            }
        }
    }

    protected class Blockmess extends ApplicationInterface {

        public Blockmess(@NotNull String[] blockmessProperties) {
            super(blockmessProperties);
        }

        @Override
        public byte[] processOperation(byte[] operation) {
            // LOG.info("called process operation");

            BlockmessWrapperServer.this.processOperation(operation);
            return TRUE;
        }
    }

    
}
