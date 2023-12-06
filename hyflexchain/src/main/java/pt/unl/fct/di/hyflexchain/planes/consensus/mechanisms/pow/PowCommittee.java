package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.pow;

import java.util.LinkedHashSet;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeElectionCriteria;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeId;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeElectionCriteria.CommitteeValidity;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeElectionCriteria.RandSource;

public final class PowCommittee extends Committee {

	public static final CommitteeElectionCriteria POW_CRITERIA =
		new CommitteeElectionCriteria(0, 0, RandSource.NONE,
			CommitteeValidity.INFINITE_VALIDITY);

	public static final PowCommittee COMITTEE = new PowCommittee();

	public static final CommitteeId COMMITTEE_ID = CommitteeId.FIRST_COMMITTEE_ID;

	private PowCommittee() {
		super(0, ConsensusMechanism.PoW, POW_CRITERIA,
			LinkedHashSet.newLinkedHashSet(0));
	}

	@Override
	public boolean verifyCommitteeId() {
		return id == COMITTEE.id;
	}
	
}
