package pt.unl.fct.di.hyflexchain.planes.consensus.committees;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.InvalidAddressException;

/**
 * A committee of nodes with the purpose of executing an instance of
 * a specific consensus mechanism.
 */
public class Committee
{
	protected ConsensusMechanism consensusMechanism;

	protected CommitteeElectionCriteria criteria;

    protected LinkedHashSet<Address> committee;

	/**
	 * 
	 */
	public Committee() {
	}

	/**
	 * 
	 * @param consensusMechanism
	 * @param criteria
	 * @param committee
	 */
	public Committee(ConsensusMechanism consensusMechanism,
		CommitteeElectionCriteria criteria, LinkedHashSet<Address> committee) {
		this.consensusMechanism = consensusMechanism;
		this.criteria = criteria;
		this.committee = committee;
	}

	/**
	 * The Consensus mechanism for which this committee was elected.
	 * @return The Consensus mechanism
	 */
	public ConsensusMechanism getConsensusMechanism()
	{
		return this.consensusMechanism;
	}

	/**
	 * The cardinality of this committee.
	 * @return Committee size
	 */
	public int size()
	{
		return this.committee.size();
	}

	public Map<Address, PublicKey> addressesToPublicKeys() throws InvalidAddressException
	{
		Map<Address, PublicKey> map = new HashMap<>(this.committee.size());

		for (Address address : this.committee) {
			map.put(address, address.readPublicKey());
		}
		
		return map;
	}

	/**
	 * The addresses of the nodes in the committee.
	 * @return Committee addresses.
	 */
	public LinkedHashSet<Address> getCommitteeAddresses()
	{
		return this.committee;
	}

	/**
	 * The election criteria used for this committee.
	 * @return The election criteria
	 */
	public CommitteeElectionCriteria getCommitteeElectionCriteria()
	{
		return this.criteria;
	}

	/**
	 * @param consensusMechanism the consensusMechanism to set
	 */
	public void setConsensusMechanism(ConsensusMechanism consensusMechanism) {
		this.consensusMechanism = consensusMechanism;
	}

	/**
	 * @param criteria the criteria to set
	 */
	public void setCriteria(CommitteeElectionCriteria criteria) {
		this.criteria = criteria;
	}

	/**
	 * @param committee the committee to set
	 */
	public void setCommittee(LinkedHashSet<Address> committee) {
		this.committee = committee;
	}

	
}
