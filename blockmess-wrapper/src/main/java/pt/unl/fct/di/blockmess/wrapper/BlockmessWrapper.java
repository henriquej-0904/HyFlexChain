package pt.unl.fct.di.blockmess.wrapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jetbrains.annotations.NotNull;

import applicationInterface.ApplicationInterface;

/**
 * A wrapper for the blockmess system.
 */
public class BlockmessWrapper
{
    private static final byte[] EMPTY = new byte[0];

    protected DataInputStream input;

    protected DataOutputStream output;

    protected final Blockmess blockmess;

    /**
     * Initialize the wrapper and the underlying Blockmess
     * @param input The stream to read input from
     * @param output The stream to write the output
     */
    public BlockmessWrapper(String[] blockmessProps) {
        this.blockmess = new Blockmess(blockmessProps);
    }

    /**
     * Start waiting for requests in the input stream.
     * @throws IOException
     */
    public void start(InputStream input, OutputStream output) throws IOException
    {
        this.input = new DataInputStream(input);
        this.output = new DataOutputStream(output);

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
        if (operation.length == 0 || this.output == null)
            return;

        try {
            this.output.writeInt(operation.length);
            this.output.write(operation);
        } catch (Exception e) {
            this.input = null;
            this.output = null;
            
            e.printStackTrace();
        }
    }

    protected class Blockmess extends ApplicationInterface {

        public Blockmess(@NotNull String[] blockmessProperties) {
            super(blockmessProperties);
        }

        @Override
        public byte[] processOperation(byte[] operation) {
            BlockmessWrapper.this.processOperation(operation);
            return EMPTY;
        }
    }

    
}
