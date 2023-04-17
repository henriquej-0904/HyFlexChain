package pt.unl.fct.di.hyflexchain.planes.data.transaction;

/**
 * Represents a HyFlexChain Transaction type
 */
public class HyFlexChainTransaction {

	/**
	 * version
	 */

	/**
	* The transaction id (a hash of the transaction data).
	*/
	protected String id;

	/**
	 * The address of the sender, that will be signing the transaction.
	 * This will be an externally-owned account as contract accounts cannot send transactions.
	 */
	protected String from;

	/**
	 * The receiving address (if an externally-owned account,
	 * the transaction will transfer value.
	 * If a contract account, the transaction will execute the contract code)
	 */
	protected String recipient;

	protected String signatureType;

	/**
	 * The identifier of the sender.
	 * This is generated when the sender's private key signs the transaction
	 * and confirms the sender has authorized this transaction
	 */
	protected String signature;

	/**
	 * A sequentially incrementing counter which indicates the transaction number from the account
	 */
	protected long nonce;

	/**
	 * The amount of cryptocurrency to transfer from sender to recipient.
	 */
	protected int value;

	/**
	 * Optional field to include arbitrary data
	 */
	protected byte[] data;

	//TODO: Add gas to transaction

	//TODO smart contract (codigo, referencia)


	/**
	 * Create a transaction
	 */
	public HyFlexChainTransaction() {
	}
	
}
