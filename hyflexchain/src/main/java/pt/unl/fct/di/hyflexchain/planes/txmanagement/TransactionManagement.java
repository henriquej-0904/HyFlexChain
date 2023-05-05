package pt.unl.fct.di.hyflexchain.planes.txmanagement;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

/**
 * Responsible for managing transactions.
 */
public interface TransactionManagement {
	
	/**
	 * Receives a transaction to be dispatched by the system.
	 * @param tx The transaction
	 */
	void dispatchTransaction(HyFlexChainTransaction tx);

}
