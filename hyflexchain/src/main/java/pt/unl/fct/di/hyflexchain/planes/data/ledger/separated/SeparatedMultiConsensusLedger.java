package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated;

import pt.unl.fct.di.hyflexchain.planes.data.DataPlane;
import pt.unl.fct.di.hyflexchain.planes.data.LedgerConfig;

/**
 * A Multi Consensus Ledger, i.e. there is a separate ledger
 * for different consensus mechanisms. So, the state of
 * those multiple ledger instances is never merged.
 */
public abstract class SeparatedMultiConsensusLedger implements DataPlane
{
	protected final LedgerConfig configs;

	/**
	 * Create a new instance of the ledger
	 * @param ledgerConfig The configuration of the ledger.
	 */
	protected SeparatedMultiConsensusLedger(LedgerConfig ledgerConfig)
	{
		this.configs = ledgerConfig;
	}

	@Override
	public LedgerConfig getLedgerParams() {
		return this.configs;
	}
	
}
