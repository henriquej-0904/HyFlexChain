package pt.unl.fct.di.hyflexchain.planes.data.block;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

/**
 * The body of a Block.
 */
public class BlockBody
{
	/**
	 * The version of this header
	 */
	protected String version;

	/**
	 * The Merkle tree of this block’s transactions
	 */
	protected MerkleTree merkleTree;

	/**
	 * The list of transactions in this block
	 */
	protected LinkedHashMap<String, HyFlexChainTransaction> transactions;


	/**
	 * Create a new Block Body
	 * @param version The version of this header
	 * @param merkleTree The Merkle tree of this block’s transactions
	 * @param transactions The list of transactions in this block
	 */
	public BlockBody(String version, MerkleTree merkleTree,
		LinkedHashMap<String, HyFlexChainTransaction> transactions) {
		this.version = version;
		this.merkleTree = merkleTree;
		this.transactions = transactions;
	}

	/**
	 * Create a new Block Body with the current version and specified list of
	 * transactions. It also creates a new Merkle Tree for the transactions.
	 * 
	 * @param transactions The list of transactions in this block
	 */
	public BlockBody(LinkedHashMap<String, HyFlexChainTransaction> transactions) {
		this.version = Version.V1_0.getVersion();
		this.merkleTree = new MerkleTree(transactions.keySet());
		this.transactions = transactions;
	}

	/**
	 * Create a new Block Body.
	 * 
	 */
	public BlockBody() {}


	/**
	 * The version of this Block Body
	 */
	public static enum Version {
		V1_0("v1.0");

		private String version;

		/**
		 * @param version
		 */
		private Version(String version) {
			this.version = version;
		}

		/**
		 * @return the version
		 */
		public String getVersion() {
			return version;
		}
	}


	/**
	 * The version of this header
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * The version of this header
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * The Merkle tree of this block’s transactions
	 * @return the merkleTree
	 */
	public MerkleTree getMerkleTree() {
		return merkleTree;
	}

	/**
	 * The Merkle tree of this block’s transactions
	 * @param merkleTree the merkleTree to set
	 */
	public void setMerkleTree(MerkleTree merkleTree) {
		this.merkleTree = merkleTree;
	}

	/**
	 * The list of transactions in this block
	 * @return the transactions
	 */
	public LinkedHashMap<String, HyFlexChainTransaction> getTransactions() {
		return transactions;
	}

	/**
	 * The list of transactions in this block
	 * @param transactions the transactions to set
	 */
	public void setTransactions(LinkedHashMap<String, HyFlexChainTransaction> transactions) {
		this.transactions = transactions;
	}

	/**
	 * Find a transaction given its tx hash.
	 * @param txHash The hash of the transaction
	 * @return The transaction
	 */
	public Optional<HyFlexChainTransaction> findTransaction(String txHash)
	{
		return Optional.ofNullable(this.transactions.get(txHash));
	}


	/**
	 * Find a list of transaction given their tx hash.
	 * @param txHashes The hashes of the transactions
	 * @return The transactions
	 */
	public List<HyFlexChainTransaction> findTransactions(List<String> txHashes)
	{
		List<HyFlexChainTransaction> txs = new ArrayList<>(txHashes.size());

		for (String txHash : txHashes) {
			var tx = this.transactions.get(txHash);
			if (tx != null)
				txs.add(tx);
		}

		return txs;
	}
	
}
