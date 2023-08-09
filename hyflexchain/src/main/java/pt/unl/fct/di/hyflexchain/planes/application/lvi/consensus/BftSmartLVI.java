package pt.unl.fct.di.hyflexchain.planes.application.lvi.consensus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import org.apache.commons.collections4.map.LinkedMap;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeId;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft.BftCommittee;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.config.BFT_SMaRtConfig;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.election.StaticElection;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;

public class BftSmartLVI extends LedgerViewConsensusImpl {

	private final BFT_SMaRtConfig config;

	private final LinkedMap<CommitteeId, BftCommittee> committees;
	
	private volatile Entry<CommitteeId, BftCommittee> currentCommittee;


    public BftSmartLVI() {
        super(ConsensusMechanism.BFT_SMaRt);

		this.config = new BFT_SMaRtConfig(
			MultiLedgerConfig.getInstance().getLedgerConfig(this.consensus));

		this.committees = new LinkedMap<>(10);

		committees.put(CommitteeId.FIRST_COMMITTEE_ID,
			new StaticElection(config).performCommitteeElection(config.getStaticElectionCriteria())
				.get());

		currentCommittee = null;

		this.data.uponNewBftCommitteeBlock((b, c) ->
			addCommittee(
				new CommitteeId(
					c.getId(),
					b.header().getMetaHeader().getHash()
				),
				c
			));
    }

    @Override
	public Entry<CommitteeId, BftCommittee> getActiveCommittee() {
		return this.currentCommittee;
	}

	@Override
	public synchronized List<BftCommittee> getLedgerViewPreviousCommittees(int lastN) {
		int high = this.committees.size() - 1;
		int low = high - lastN + 1;

		if (low < 0)
			low = 0;

		int size = high - low + 1;
		var list = new ArrayList<BftCommittee>(size);

		for (int i = high; i <= low; i--)
			list.add(this.committees.getValue(i));

		return list;
	}

	@Override
	public synchronized Optional<Entry<CommitteeId, ? extends Committee>> getNextCommittee() {
		if (this.currentCommittee == null)
			return Optional.of(this.committees.entrySet().iterator().next());

		var nextKey = this.committees.nextKey(this.currentCommittee.getKey());
		return Optional.ofNullable(
			nextKey == null ? null :
				Map.entry(nextKey, this.committees.get(nextKey))
		);
	}

	public Optional<Entry<CommitteeId, BftCommittee>> advanceToNextCommittee()
	{
		var nextCommittee = getNextCommittee();

		if (nextCommittee.isEmpty())
			return Optional.empty();

		this.currentCommittee = (Entry<CommitteeId, BftCommittee>) nextCommittee.get();
		return Optional.of(this.currentCommittee);
	}

	protected synchronized void addCommittee(CommitteeId id, BftCommittee committee)
	{
		this.committees.put(id, committee);
	}
    
}
