package pt.unl.fct.di.hyflexchain.planes.execution.contracts;

public class InvalidSmartContractException extends Exception {

    public InvalidSmartContractException()
    {
        super();
    }

    public InvalidSmartContractException(String msg)
    {
        super(msg);
    }

    public InvalidSmartContractException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
