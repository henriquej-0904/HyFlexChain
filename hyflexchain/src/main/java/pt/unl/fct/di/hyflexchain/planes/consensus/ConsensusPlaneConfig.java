package pt.unl.fct.di.hyflexchain.planes.consensus;

import java.util.EnumMap;
import java.util.stream.Collectors;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.pow.PowConsensus;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;

/**
 * This class is responsible for instantiating and
 * intialize the Consensus Plane.
 */
public class ConsensusPlaneConfig {
	
	protected final EnumMap<ConsensusMechanism, ConsensusInterface>
		consensusInterfaces;

	/**
	 * Create a new Consensus Plane instance and initialize it.
	 * @param config The config object
	 */
	public ConsensusPlaneConfig(MultiLedgerConfig config) {
		this.consensusInterfaces = init(config);
	}

	protected EnumMap<ConsensusMechanism, ConsensusInterface> init(MultiLedgerConfig config)
	{
		return config.getActiveConsensusMechanisms()
			.stream().map(this::createConsensusInstance)
			.collect(Collectors.toMap(
				ConsensusInterface::getConsensus,
				(ci) -> ci,
				(ci1, ci2) -> ci1,
				() -> new EnumMap<>(ConsensusMechanism.class)
			));
	}

	protected ConsensusInterface createConsensusInstance(ConsensusMechanism consensus)
	{
		return switch (consensus) {
			case PoW ->
				new PowConsensus(LedgerViewInterface.getInstance());
		};
	}

}
