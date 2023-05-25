package pt.unl.fct.di.hyflexchain.planes.consensus.pow;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeElectionCriteria;

public class PowCommittee implements Committee {

	public static final PowCommittee COMITTEE = new PowCommittee();

	private final ConsensusMechanism pow;



	/**
	 * 
	 */
	public PowCommittee() {
		this.pow = ConsensusMechanism.PoW;
	}

	@Override
	public ConsensusMechanism getConsensusMechanism() {
		return pow;
	}

	@Override
	public int size() {
		return -1;
	}

	@Override
	public CommitteeElectionCriteria getCommitteeElectionCriteria() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getCommitteeElectionCriteria'");
	}
	
}
