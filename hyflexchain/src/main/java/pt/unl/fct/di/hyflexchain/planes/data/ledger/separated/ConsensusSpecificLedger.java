package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusType;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;

/**
 * An interface for a consensus specific ledger implementation 
 */
public interface ConsensusSpecificLedger
{
	/**
	 * Get the consensus mechanism of this ledger implementation.
	 * @return The consensus mechanism
	 */
	ConsensusType getConsensusMechanism();

	/**
	 * Write an ordered block to the Ledger.
	 * @param block The ordered block
	 */
	void writeOrderedBlock(HyFlexChainBlock block);
}
