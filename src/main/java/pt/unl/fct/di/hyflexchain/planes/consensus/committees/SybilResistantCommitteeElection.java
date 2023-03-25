package pt.unl.fct.di.hyflexchain.planes.consensus.committees;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusType;
import pt.unl.fct.di.hyflexchain.planes.ledgerview.views.LedgerView;

/**
 * An Interface for electing a committee of nodes with sysbil resistance.
 */
public interface SybilResistantCommitteeElection {
	
	/**
	 * Perform a commitee election procedure based on the current
	 * Ledger view, consensus mechanism and election criteria.
	 * @param view The current Ledger view
	 * @param consensusType The consensus mechanism for which the committee
	 * is being elected
	 * @param criteria Other election criteria
	 * @return The elected committee.
	 */
	Committee performCommitteeElection(LedgerView view,
		ConsensusType consensusType, CommitteeElectionCriteria criteria);

}
