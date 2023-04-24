package pt.unl.fct.di.hyflexchain.planes.application.ti;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

/**
 * An interface to submit transactions.
 */
public interface TransactionInterface {
	
	/**
	 * Send transaction primitive.
	 * @param tx The transaction to send.
	 * 
	 * @return The generated transaction id.
	 */
	String sendTransaction(HyFlexChainTransaction tx);

}
