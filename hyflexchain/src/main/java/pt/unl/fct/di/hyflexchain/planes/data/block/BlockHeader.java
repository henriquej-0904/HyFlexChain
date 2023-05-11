package pt.unl.fct.di.hyflexchain.planes.data.block;

import java.security.MessageDigest;

import pt.unl.fct.di.hyflexchain.util.Utils;

/**
 * The Header of a Block.
 */
public class BlockHeader
{
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
	protected String prevHash;

	/**
	 * A hash of the root of the merkle tree of this block’s transactions
	 */
	protected String merkleRoot;

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
			String prevHash, String merkleRoot,
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
	public static BlockHeader create(BlockMetaHeader metaHeader, String prevHash,
			String merkleRoot, long nonce) {
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
	public String getPrevHash() {
		return prevHash;
	}

	/**
	 * A reference to the hash of the previous (parent) block in the chain
	 * @param prevHash the prevHash to set
	 */
	public void setPrevHash(String prevHash) {
		this.prevHash = prevHash;
	}

	/**
	 * A hash of the root of the merkle tree of this block’s transactions
	 * @return the merkleRoot
	 */
	public String getMerkleRoot() {
		return merkleRoot;
	}

	/**
	 * A hash of the root of the merkle tree of this block’s transactions
	 * @param merkleRoot the merkleRoot to set
	 */
	public void setMerkleRoot(String merkleRoot) {
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

	public MessageDigest calcHash(MessageDigest msgDigest)
	{
		metaHeader.calcHash(msgDigest);
		msgDigest.update(version.getBytes());
		msgDigest.update(prevHash.getBytes());
		msgDigest.update(merkleRoot.getBytes());
		msgDigest.update(Utils.toBytes(nonce));

		return msgDigest;
	}
	
}
