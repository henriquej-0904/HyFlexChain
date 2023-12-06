package pt.unl.fct.di.hyflexchain.planes.data.block;

import java.io.IOException;

import org.apache.tuweni.bytes.Bytes;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.BytesOps;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

/**
 * The Header of a Block.
 */
public class BlockHeader implements BytesOps
{
	public static final Serializer SERIALIZER = new Serializer();

	/**
	 * The meta header of this block
	 */
	protected BlockMetaHeader metaHeader;

	/**
	 * The version of this header
	 */
	protected String version;

	/**
	 * A reference to the hash of the previous (parent) block in the chain
	 */
	protected Bytes prevHash;

	/**
	 * A hash of the root of the merkle tree of this block's transactions
	 */
	protected Bytes merkleRoot;

	/**
	 * A nonce for this block.
	 */
	protected long nonce;

	/**
	 * Create a new Block Header.
	 * 
	 * @param metaHeader The meta header of this block
	 * @param version The version of this header
	 * @param prevHash A reference to the hash of the previous (parent) block in the chain
	 * @param merkleRoot A hash of the root of the merkle tree of this block’s transactions
	 * @param nonce A nonce for this block.
	 */
	public BlockHeader(BlockMetaHeader metaHeader, String version,
			Bytes prevHash, Bytes merkleRoot,
			long nonce) {
		this.metaHeader = metaHeader;
		this.version = version;
		this.prevHash = prevHash;
		this.merkleRoot = merkleRoot;
		this.nonce = nonce;
	}

	/**
	 * Create a new Block Header with the current version.
	 * 
	 * @param metaHeader The meta header of this block
	 * @param prevHash A reference to the hash of the previous (parent) block in the chain
	 * @param merkleRoot A hash of the root of the merkle tree of this block’s transactions
	 * @param nonce A nonce for this block.
	 */
	public static BlockHeader create(BlockMetaHeader metaHeader, Bytes prevHash, Bytes merkleRoot, long nonce) {
		return new BlockHeader(metaHeader, Version.V1_0.getVersion(),
			prevHash, merkleRoot, nonce);
	}

	/**
	 * Create a new Block Header.
	 */
	public BlockHeader() {}

	/**
	 * The version of this Block Header
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
	 * The meta header of this block
	 * @return the metaHeader
	 */
	public BlockMetaHeader getMetaHeader() {
		return metaHeader;
	}

	/**
	 * The meta header of this block
	 * @param metaHeader the metaHeader to set
	 */
	public void setMetaHeader(BlockMetaHeader metaHeader) {
		this.metaHeader = metaHeader;
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
	 * A reference to the hash of the previous (parent) block in the chain
	 * @return the prevHash
	 */
	public Bytes getPrevHash() {
		return prevHash;
	}

	/**
	 * A reference to the hash of the previous (parent) block in the chain
	 * @param prevHash the prevHash to set
	 */
	public void setPrevHash(Bytes prevHash) {
		this.prevHash = prevHash;
	}

	/**
	 * A hash of the root of the merkle tree of this block’s transactions
	 * @return the merkleRoot
	 */
	public Bytes getMerkleRoot() {
		return merkleRoot;
	}

	/**
	 * A hash of the root of the merkle tree of this block’s transactions
	 * @param merkleRoot the merkleRoot to set
	 */
	public void setMerkleRoot(Bytes merkleRoot) {
		this.merkleRoot = merkleRoot;
	}

	/**
	 * A nonce for this block.
	 * @return the nonce
	 */
	public long getNonce() {
		return nonce;
	}

	/**
	 * A nonce for this block.
	 * @param nonce the nonce to set
	 */
	public void setNonce(long nonce) {
		this.nonce = nonce;
	}

	@Override
	public int serializedSize() {
		return metaHeader.serializedSize()
			+ BytesOps.serializedSize(version)
			+ BytesOps.serializedSize(prevHash)
			+ BytesOps.serializedSize(merkleRoot)
			+ Long.BYTES;
	}

	public int serializedSize(int metaHeaderSize) {
		return metaHeaderSize
			+ BytesOps.serializedSize(version)
			+ BytesOps.serializedSize(prevHash)
			+ BytesOps.serializedSize(merkleRoot)
			+ Long.BYTES;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BlockHeader other = (BlockHeader) obj;
		if (metaHeader == null) {
			if (other.metaHeader != null)
				return false;
		} else if (!metaHeader.equals(other.metaHeader))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		if (prevHash == null) {
			if (other.prevHash != null)
				return false;
		} else if (!prevHash.equals(other.prevHash))
			return false;
		if (merkleRoot == null) {
			if (other.merkleRoot != null)
				return false;
		} else if (!merkleRoot.equals(other.merkleRoot))
			return false;
		if (nonce != other.nonce)
			return false;
		return true;
	}


	public static class Serializer implements ISerializer<BlockHeader>
	{

		protected static final ISerializer<byte[]> byteArraySerializer =
			Utils.serializer.getArraySerializerByte();

		protected static final ISerializer<String> stringSerializer =
			Utils.serializer.getSerializer(String.class);

		@Override
		public void serialize(BlockHeader t, ByteBuf out) throws IOException {
			BlockMetaHeader.SERIALIZER.serialize(t.metaHeader, out);
			stringSerializer.serialize(t.version, out);
			byteArraySerializer.serialize(t.prevHash.toArrayUnsafe(), out);
			byteArraySerializer.serialize(t.merkleRoot.toArrayUnsafe(), out);
			out.writeLong(t.nonce);
		}

		public void serializeAllButMetaHeader(BlockHeader t, ByteBuf out) throws IOException {
			stringSerializer.serialize(t.version, out);
			byteArraySerializer.serialize(t.prevHash.toArrayUnsafe(), out);
			byteArraySerializer.serialize(t.merkleRoot.toArrayUnsafe(), out);
			out.writeLong(t.nonce);
		}

		@Override
		public BlockHeader deserialize(ByteBuf in) throws IOException {
			var res = new BlockHeader();

			res.metaHeader = BlockMetaHeader.SERIALIZER.deserialize(in);
			res.version = stringSerializer.deserialize(in);
			res.prevHash = Bytes.wrap(byteArraySerializer.deserialize(in));
			res.merkleRoot = Bytes.wrap(byteArraySerializer.deserialize(in));
			res.nonce = in.readLong();

			return res;
		}
		
	}
	
}
