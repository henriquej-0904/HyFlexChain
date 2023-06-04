package pt.unl.fct.di.hyflexchain.planes.consensus;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The type of supported Consensus mechanisms.
 */
public enum ConsensusMechanism
{
	/**
	 * Proof of Work
	 */
	PoW ("pow");

	private String consensus;

	/**
	 * @param consensus
	 */
	private ConsensusMechanism(String consensus) {
		this.consensus = consensus;
	}

	/**
	 * @return the consensus
	 */
	@JsonValue
	public String getConsensus() {
		return consensus;
	}

	@Override
	public String toString() {
		return getConsensus();
	}

	
}
