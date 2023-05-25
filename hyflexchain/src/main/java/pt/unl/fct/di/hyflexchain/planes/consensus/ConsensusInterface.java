package pt.unl.fct.di.hyflexchain.planes.consensus;

import java.util.LinkedHashMap;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

/**
 * Represents the interface for interacting with a specific
 * consensus mechanism.
 */
public abstract class ConsensusInterface
{
	protected final ConsensusMechanism consensus;

	protected final LedgerViewInterface lvi;

	/**
	 * Create a new Consensus interface.
	 * @param consensus The consensus mechanism
	 * @param lvi The ledger view interface
	 */
	protected ConsensusInterface(ConsensusMechanism consensus, LedgerViewInterface lvi) {
		this.consensus = consensus;
		this.lvi = lvi;
	}

	/**
	 * Initialize the consensus implementation.
	 */
	public abstract void init();

	/**
	 * Order a block according to this specific
	 * consensus implementation.
	 * @param block The block to order.
	 */
	public abstract void orderBlock(HyFlexChainBlock block);

	/**
	 * Create a block for this consensus mechanism ready
	 * to be proposed for ordering.
	 * @param txs The list of transactions that will be included in the created block.
	 * @return A new created block with the specified list of transactions.
	 */
	protected abstract HyFlexChainBlock createBlock(LinkedHashMap<String, HyFlexChainTransaction> txs);

	/**
	 * Verify a block when for integrity and all necessary
	 * checks.
	 * @param block The block to verify
	 * @return true if it is valid, otherwise false.
	 */
	protected abstract boolean verifyBlock(HyFlexChainBlock block);

	/**
	 * Get the consensus mechanism of this class implementation.
	 * @return the consensus
	 */
	public ConsensusMechanism getConsensus() {
		return consensus;
	}

}
