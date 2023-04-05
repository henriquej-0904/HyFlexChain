package pt.unl.fct.di.hyflexchain.planes.consensus.committees;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusType;

/**
 * A committee of nodes with the purpose of executing an instance of
 * a specific consensus mechanism.
 */
public interface Committee
{
	/**
	 * The Consensus mechanism for which this committee was elected.
	 * @return The Consensus mechanism
	 */
	ConsensusType getConsensusMechanism();

	/**
	 * The cardinality of this committee.
	 * @return Committee size
	 */
	int size();

	// Set<Nodes> getCommittee();

	/**
	 * The election criteria used for this committee.
	 * @return The election criteria
	 */
	CommitteeElectionCriteria getCommitteeElectionCriteria();
}
