package pt.unl.fct.di.hyflexchain.planes.data;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusType;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.LedgerState;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

/**
 * Represents the Data Plane.
 */
public interface DataPlane {
	
	/**
	 * Get full ledger view.
	 * @return Full ledger view.
	 */
	LedgerState getView();

	/**
	 * Set Ledger parameters
	 * @param params
	 */
	void setLedgerParams(LedgerParams params);

	/**
	 * Get the applied Ledger parameters
	 * @return The applied Ledger parameters
	 */
	LedgerParams getLedgerParams();

	/**
	 * Write an ordered block to the Ledger.
	 * @param block The ordered block
	 * @param consensusType The type of consensus mechanism used to order the block
	 */
	void writeOrderedBlock(HyFlexChainBlock block, ConsensusType consensusType);

	/**
	 * Dispatch an unordered block to the Blockmess Layer to be ordered
	 * by the specified consensus mechanism.
	 * @param block The unordered block to dispatch to Blockmess
	 * @param consensusType The type of consensus mechanism to be used to order the block
	 */
	void dispatchUnorderedBlockToBlockmess(HyFlexChainBlock block, ConsensusType consensusType);

	/**
	 * Dispatch an unordered transaction to the Blockmess Layer to be ordered
	 * by the specified consensus mechanism.
	 * @param tx The unordered transaction to dispatch to Blockmess
	 * @param consensusType The type of consensus mechanism to be used to order the transaction
	 */
	void dispatchUnorderedTransactionToBlockmess(HyFlexChainTransaction tx, ConsensusType consensusType);

}
