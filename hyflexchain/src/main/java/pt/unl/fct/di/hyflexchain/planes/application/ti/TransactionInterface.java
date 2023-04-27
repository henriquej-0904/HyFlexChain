package pt.unl.fct.di.hyflexchain.planes.application.ti;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

/**
 * An interface to submit transactions.
 */
public interface TransactionInterface {
	
	/**
	 * Send transaction primitive:
	 * submits a transaction for verification and,
	 * if successfull, dispatch it to the system for ordering.
	 * @param tx The transaction to send.
	 * 
	 * @return The generated transaction id.
	 */
	String sendTransaction(HyFlexChainTransaction tx) throws InvalidTransactionException;

}
