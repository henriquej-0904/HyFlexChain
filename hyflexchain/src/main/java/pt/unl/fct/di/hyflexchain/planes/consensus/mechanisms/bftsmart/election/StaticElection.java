package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.election;

import java.util.LinkedHashSet;
import java.util.Optional;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft.BftCommittee;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft.BftCommitteeElectionCriteria;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft.SybilResistantBftCommitteeElection;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.config.BFT_SMaRtConfig;

/**
 * A class that implements a static election, i.e., it always elects
 * the same committee based on the consensus configuration.
 */
public final class StaticElection implements SybilResistantBftCommitteeElection
{
    private static final ConsensusMechanism CONSENSUS_MECHANISM = ConsensusMechanism.BFT_SMaRt;

    private BFT_SMaRtConfig config;

    private int committeeIndex;

    /**
     * @param config
     */
    public StaticElection(BFT_SMaRtConfig config, int committeeIndex) {
        this.config = config;
        this.committeeIndex = committeeIndex;
    }

    @Override
    public ConsensusMechanism consensus() {
        return CONSENSUS_MECHANISM;
    }

    @Override
    public Optional<BftCommittee> performCommitteeElection(BftCommitteeElectionCriteria criteria) {
        var committee = this.config.getStaticCommitteeAddresses();
        committee = committee.subList(committeeIndex * criteria.getSize(),
            (committeeIndex + 1) * criteria.getSize());

        return Optional.of(new BftCommittee(CONSENSUS_MECHANISM, criteria,
            new LinkedHashSet<>(committee)));
    }

    /* @Override
    public Optional<BftCommittee[]> performCommitteeElections(BftCommitteeElectionCriteria criteria, int n) {
        throw new UnsupportedOperationException("Unimplemented method 'performCommitteeElections'");
    } */
    
}
