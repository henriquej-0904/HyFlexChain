package pt.unl.fct.di.blockmess.wrapper.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A client for the blockmess wrapper.
 */
public abstract class BlockmessWrapperClient {
    protected DataInputStream input;

    protected DataOutputStream output;

    protected final Lock lock;

    protected final BlockingQueue<byte[]> pendingOperations;

    protected final Thread submitOperationsThread;

    protected final Thread processOperationsThread;

    /**
     * Create a new client.
     */
    protected BlockmessWrapperClient() {
        this.pendingOperations = new ArrayBlockingQueue<>(1000);
        this.submitOperationsThread = new Thread(this::submitOperations);
        this.processOperationsThread = new Thread(this::processOperations);
        this.lock = new ReentrantLock();
    }

    /**
     * Initialize the Blockmess client.
     */
    public void init() {
        this.submitOperationsThread.start();
        this.processOperationsThread.start();
    }

    /**
     * Invoke async for blockmess.
     * 
     * @param operation The operation
     * @throws IOException if an error occurred
     */
    public void invokeAsync(byte[] operation) {
        try {
            pendingOperations.put(operation);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void submitOperations()
    {
        while (true) {
            try {
                submitOperation(pendingOperations.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void submitOperation(byte[] operation) {
        if (operation.length == 0)
            return;

        var output = this.output;

        while (true) {
            try {
                this.output.writeInt(operation.length);
                this.output.write(operation);
                return;
                
            } catch (Exception e) {
                e.printStackTrace();

                // failed
                this.lock.lock();

                if (output == this.output)
                    reset();

                this.lock.unlock();
            }
        }
    }

    /**
     * Reset the state of this client.
     */
    protected abstract void reset();

    /**
     * Process the current ordered operation.
     * 
     * @param operation
     */
    public abstract void processOperation(byte[] operation);

    /**
     * Process the received operations.
     */
    protected void processOperations() {
        while (true) {
            processOperation(receiveOrderedOperation());
        }
    }

    /**
     * Receive an ordered operation from Blockmess.
     * 
     * @return The operation.
     */
    protected byte[] receiveOrderedOperation() {
        while (true) {

            var input = this.input;

            try {
                int count = input.readInt();
                byte[] operation = new byte[count];

                input.readFully(operation);
                return operation;

            } catch (Exception e) {
                e.printStackTrace();

                // failed
                this.lock.lock();

                if (input == this.input)
                    reset();

                this.lock.unlock();
            }
        }
    }
}
