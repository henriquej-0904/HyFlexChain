package pt.unl.fct.di.hyflexchain.planes.txmanagement;

import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.wrapper.TxWrapper;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.txpool.TxPool;

/**
 * Responsible for managing transactions.
 */
public interface TransactionManagement {

	public static TransactionManagement getInstance()
	{
		return TransactionManagementInstance.getInstance();
	}

	/**
	 * Get the transaction pool for the specified consensus
	 * mechanism.
	 * @param consensus The consensus
	 * @return the transaction pool for the specified consensus
	 * mechanism.
	 */
	TxPool getTxPool(ConsensusMechanism consensus);

	/**
	 * Verifies if a transaction is valid, the cryptographic signature is correct
	 * and other aspects.
	 * @param tx The transaction to verify
	 * @throws InvalidTransactionException if the transaction was not verified.
	 */
	void verifyTx(HyFlexChainTransaction tx) throws InvalidTransactionException;

	/**
	 * Receives a transaction to be verified
	 * and ordered by the system.
	 * Eventually, it will be inserted on a block and ordered
	 * through a consensus mechanism. After that, the block is
	 * appended to the Ledger.
	 * @param tx The transaction
	 */
	String dispatchTransaction(TxWrapper tx) throws InvalidTransactionException;

	/**
	 * Receives a transaction to be verified
	 * and ordered by the system.
	 * Eventually, it will be inserted on a block and ordered
	 * through a consensus mechanism. After that, the block is
	 * appended to the Ledger. <p>
	 * This method differs from {@link #dispatchTransaction(TxWrapper)}
	 * in the sense that it waits for the transaction to be
	 * finalized.
	 * @param tx The transaction
	 */
	String dispatchTransactionAndWait(TxWrapper tx) throws InvalidTransactionException;
}
