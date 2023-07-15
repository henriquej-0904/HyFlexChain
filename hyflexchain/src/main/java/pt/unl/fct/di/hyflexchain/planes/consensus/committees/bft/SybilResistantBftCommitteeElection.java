package pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft;

import pt.unl.fct.di.hyflexchain.planes.consensus.committees.election.SybilResistantCommitteeElection;

/**
 * An Interface for electing a BFT committee of nodes with sybil resistance.
 */
public interface SybilResistantBftCommitteeElection extends
    SybilResistantCommitteeElection<BftCommittee, BftCommitteeElectionCriteria> {
    
}
