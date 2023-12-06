package pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft;

import java.util.LinkedHashSet;

import org.apache.tuweni.bytes.Bytes;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.util.crypto.Crypto;

/**
 * Represents a BFT Committee
 */
public class BftCommittee extends Committee
{
	public BftCommittee(ConsensusMechanism consensusMechanism,
		BftCommitteeElectionCriteria criteria, LinkedHashSet<Address> committee)
    {
		super(generateCommitteId(committee), consensusMechanism, criteria, committee);
	}

	public BftCommitteeElectionCriteria getBftCriteria()
	{
		return (BftCommitteeElectionCriteria) this.criteria;
	}

	protected static int generateCommitteId(LinkedHashSet<Address> committee)
	{
		var md = Crypto.getSha256Digest();
		for (var address : committee) {
			address.address().update(md);
		}

		return Bytes.wrap(md.digest()).getInt(0);
	}

	@Override
	public boolean verifyCommitteeId() {
		return id == generateCommitteId(committee);
	}
}
