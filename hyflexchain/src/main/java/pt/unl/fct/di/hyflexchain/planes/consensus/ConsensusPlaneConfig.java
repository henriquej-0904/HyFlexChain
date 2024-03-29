package pt.unl.fct.di.hyflexchain.planes.consensus;

import java.util.EnumMap;
import java.util.stream.Collectors;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.BftSmartDynamicCommitteeConsensus;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.BftSmartStaticCommitteeConsensus;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.config.BFT_SMaRtConfig;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.pow.PowConsensus;
import pt.unl.fct.di.hyflexchain.util.ResetInterface;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;

/**
 * This class is responsible for instantiating and
 * intialize the Consensus Plane.
 */
public class ConsensusPlaneConfig implements ResetInterface {
	
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
				createConsensusInstanceBFT();
		};
	}

	protected ConsensusInterface createConsensusInstanceBFT()
	{
		var config = new BFT_SMaRtConfig(MultiLedgerConfig.getInstance().getLedgerConfig(ConsensusMechanism.BFT_SMaRt));
		
		if (!config.dynamicCommittees())
			return new BftSmartStaticCommitteeConsensus(LedgerViewInterface.getInstance());

		System.out.println("Init BFT dynamic committee consensus");
		return new BftSmartDynamicCommitteeConsensus(LedgerViewInterface.getInstance());
	}

	@Override
	public void reset() {
		for (var c : this.consensusInterfaces.values()) {
			((ResetInterface)c).reset();
		}
	}

}
