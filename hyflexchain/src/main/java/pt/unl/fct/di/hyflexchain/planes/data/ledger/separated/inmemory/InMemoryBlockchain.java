package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.inmemory;

import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.util.collections.InsertionOrderedMap;

/**
* The blockchain: a map that preserves insertion order
* and for each key (Block Id) corresponds a Block.
*/
public class InMemoryBlockchain extends InsertionOrderedMap<String, HyFlexChainBlock>
{
	protected final static int BLOCKCHAIN_INIT_SIZE = 1000;

	/**
	 * 
	 */
	public InMemoryBlockchain()
	{
		super(BLOCKCHAIN_INIT_SIZE);
	}

	public InMemoryBlockchain(int expectedSize)
	{
		super(expectedSize);
	}
	
}
