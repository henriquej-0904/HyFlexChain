package pt.unl.fct.di.hyflexchain.planes.data.block;

import java.security.MessageDigest;

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
	public void calcHash()
	{
		header().getMetaHeader()
			.setHash(
				Utils.toHex(
					calcHash(Crypto.getSha256Digest()).digest()
				)
			);
	}

	public MessageDigest calcHash(MessageDigest msgDigest)
	{
		header().calcHash(msgDigest);
		body().calcHash(msgDigest);
		return msgDigest;
	}
}
