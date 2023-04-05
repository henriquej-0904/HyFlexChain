package pt.unl.fct.di.hyflexchain.planes.ledgerview;

import pt.unl.fct.di.hyflexchain.planes.ledgerview.views.HistoryPreviousCommittees;
import pt.unl.fct.di.hyflexchain.planes.ledgerview.views.UTXOset;

/**
 * The ledger view plane.
 */
public interface LedgerViewPlane {
	
	/**
	 * Get a ledger view of the UTXO set.
	 * @return UTXO set
	 */
	UTXOset getLedgerViewUTXOset();

	/**
	 * Get a ledger view of previous committees.
	 * @return Previous Committees.
	 */
	HistoryPreviousCommittees getLedgerViewPreviousCommittees();

}
