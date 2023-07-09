package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.election;

import java.util.LinkedHashSet;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
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

    /**
     * @param config
     */
    public StaticElection(BFT_SMaRtConfig config) {
        this.config = config;
    }

    @Override
    public ConsensusMechanism consensus() {
        return CONSENSUS_MECHANISM;
    }

    @Override
    public BftCommittee performCommitteeElection(LedgerViewInterface lvi, BftCommitteeElectionCriteria criteria) {
        return new BftCommittee(CONSENSUS_MECHANISM, criteria,
            new LinkedHashSet<>(this.config.getStaticCommitteeAddresses()));
    }
    
}
