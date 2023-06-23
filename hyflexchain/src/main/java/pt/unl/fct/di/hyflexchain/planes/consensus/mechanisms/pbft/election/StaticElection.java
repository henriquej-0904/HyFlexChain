package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.pbft.election;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.LedgerView;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeElectionCriteria;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.SybilResistantCommitteeElection;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.pbft.config.BFT_SMaRtConfig;

public class StaticElection implements SybilResistantCommitteeElection
{
    protected static final ConsensusMechanism CONSENSUS_MECHANISM = ConsensusMechanism.BFT_SMaRt;

    protected BFT_SMaRtConfig config;

    /**
     * @param config
     */
    public StaticElection(BFT_SMaRtConfig config) {
        this.config = config;
    }

    @Override
    public Committee performCommitteeElection(LedgerView view, CommitteeElectionCriteria criteria) {
        //TODO: 
        return null;
    }
    
}
