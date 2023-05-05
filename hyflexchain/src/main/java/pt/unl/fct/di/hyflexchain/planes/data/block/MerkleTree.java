package pt.unl.fct.di.hyflexchain.planes.data.block;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

/**
 * A Merkle Tree for transactions in a block.
 */
public class MerkleTree
{
	/**
	 * Create a new Merkle Tree based on a list of transactions.
	 * @param transactions The list of transactions
	 */
	public MerkleTree(HyFlexChainTransaction[] transactions)
	{

	}

	public String rootHash()
	{
		throw new Error("not implemented");
	}
}
