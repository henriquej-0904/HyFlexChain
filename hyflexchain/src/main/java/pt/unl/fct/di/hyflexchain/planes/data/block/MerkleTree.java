package pt.unl.fct.di.hyflexchain.planes.data.block;

import java.util.Collection;

/**
 * A Merkle Tree for transactions in a block.
 */
public class MerkleTree
{
	/**
	 * Create a new Merkle Tree based on a list of transactions.
	 * @param txHashes The list of transaction hashes
	 */
	public MerkleTree(Collection<String> txHashes)
	{

	}

	public String rootHash()
	{
		throw new Error("not implemented");
	}
}
