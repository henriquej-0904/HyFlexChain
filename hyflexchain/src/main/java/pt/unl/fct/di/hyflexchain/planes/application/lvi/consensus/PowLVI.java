package pt.unl.fct.di.hyflexchain.planes.application.lvi.consensus;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeId;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.pow.PowCommittee;

public class PowLVI extends LedgerViewConsensusImpl {

	private static final Entry<CommitteeId, Committee> COMMITTEE =
		Map.entry(CommitteeId.FIRST_COMMITTEE_ID, PowCommittee.COMITTEE);

	public PowLVI() {
		super(ConsensusMechanism.PoW);
	}

	@Override
	public Entry<CommitteeId, Committee> getActiveCommittee() {
		return COMMITTEE;
	}

	@Override
	public List<Committee> getLedgerViewPreviousCommittees(int lastN) {
		return List.of();
	}

	@Override
	public Optional<Entry<CommitteeId, ? extends Committee>> getNextCommittee() {
		return Optional.empty();
	}

	
	
}
