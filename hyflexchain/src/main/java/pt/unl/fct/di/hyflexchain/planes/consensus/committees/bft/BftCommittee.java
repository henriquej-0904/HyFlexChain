package pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft;

import java.util.LinkedHashSet;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;

/**
 * Represents a BFT Committee
 */
public class BftCommittee extends Committee
{
	public BftCommittee(ConsensusMechanism consensusMechanism,
		BftCommitteeElectionCriteria criteria, LinkedHashSet<Address> committee)
    {
		super(consensusMechanism, criteria, committee);
	}
}
