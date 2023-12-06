package pt.unl.fct.di.hyflexchain.planes.application.ti;

/**
 * The transaction is invalid
 */
public class InvalidTransactionException extends Exception {
	
	public InvalidTransactionException(String msg)
	{
		super(msg);
	}

	public InvalidTransactionException(String msg, Throwable t)
	{
		super(msg, t);
	}

	public static InvalidTransactionException invalidVersion(String version)
	{
		var msg = String.format("Invalid version : %s", version);
		return new InvalidTransactionException(msg);
	}

}
