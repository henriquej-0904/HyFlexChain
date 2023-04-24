package pt.unl.fct.di.hyflexchain.planes.application.ti;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

/**
 * An interface to submit transactions.
 */
public interface TransactionInterface {
	
	/**
	 * Send transaction primitive:
	 * submits a transaction for verification and,
	 * if successfull, add it to the transaction pool.
	 * Eventually, it will be inserted on a block and ordered
	 * through a consensus mechanism. After that, the block is
	 * appended to the Ledger.
	 * @param tx The transaction to send.
	 * 
	 * @return The generated transaction id.
	 */
	String sendTransaction(HyFlexChainTransaction tx);

}
