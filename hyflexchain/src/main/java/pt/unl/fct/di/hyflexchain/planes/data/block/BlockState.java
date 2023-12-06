package pt.unl.fct.di.hyflexchain.planes.data.block;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionState;

/**
 * The possible states of a block
 */
public enum BlockState {
	/**
	 * The finalized state
	 */
	FINALIZED,

	/**
	 * A pending state that can eventually become finalized
	 */
	PENDING;


	public TransactionState toTransactionState()
	{
		TransactionState res = null;

		switch (this) {
			case PENDING:
				res = TransactionState.PENDING;
				break;

			case FINALIZED:
				res = TransactionState.FINALIZED;
				break;
		}

		return res;
	}
}
