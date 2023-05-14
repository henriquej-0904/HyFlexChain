package pt.unl.fct.di.hyflexchain.planes.data.block;

import java.security.MessageDigest;

import org.apache.logging.log4j.Logger;

import pt.unl.fct.di.hyflexchain.util.Crypto;
import pt.unl.fct.di.hyflexchain.util.Utils;

/**
 * Represents a block of transactions with all necessary metadata.
 */
public record HyFlexChainBlock(
	BlockHeader header, BlockBody body
)
{
	/**
	 * Calculate the hash of this block
	 * and set it to the hash field of the header.
	 */
	public void calcAndSetHash()
	{
		header().getMetaHeader()
			.setHash(calcHash());
	}

	public String calcHash()
	{
		return Utils.toHex(
			calcHash(Crypto.getSha256Digest()).digest()
		);
	}

	public MessageDigest calcHash(MessageDigest msgDigest)
	{
		header().calcHash(msgDigest);
		body().calcHash(msgDigest);
		return msgDigest;
	}

	public boolean verifyHash()
	{
		var hash = header().getMetaHeader().getHash();

		if (hash == null)
			return false;

		return hash.equalsIgnoreCase(calcHash());
	}

	public boolean verifyBlock(Logger log)
	{
		if (!verifyHash())
		{
			log.info("Block invalid hash.");
			return false;
		}

		if (! body().getMerkleTree().verifyTree(
			body().getTransactions().keySet()
		))
		{
			log.info("Block Merkle tree.");
			return false;
		}

		return true;
	}
}
