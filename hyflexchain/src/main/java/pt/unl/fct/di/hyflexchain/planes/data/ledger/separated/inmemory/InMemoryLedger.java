package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.inmemory;

import java.util.Properties;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusType;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.SeparatedLedger;

/**
 * An implementation of the Separated Ledger using
 * an in memory approach
 */
public class InMemoryLedger implements SeparatedLedger {

	/**
	 * The configuration of the ledger.
	 */
	protected final Properties config;

	/**
	 * The consensus mechanism used by this ledger.
	 */
	protected final ConsensusType consensus;

	/**
	 * Create a new instance of the ledger for a specific consensus
	 * mechanism
	 * @param configonfig The configuration of the ledger.
	 * @param consensus The consensus mechanism used by this ledger.
	 */
	public InMemoryLedger(Properties config, ConsensusType consensus) {
		this.config = config;
		this.consensus = consensus;
	}

	@Override
	public ConsensusType getConsensusMechanism() {
		return this.consensus;
	}

	@Override
	public void writeOrderedBlock(HyFlexChainBlock block) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'writeOrderedBlock'");
	}

	

	
	
}
