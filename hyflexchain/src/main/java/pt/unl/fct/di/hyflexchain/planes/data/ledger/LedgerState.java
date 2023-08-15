package pt.unl.fct.di.hyflexchain.planes.data.ledger;

import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.util.crypto.HashedObject;

/**
 * Represents the State of the full Ledger
 */
public interface LedgerState extends Iterable<HashedObject<HyFlexChainBlock>>
{
	public void loadFullLedger(byte[] ledger);

	public byte[] getFullLedger();
}
