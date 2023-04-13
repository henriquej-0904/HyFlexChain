package pt.unl.fct.di.hyflexchain.planes.data.transaction;

/**
 * The different types of transactions.
 */
public enum TransactionType {
	
	/**
	 * Regular transactions: a transaction from one account to another.
	 */
	REGULAR,

	/**
	 * Contract deployment transactions: a transaction without a 'to' address,
	 * where the data field is used for the contract code.
	 */
	CONTRACT,

	/**
	 * Execution of a contract: a transaction that interacts with a deployed
	 * smart contract. In this case, 'to' address is the smart contract address.
	 */
	EXEC

}
