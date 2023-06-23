package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.pow;

import java.util.LinkedHashSet;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;

public class PowCommittee extends Committee {

	public static final PowCommittee COMITTEE = new PowCommittee();

	/**
	 * 
	 */
	public PowCommittee() {
		super();
		this.consensusMechanism = ConsensusMechanism.PoW;
		this.committee = LinkedHashSet.newLinkedHashSet(1);
	}

	@Override
	public int size() {
		return -1;
	}
	
}
