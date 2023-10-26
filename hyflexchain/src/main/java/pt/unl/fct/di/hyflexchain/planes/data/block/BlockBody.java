package pt.unl.fct.di.hyflexchain.planes.data.block;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.tuweni.bytes.Bytes;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HashedTx;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.SerializedTx;
import pt.unl.fct.di.hyflexchain.util.BytesOps;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

/**
 * The body of a Block.
 */
public class BlockBody implements BytesOps
{
	public static final Serializer SERIALIZER = new Serializer();

	/**
	 * The version of this header
	 */
	protected String version;

	/**
	 * The list of transactions in this block
	 */
	protected LinkedHashMap<Bytes, HyFlexChainTransaction> transactions;

	protected BlockBody(String version)
	{
		this.version = version;
	}

	/**
	 * Create a new Block Body
	 * @param version The version of this body
	 * @param transactions The list of transactions in this block
	 */
	public BlockBody(String version,
		LinkedHashMap<Bytes, HyFlexChainTransaction> transactions) {
		this.version = version;
		this.transactions = transactions;
	}

	/**
	 * Create a new Block Body with the specified version and list of
	 * transactions.
	 * 
	 * @param version The version of this body
	 * @param transactions The list of transactions in this block
	 */
	public static BlockBody from(String version, HashedTx... transactions) {
		var body = new BlockBody(version);
		body.setTransactions(transactions);
		return body;
	}

	/**
	 * Create a new Block Body with the current version and specified list of
	 * transactions.
	 * 
	 * @param transactions The list of transactions in this block
	 */
	public static BlockBody from(HashedTx... transactions) {
		var body = new BlockBody(Version.V1_0.getVersion());
		body.setTransactions(transactions);
		return body;
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
	public void setTransactions(HashedTx[] transactions) {
		this.transactions = Stream.of(transactions)
			.collect(
				Collectors.toMap(
					(tx) -> Bytes.wrap(tx.getTxHash()),
					HashedTx::getTx,
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
	public Optional<HyFlexChainTransaction> findTransaction(Bytes txHash)
	{
		return Optional.ofNullable(this.transactions.get(txHash));
	}


	/**
	 * Find a list of transaction given their tx hash.
	 * @param txHashes The hashes of the transactions
	 * @return The transactions
	 */
	public List<HyFlexChainTransaction> findTransactions(List<Bytes> txHashes)
	{
		List<HyFlexChainTransaction> txs = new ArrayList<>(txHashes.size());

		for (var txHash : txHashes) {
			var tx = this.transactions.get(txHash);
			if (tx != null)
				txs.add(tx);
		}

		return txs;
	}

	public LinkedHashMap<Bytes, HyFlexChainTransaction> findTransactions()
	{
		return this.transactions;
	}

	public MerkleTree createMerkleTree()
	{
		return MerkleTree.createMerkleTree(this.transactions.keySet());
	}

	@Override
	public int serializedSize() {
		return BytesOps.serializedSize(version)
			+ BytesOps.serializedSize(transactions.values());
	}

	public static int serializedSize(Collection<SerializedTx> txs) {
		return BytesOps.serializedSize(Version.V1_0.getVersion())
			+ Integer.BYTES + txs.stream().mapToInt((t) -> t.serialized().length)
				.sum();
	}

	public static class Serializer implements ISerializer<BlockBody>
	{
		protected static final ISerializer<String> stringSerializer =
			Utils.serializer.getSerializer(String.class);

		protected static final ISerializer<Collection<HyFlexChainTransaction>> txsSerializer =
			Utils.serializer.getCollectionSerializer(HyFlexChainTransaction.SERIALIZER, i -> new ArrayList<>(i));

		protected static final ISerializer<HashedTx[]> txsWrapperArraySerializer =
			Utils.serializer.getArraySerializer(HashedTx.class, HashedTx.SERIALIZER);

		protected static final ISerializer<Collection<SerializedTx>> serializedTxCollSerializer =
			Utils.serializer.getCollectionSerializer(SerializedTx.SERIALIZER, ArrayList::new);

		@Override
		public void serialize(BlockBody t, ByteBuf out) throws IOException {
			stringSerializer.serialize(t.version, out);
			txsSerializer.serialize(t.transactions.values(), out);
		}

		public void serialize(Collection<SerializedTx> txs, ByteBuf out) throws IOException {
			stringSerializer.serialize(Version.V1_0.getVersion(), out);
			out.writeInt(txs.size());
			txs.forEach(t -> out.writeBytes(t.serialized()));
			// serializedTxCollSerializer.serialize(txs, out);
		}

		@Override
		public BlockBody deserialize(ByteBuf in) throws IOException {
			return from(stringSerializer.deserialize(in),
				txsWrapperArraySerializer.deserialize(in));
		}
		
	}
	
}
