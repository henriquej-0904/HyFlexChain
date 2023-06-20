package pt.unl.fct.di.blockmess.wrapper;

import java.io.IOException;

/**
 * A wapper for blockmess that receives an operation to order through
 * stdin and writes to stdout the current decided operation.
 */
public class RedirectIOblockmessWrapper {

    public static void main(String[] args) throws IOException {
        BlockmessWrapper wrapper = new BlockmessWrapper(System.in, System.out);
        wrapper.start(args);
    }
}
