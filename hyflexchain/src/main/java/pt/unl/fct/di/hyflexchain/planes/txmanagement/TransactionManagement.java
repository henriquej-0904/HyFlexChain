package pt.unl.fct.di.hyflexchain.planes.txmanagement;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

/**
 * Responsible for managing transactions.
 */
public interface TransactionManagement {
	
	/**
	 * Receives a transaction to be verified and ordered by the system.
	 * Eventually, it will be inserted on a block and ordered
	 * through a consensus mechanism. After that, the block is
	 * appended to the Ledger.
	 * @param tx The transaction
	 */
	void dispatchTransaction(HyFlexChainTransaction tx);

}
