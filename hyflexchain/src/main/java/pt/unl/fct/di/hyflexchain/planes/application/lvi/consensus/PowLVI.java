package pt.unl.fct.di.hyflexchain.planes.application.lvi.consensus;

import java.util.List;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.consensus.pow.PowCommittee;

public class PowLVI extends LedgerViewConsensusImpl {

	public PowLVI() {
		super(ConsensusMechanism.PoW);
	}

	@Override
	public Committee getActiveCommittee() {
		return PowCommittee.COMITTEE;
	}

	@Override
	public List<Committee> getLedgerViewPreviousCommittees(int lastN) {
		return List.of(PowCommittee.COMITTEE);
	}
	
}
