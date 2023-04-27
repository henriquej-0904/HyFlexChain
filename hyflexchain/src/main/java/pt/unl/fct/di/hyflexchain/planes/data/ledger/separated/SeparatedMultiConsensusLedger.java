package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated;

import java.util.EnumMap;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusType;
import pt.unl.fct.di.hyflexchain.planes.data.DataPlane;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.inmemory.InMemoryLedger;
import pt.unl.fct.di.hyflexchain.util.config.LedgerConfig;

/**
 * A Multi Consensus Ledger, i.e. there is a separate ledger
 * for different consensus mechanisms. So, the state of
 * those multiple ledger instances is never merged.
 */
public class SeparatedMultiConsensusLedger implements DataPlane
{
	protected final LedgerConfig configs;

	protected final EnumMap<ConsensusType, ConsensusSpecificLedger> ledgerByConsensus;

	/**
	 * Create a new instance of the ledger
	 * @param ledgerConfig The configuration of the ledger.
	 */
	protected SeparatedMultiConsensusLedger(LedgerConfig ledgerConfig)
	{
		this.configs = ledgerConfig;
		this.ledgerByConsensus = initLedgers();
	}

	/**
	 * Init all specific ledgers.
	 * @return A map with all specific ledgers.
	 */
	protected EnumMap<ConsensusType, ConsensusSpecificLedger> initLedgers()
	{
		var ledgerType = LedgerConfig.GENERAL_CONFIG
			.LEDGER_DB_TYPE.getLedgerDbTypeValue();

		EnumMap<ConsensusType, ConsensusSpecificLedger> res = new EnumMap<>(ConsensusType.class);

		switch (ledgerType) {
			case IN_MEMORY:
				for (ConsensusType c : ConsensusType.values()) {
					res.put(c, new InMemoryLedger(c));
				}
				break;
		}

		return res;
	}

	@Override
	public LedgerConfig getLedgerConfig() {
		return this.configs;
	}

	@Override
	public void writeOrderedBlock(HyFlexChainBlock block, ConsensusType consensusType) {
		this.ledgerByConsensus.get(consensusType).writeOrderedBlock(block);
	}
	
}
