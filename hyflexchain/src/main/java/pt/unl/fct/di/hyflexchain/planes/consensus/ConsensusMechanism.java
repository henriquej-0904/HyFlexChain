package pt.unl.fct.di.hyflexchain.planes.consensus;

import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The type of supported Consensus mechanisms.
 */
public enum ConsensusMechanism
{
	/**
	 * Proof of Work
	 */
	PoW ("pow"),

	/**
	 * BFT-SMaRt
	 */
	BFT_SMaRt ("bft_smart");

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

	public static ConsensusMechanism parse(String value)
	{
		final var valToComp = value.toLowerCase();
		var res = Stream.of(ConsensusMechanism.values())
			.filter((c) -> c.getConsensus().equals(valToComp))
			.findFirst();

		if (!res.isPresent())
			throw new IllegalArgumentException("Invalid consensus mechanism: " + value);

		return res.get();
	}

	
}
