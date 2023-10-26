package pt.unl.fct.di.hyflexchain.planes.data.transaction;

/**
 * Trwon when an {@link Address} is invalid.
 */
public class InvalidAddressException extends Exception {
    
    public InvalidAddressException(String msg)
    {
        super(msg);
    }

    public InvalidAddressException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

}
