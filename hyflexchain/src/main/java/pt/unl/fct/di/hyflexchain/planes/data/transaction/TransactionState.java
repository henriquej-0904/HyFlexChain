package pt.unl.fct.di.hyflexchain.planes.data.transaction;

/**
 * The possible states of a transaction
 */
public enum TransactionState
{
	/**
	 * The finalized state
	 */
	FINALIZED,

	/**
	 * A pending state that can eventually become finalized
	 */
	PENDING,

	/**
	 * THe specified transaction was not found.
	 */
	NOT_FOUND;
}
