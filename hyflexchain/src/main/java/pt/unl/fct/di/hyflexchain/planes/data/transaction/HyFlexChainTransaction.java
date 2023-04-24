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
	 * The hash of the transaction (excluding signature)
	 */
	protected String hash;

	/**
	 * The address of the sender, that will be signing the transaction.
	 */
	protected String address;

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
	 * The input transactions
	 */
	protected TxInput[] inputTxs;

	/**
	 * The output transactions
	 */
	protected UTXO[] outputTxs;

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
