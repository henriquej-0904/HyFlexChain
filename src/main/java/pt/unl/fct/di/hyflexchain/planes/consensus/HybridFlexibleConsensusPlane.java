package pt.unl.fct.di.hyflexchain.planes.consensus;

import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;

/**
 * The main Service Plane in HyFlexChain:
 * The Hybrid and Flexible Consensus Plane.
 * 
 * This Service Plane is responsible for ordering transactions/blocks
 * according to rules and policies defined by the execution of
 * Smart Contracts.
 * 
 * This Consensus Plane is Hybrid due to the support of multiple Consensus
 * mechanisms. Additionally, it is also Flexible by the possibility of users being able to
 * choose different Consensus mechanisms at runtime to order their transactions.
 */
public interface HybridFlexibleConsensusPlane
{
	/**
	 * Order a block according to the specified consensus mechanism.
	 * 
	 * Some consensus mechanisms are implemented in the Blockmess Layer, so this call
	 * is redirect to Blockmess in that case.
	 * @param block
	 * @param consensusType
	 */
	void orderBlock(HyFlexChainBlock block, ConsensusType consensusType);

	// callback -> blockOrdered(block, consensusType)
}
