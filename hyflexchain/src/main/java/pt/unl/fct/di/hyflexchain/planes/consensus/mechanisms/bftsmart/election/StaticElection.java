package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.election;

import java.util.LinkedHashSet;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeElectionCriteria;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.SybilResistantCommitteeElection;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.config.BFT_SMaRtConfig;

/**
 * A class that implements a static election, i.e., it elects
 * always the same committee based on the consensus configuration.
 */
public final class StaticElection implements SybilResistantCommitteeElection
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
    public Committee performCommitteeElection(LedgerViewInterface lvi, CommitteeElectionCriteria criteria) {
        return new Committee(CONSENSUS_MECHANISM, criteria,
            new LinkedHashSet<>(this.config.getStaticCommitteeAddresses()));
    }
    
}
