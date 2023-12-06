package pt.unl.fct.di.hyflexchain.planes.application.ti;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.wrapper.TxWrapper;

/**
 * An interface to submit transactions.
 */
public interface TransactionInterface {

	public static TransactionInterface getInstance()
	{
		return TransactionInterfaceInstance.getInstance();
	}

	/**
	 * Verify a transaction.
	 * @param tx
	 * @throws InvalidTransactionException
	 */
	void verifyTx(HyFlexChainTransaction tx) throws InvalidTransactionException;

	/**
	 * Send transaction primitive:
	 * submits a transaction for verification and,
	 * if successfull, dispatch it to the system for ordering.
	 * @param tx The transaction to send.
	 * 
	 * @return The generated transaction id.
	 */
	String sendTransaction(TxWrapper tx) throws InvalidTransactionException;

	/**
	 * Send transaction primitive:
	 * submits a transaction for verification and,
	 * if successfull, dispatch it to the system for ordering. <p>
	 * This method differs from {@link #sendTransaction(TxWrapper)}
	 * in the sense that it waits for the transaction to be
	 * finalized.
	 * @param tx The transaction to send.
	 * 
	 * @return The generated transaction id.
	 */
	String sendTransactionAndWait(TxWrapper tx) throws InvalidTransactionException;

}
