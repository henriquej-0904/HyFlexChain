package pt.unl.fct.di.hyflexchain.planes.data.transaction;

/**
 * Represents a HyFlexChain Transaction
 */
public class HyFlexChainTransaction {

	/**
	 * The version of the transaction.
	 */
	protected String version;

	/**
	* The transaction id.
	*/
	protected String id;

	/**
	 * The hash of the transaction (excluding signature)
	 */
	protected String hash;

	/**
	 * The address of the sender, that will be signing the transaction.
	 */
	protected String from;

	/**
	 * The receiving address.
	 */
	protected String recipient;

	/**
	 * The signature algorithm.
	 */
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
