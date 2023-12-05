package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.election;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.BlockFilter;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.consensus.BftSmartLVI;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft.BftCommittee;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft.BftCommitteeElectionCriteria;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft.SybilResistantBftCommitteeElection;
import pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.config.BFT_SMaRtConfig;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.util.crypto.HashedObject;

/**
 * An implementation of Sybil-resistant committee election for BFT consensus
 * based on the recipient nodes of the last X finalized blocks.
 * For the election of committees, any node can propose a block of future committees
 * elected with random properties over the recipient nodes of the last X finalized blocks.
 * The other nodes can verify if the proposal is valid by creating their view
 * of the electoral base and verifying if the proposed nodes are contaneid in it.
 */
public class RecipientNodesBasedCommitteeElection implements SybilResistantBftCommitteeElection
{
    private static final ConsensusMechanism CONSENSUS_MECHANISM = ConsensusMechanism.BFT_SMaRt;

    private final BFT_SMaRtConfig config;

    /**
     * Get the last N finalized blocks.
     */
    private final IntFunction<List<HashedObject<HyFlexChainBlock>>> getLastFinalizedBlocks;

    /**
     * Get the last N previous committees.
     */
    private final IntFunction<List<BftCommittee>> getPreviousCommittees;

    private final int lastBlocks;

    private final int nPreviousCommittees;

    /**
     * @param config
     */
    public RecipientNodesBasedCommitteeElection(LedgerViewInterface lvi, BFT_SMaRtConfig config) {
        this.config = config;

        BftSmartLVI lviBftSmart = (BftSmartLVI) lvi.getLVI(CONSENSUS_MECHANISM);

        this.lastBlocks = this.config.getCommitteeElectionParamRecipientNodesOfLastFinalizedBlocks();

        this.getLastFinalizedBlocks = (nBlocks) -> lviBftSmart.getBlocks(
            BlockFilter.fromFilter(BlockFilter.Type.LAST_N, nBlocks));

        this.nPreviousCommittees = this.config.getCommitteeElectionParamPreviousCommittees();
        this.getPreviousCommittees = (n) -> lviBftSmart.getLedgerViewPreviousCommittees(n);
    }

    @Override
    public ConsensusMechanism consensus() {
        return CONSENSUS_MECHANISM;
    }

    @Override
    public Optional<BftCommittee> performCommitteeElection(BftCommitteeElectionCriteria criteria) {
        var lastBlocks = this.getLastFinalizedBlocks.apply(this.lastBlocks);
        var previousCommittees = this.getPreviousCommittees.apply(this.nPreviousCommittees);
        return performCommitteeElection(
            criteria,
            createElectoralBase(lastBlocks),
            getNodesPreviousCommittees(previousCommittees)
        );
    }

    /* @Override
    public Optional<BftCommittee[]> performCommitteeElections(BftCommitteeElectionCriteria criteria, int n)
    {
        if (n <= 0)
            throw new IllegalArgumentException("Number of committees must be greater than 0");

        var lastBlocks = this.getLastFinalizedBlocks.apply(this.lastBlocks);
        var electoralBase = createElectoralBase(lastBlocks);

        var previousCommittees = this.getPreviousCommittees.apply(this.nPreviousCommittees);
        var previousCommitteesNodes = getNodesPreviousCommittees(previousCommittees);

        var electedCommittee = performCommitteeElection(criteria, electoralBase, previousCommitteesNodes);

        if (electedCommittee.isEmpty())
            return Optional.empty();

        BftCommittee[] committees = new BftCommittee[n];
        committees[0] = electedCommittee.get();

        if (n == 1)
            return Optional.of(committees);

        Queue<Committee> previousCommitteesQueue;
        
        if (previousCommittees.size() == this.nPreviousCommittees)
            previousCommitteesQueue = new ArrayDeque<>(previousCommittees);
        else
        {
            previousCommitteesQueue = new ArrayDeque<>(this.nPreviousCommittees);
            previousCommitteesQueue.addAll(previousCommittees);
        }

        for (int i = 1; i < committees.length; i++) {
            if (previousCommitteesQueue.size() == this.nPreviousCommittees)
            {
                previousCommitteesNodes.removeAll(previousCommitteesQueue.poll()
                    .getCommitteeAddresses());
            }

            previousCommitteesQueue.add(committees[i - 1]);
            previousCommitteesNodes.addAll(committees[i - 1].getCommitteeAddresses());

            electedCommittee = performCommitteeElection(criteria, electoralBase, previousCommitteesNodes);

            if (electedCommittee.isEmpty())
                return Optional.empty();

            committees[i] = electedCommittee.get();
        }

        return Optional.of(committees);
    } */


    protected Optional<BftCommittee> performCommitteeElection(BftCommitteeElectionCriteria criteria,
        Set<Address> electoralBase, Set<Address> nodesPreviousCommittees)
    {
        int committeeSize = criteria.getSize();
        
        if (electoralBase.size() < committeeSize)
            return Optional.empty();

        return electCommittee(
            criteria,
            electoralBase,
            nodesPreviousCommittees,
            committeeSize
        );
    }

    protected Set<Address> createElectoralBase(List<HashedObject<HyFlexChainBlock>> lastBlocks)
    {
        return lastBlocks.stream()
            .flatMap(b -> Stream.of(b.obj().body().getTransactions()))
            .flatMap(t -> Stream.of(t.recipientAddresses()))
            .collect(Collectors.toSet());
    }

    protected Set<Address> getNodesPreviousCommittees(Collection<BftCommittee> previousCommittees)
    {
        return previousCommittees.stream()
            .flatMap(c -> c.getCommittee().stream())
            .collect(Collectors.toSet());
    }

    protected Optional<BftCommittee> electCommittee(BftCommitteeElectionCriteria criteria,
        Set<Address> electoralBase, Collection<Address> except,
        int n)
    {
        electoralBase = new HashSet<>(electoralBase);
        electoralBase.removeAll(except);
        
        if (electoralBase.size() < n)
            return Optional.empty();

        var committee = electoralBase.stream()
            .limit(n)
            .collect(Collectors.toCollection(() -> LinkedHashSet.newLinkedHashSet(n)));
        
        return Optional.of(
            new BftCommittee(CONSENSUS_MECHANISM, criteria, committee)
        );
    }
    
}