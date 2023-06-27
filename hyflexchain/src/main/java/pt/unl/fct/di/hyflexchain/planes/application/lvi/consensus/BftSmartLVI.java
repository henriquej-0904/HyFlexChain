package pt.unl.fct.di.hyflexchain.planes.application.lvi.consensus;

import java.util.List;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;

public class BftSmartLVI extends LedgerViewConsensusImpl {

    public BftSmartLVI() {
        super(ConsensusMechanism.BFT_SMaRt);
    }

    @Override
	public Committee getActiveCommittee() {
		throw new UnsupportedOperationException("Unimplemented method 'getActiveCommittee'");
	}

	@Override
	public List<Committee> getLedgerViewPreviousCommittees(int lastN) {
		throw new UnsupportedOperationException("Unimplemented method 'getLedgerViewPreviousCommittees'");
	}
    
}
