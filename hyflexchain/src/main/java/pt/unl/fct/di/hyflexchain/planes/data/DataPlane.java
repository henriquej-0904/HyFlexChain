package pt.unl.fct.di.hyflexchain.planes.data;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.util.config.LedgerConfig;

/**
 * Represents the Data Plane.
 */
public interface DataPlane {

	/**
	 * Get the applied Ledger parameters
	 * @return The applied Ledger parameters
	 */
	LedgerConfig getLedgerConfig();

	/**
	 * Write an ordered block to the Ledger.
	 * @param block The ordered block
	 * @param consensusType The type of consensus mechanism used to order the block
	 */
	void writeOrderedBlock(HyFlexChainBlock block, ConsensusMechanism consensusType);

	/**
	 * Dispatch an unordered block to the Blockmess Layer to be ordered
	 * by the specified consensus mechanism.
	 * @param block The unordered block to dispatch to Blockmess
	 * @param consensusType The type of consensus mechanism to be used to order the block
	 */
	// void dispatchUnorderedBlockToBlockmess(HyFlexChainBlock block, ConsensusType consensusType);

	/**
	 * Dispatch an unordered transaction to the Blockmess Layer to be ordered
	 * by the specified consensus mechanism.
	 * @param tx The unordered transaction to dispatch to Blockmess
	 * @param consensusType The type of consensus mechanism to be used to order the transaction
	 */
	// void dispatchUnorderedTransactionToBlockmess(HyFlexChainTransaction tx, ConsensusType consensusType);

}
