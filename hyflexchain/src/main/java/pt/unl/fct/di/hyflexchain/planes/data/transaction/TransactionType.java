package pt.unl.fct.di.hyflexchain.planes.data.transaction;

/**
 * The different types of transactions.
 */
public enum TransactionType {
	
	/**
	 * Regular transactions: a transaction that transfers assets/tokens.
	 */
	TRANSFER ((byte) 1),

	/**
	 * Contract deployment transactions: a transaction that creates
	 * and installs a smart contract on the chain. After being installed
	 * it can be referenced in future transactions for execution.
	 */
	CONTRACT_CREATE ((byte) 2),

	/**
	 * Revoke a contract previously installed on the chain.
	 * After this transaction is approved/executed it is no longer
	 * possible to reference and execute the revoked smart contract.
	 */
	CONTRACT_REVOKE ((byte) 3),

	/**
	 * An internal type used by HyFlexChain nodes to propose
	 * committees for the future.
	 */
	COMMITTEE_ELECTION ((byte) 4),

	/**
	 * An internal type used by HyFlexChain nodes to rotate
	 * the currently executing committee.
	 */
	COMMITTEE_ROTATION ((byte) 5);


	public final byte id;

	/**
	 * @param id
	 */
	private TransactionType(byte id) {
		this.id = id;
	}

}
