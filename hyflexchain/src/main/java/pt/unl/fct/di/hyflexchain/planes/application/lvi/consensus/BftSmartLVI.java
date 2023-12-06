package pt.unl.fct.di.hyflexchain.planes.application.lvi.consensus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import org.apache.commons.collections4.map.LinkedMap;
import org.apache.tuweni.bytes.Bytes;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeId;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft.BftCommittee;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.config.BFT_SMaRtConfig;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.election.StaticElection;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;

public class BftSmartLVI extends LedgerViewConsensusImpl {

	private final BFT_SMaRtConfig config;

	private final LinkedMap<CommitteeId, BftCommittee> previousCommittees, nextCommittees;
	
	private Optional<Entry<CommitteeId, BftCommittee>> currentCommittee;


    public BftSmartLVI() {
        super(ConsensusMechanism.BFT_SMaRt);

		this.config = new BFT_SMaRtConfig(
			MultiLedgerConfig.getInstance().getLedgerConfig(this.consensus));

		this.previousCommittees = new LinkedMap<>(10);
		this.nextCommittees = new LinkedMap<>(10);

		nextCommittees.put(CommitteeId.FIRST_COMMITTEE_ID,
			new StaticElection(config, 0).performCommitteeElection(config.getStaticElectionCriteria())
				.get());

		currentCommittee = Optional.empty();

		this.data.uponNewBftCommitteeBlock((b, c) ->
			addCommittee(
				new CommitteeId(
					c.getId(),
					b.hash()
				),
				c
			));

		if (!config.dynamicCommittees())
			return;

		if (config.dynamicCommittees() && config.electDynamicCommittees())
			return;

		// get committee samples from configuration
		loadNextCommitteeSamples();
    }

	private void loadNextCommitteeSamples()
	{
		int samples = this.config.getCommitteeSamples() - 1;

		if (samples <= 0)
			return;

		var committeeCriteria = this.config.getStaticElectionCriteria();
		for (int i = 1; i <= samples; i++)
		{
			var committee = new StaticElection(this.config, i)
				.performCommitteeElection(committeeCriteria)
				.get();

			this.nextCommittees.put(new CommitteeId(committee.getId(), Bytes.EMPTY), committee);
		}
	}

    @Override
	public void reset() {
		super.reset();

		this.previousCommittees.clear();
		this.nextCommittees.clear();
		this.nextCommittees.put(CommitteeId.FIRST_COMMITTEE_ID,
			new StaticElection(config, 0).performCommitteeElection(config.getStaticElectionCriteria())
				.get());

		this.currentCommittee = Optional.empty();

		this.data.uponNewBftCommitteeBlock((b, c) ->
			addCommittee(
				new CommitteeId(
					c.getId(),
					b.hash()
				),
				c
			));

		if (!config.dynamicCommittees())
			return;

		if (config.dynamicCommittees() && config.electDynamicCommittees())
			return;

		// get committee samples from configuration
		loadNextCommitteeSamples();
	}

	@Override
	public Optional<Entry<CommitteeId, ? extends Committee>> getActiveCommittee() {
		Object r = this.currentCommittee;
		return (Optional<Entry<CommitteeId, ? extends Committee>>) r;
	}

	public Optional<Entry<CommitteeId, BftCommittee>> getActiveBftCommittee() {
		return this.currentCommittee;
	}

	@Override
	public List<BftCommittee> getLedgerViewPreviousCommittees(int lastN) {

		synchronized(this.previousCommittees)
		{
			int high = this.previousCommittees.size() - 1;
			int low = high - lastN + 1;

			if (low < 0)
				low = 0;

			int size = high - low + 1;
			var list = new ArrayList<BftCommittee>(size);

			for (int i = high; i <= low; i--)
				list.add(this.previousCommittees.getValue(i));

			return list;
		}
	}

	public Optional<Entry<CommitteeId, BftCommittee>> getNextBftCommittee() {
		synchronized(this.nextCommittees)
		{
			if (this.nextCommittees.isEmpty())
				return Optional.empty();
			
			return Optional.of(Map.entry(this.nextCommittees.get(0), this.nextCommittees.getValue(0)));
		}
	}

	@Override
	public Optional<Entry<CommitteeId, ? extends Committee>> getNextCommittee() {
		synchronized(this.nextCommittees)
		{
			if (this.nextCommittees.isEmpty())
				return Optional.empty();
			
			return Optional.of(Map.entry(this.nextCommittees.get(0), this.nextCommittees.getValue(0)));
		}
	}

	public Optional<Entry<CommitteeId, BftCommittee>> advanceToNextCommittee()
	{
		var nextCommittee = getNextBftCommittee();

		if (nextCommittee.isEmpty())
			return Optional.empty();

		synchronized(this.previousCommittees)
		{
			synchronized(this.nextCommittees)
			{
				this.nextCommittees.remove(0);
				this.previousCommittees.put(nextCommittee.get().getKey(), nextCommittee.get().getValue());
				this.currentCommittee = nextCommittee;
				return this.currentCommittee;
			}
		}
	}

	protected synchronized void addCommittee(CommitteeId id, BftCommittee committee)
	{
		synchronized (this.nextCommittees)
		{
			this.nextCommittees.put(id, committee);
		}
	}
    
}
