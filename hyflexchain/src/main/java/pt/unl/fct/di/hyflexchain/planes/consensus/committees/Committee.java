package pt.unl.fct.di.hyflexchain.planes.consensus.committees;

import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.InvalidAddressException;
import pt.unl.fct.di.hyflexchain.util.crypto.Crypto;

/**
 * A committee of nodes with the purpose of executing an instance of
 * a specific consensus mechanism.
 */
public class Committee
{
	protected int id;

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
		this.id = generateCommitteId();
	}

	protected int generateCommitteId()
	{
		var serializer = Address.SERIALIZER;
		ByteBuffer buff = null;
		int size;
		
		var digest = Crypto.getSha256Digest();

		for (Address address : committee) {
			if ((buff == null ? -1 : buff.capacity()) < (size = serializer.serializedSize(address)))
			{
				buff = ByteBuffer.allocate(size);
				serializer.serialize(address, buff);
			}
			else
				serializer.serialize(address, buff);

			int len = buff.position();
			
			digest.update(buff.array(), 0, len);
			buff.position(0);
		}

		byte[] result = digest.digest();
		buff = ByteBuffer.wrap(result);

		return buff.getInt();
	}

	public boolean verifyCommitteeId()
	{
		return this.id == generateCommitteId();
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

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
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



	
}
