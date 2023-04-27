package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.inmemory;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewConsensusInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.ConsensusSpecificLedger;

/**
 * An implementation of the Separated Ledger using
 * an in memory approach
 */
public class InMemoryLedger implements ConsensusSpecificLedger,
	LedgerViewConsensusInterface
{

	/**
	 * The consensus mechanism used by this ledger.
	 */
	protected final ConsensusMechanism consensus;

	/**
	 * Create a new instance of the ledger for a specific consensus
	 * mechanism
	 * @param consensus The consensus mechanism used by this ledger.
	 */
	public InMemoryLedger(ConsensusMechanism consensus) {
		this.consensus = consensus;
	}

	@Override
	public ConsensusMechanism getConsensusMechanism() {
		return this.consensus;
	}

	@Override
	public void writeOrderedBlock(HyFlexChainBlock block) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'writeOrderedBlock'");
	}

	

	
	
}
