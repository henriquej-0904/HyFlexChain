package pt.unl.fct.di.hyflexchain.planes.data.block;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	public static BlockBody from(LinkedHashMap<String, HyFlexChainTransaction> transactions) {
		return new BlockBody(Version.V1_0.getVersion(),
			new MerkleTree(transactions.keySet()), transactions);
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
		return transactions.values().toArray(HyFlexChainTransaction[]::new);
	}

	/**
	 * The list of transactions in this block
	 * @param transactions the transactions to set
	 */
	public void setTransactions(HyFlexChainTransaction[] transactions) {
		this.transactions = Stream.of(transactions)
			.collect(
				Collectors.toMap(
					HyFlexChainTransaction::hash,
					UnaryOperator.identity(),
					(x, y) -> x,
					() -> LinkedHashMap.newLinkedHashMap(transactions.length)
				)
			);
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

	public LinkedHashMap<String, HyFlexChainTransaction> findTransactions()
	{
		return this.transactions;
	}


	public MessageDigest calcHash(MessageDigest msgDigest)
	{
		msgDigest.update(version.getBytes());
		msgDigest.update(merkleTree.getRoot().hash().getBytes());
		transactions.keySet().forEach((hash) -> msgDigest.update(hash.getBytes()));
		
		return msgDigest;
	}
	
}
