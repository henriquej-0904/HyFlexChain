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
public abstract class Committee
{
	protected int id;

	protected ConsensusMechanism consensusMechanism;

	protected CommitteeElectionCriteria criteria;

    protected LinkedHashSet<Address> committee;

	/**
	 * 
	 */
	protected Committee() {
	}

	/**
	 * 
	 * @param consensusMechanism
	 * @param criteria
	 * @param committee
	 */
	protected Committee(int id, ConsensusMechanism consensusMechanism,
		CommitteeElectionCriteria criteria, LinkedHashSet<Address> committee) {
		this.consensusMechanism = consensusMechanism;
		this.criteria = criteria;
		this.committee = committee;
		this.id = id;
	}

	/**
	 * Verifies the id of this committee.
	 * @return true if valid.
	 */
	public abstract boolean verifyCommitteeId();

	/**
	 * The Consensus mechanism for which this committee was elected.
	 * @return The Consensus mechanism
	 */
	public ConsensusMechanism consensusMechanism()
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

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the criteria
	 */
	public CommitteeElectionCriteria getCriteria() {
		return criteria;
	}

	/**
	 * @return the committee
	 */
	public LinkedHashSet<Address> getCommittee() {
		return committee;
	}

	public Map<Address, PublicKey> addressesToPublicKeys() throws InvalidAddressException
	{
		Map<Address, PublicKey> map = new HashMap<>(this.committee.size());

		for (Address address : this.committee) {
			map.put(address, address.readPublicKey());
		}
		
		return map;
	}

	public int getReplicaId(Address address)
	{
		var it = this.committee.iterator();
		for (int i = 0; it.hasNext(); i++) {
			if (address.equals(it.next()))
				return i;
		}

		return -1;
	}
}
