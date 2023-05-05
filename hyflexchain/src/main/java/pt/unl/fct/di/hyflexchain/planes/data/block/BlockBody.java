package pt.unl.fct.di.hyflexchain.planes.data.block;

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
	protected HyFlexChainTransaction[] transactions;


	/**
	 * Create a new Block Body
	 * @param version The version of this header
	 * @param merkleTree The Merkle tree of this block’s transactions
	 * @param transactions The list of transactions in this block
	 */
	public BlockBody(String version, MerkleTree merkleTree, HyFlexChainTransaction[] transactions) {
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
	public BlockBody(HyFlexChainTransaction[] transactions) {
		this.version = Version.V1_0.getVersion();
		this.merkleTree = new MerkleTree(transactions);
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
	public HyFlexChainTransaction[] getTransactions() {
		return transactions;
	}

	/**
	 * The list of transactions in this block
	 * @param transactions the transactions to set
	 */
	public void setTransactions(HyFlexChainTransaction[] transactions) {
		this.transactions = transactions;
	}

	
}
