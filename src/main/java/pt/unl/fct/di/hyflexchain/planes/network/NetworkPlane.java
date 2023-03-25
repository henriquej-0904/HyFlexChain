package pt.unl.fct.di.hyflexchain.planes.network;

import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

/**
 * The base Service Plane that deals with communication through the network.
 * 
 */
public interface NetworkPlane
{
	void broadcastTx(HyFlexChainTransaction tx);

	void broadcastBlock(HyFlexChainBlock block);

	
}
