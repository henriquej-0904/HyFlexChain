package pt.unl.fct.di.hyflexchain.planes.consensus;

import java.util.EnumMap;
import java.util.stream.Collectors;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.BftSmartConsensus;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.pow.PowConsensus;
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
		this.consensusInterfaces = create(config);
	}

	public void init()
	{
		this.consensusInterfaces.values().forEach(ConsensusInterface::init);
	}

	protected EnumMap<ConsensusMechanism, ConsensusInterface> create(MultiLedgerConfig config)
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
			case BFT_SMaRt ->
				new BftSmartConsensus(LedgerViewInterface.getInstance());
		};
	}

}
