package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.tuweni.bytes.Bytes;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeId;
import pt.unl.fct.di.hyflexchain.planes.data.DataPlane;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.util.ResetInterface;
import pt.unl.fct.di.hyflexchain.util.crypto.HashedObject;

/**
 * A class that implements a Data Structure to temporary
 * save verified blocks. When there are consecutive blocks
 * they are written to the ledger.
 */
public class VerifiedBlockProcessor implements ResetInterface {
    
    private final ConsensusMechanism consensus;
    
    private final LedgerViewInterface lvi;

    private final DataPlane ledger;

    /**
     * A map in which the key is the committee ID and the value is a
     * map with the previous block hash
     * of the associated block
     */
    private final Map<CommitteeId, Map<Bytes, HashedObject<HyFlexChainBlock>>> pendingBlocks;

    /**
     * @param lvi
     * @param ledger
     * @param consensus
     */
    public VerifiedBlockProcessor(LedgerViewInterface lvi, DataPlane ledger,
        ConsensusMechanism consensus) {
        this.consensus = consensus;
        this.lvi = lvi;
        this.ledger = ledger;
        this.pendingBlocks = HashMap.newHashMap(10);
    }

    @Override
    public void reset() {
        this.pendingBlocks.clear();
    }

    protected Map<Bytes, HashedObject<HyFlexChainBlock>> getPendingBlocks(CommitteeId committeeId)
    {
        return this.pendingBlocks.get(committeeId);
    }

    protected Map<Bytes, HashedObject<HyFlexChainBlock>> removePendingBlocks(CommitteeId committeeId)
    {
        return this.pendingBlocks.remove(committeeId);
    }

    protected Map<Bytes, HashedObject<HyFlexChainBlock>> getOrCreatePendingBlocks(CommitteeId committeeId)
    {
        return this.pendingBlocks.computeIfAbsent(committeeId,
            (k) -> HashMap.newHashMap(20));
    }

    public boolean alreadyProcessed(HashedObject<HyFlexChainBlock> block)
    {
        var pendingBlocksForCommittee = getPendingBlocks(block.obj().header().getMetaHeader().getCommitteeId());

        if (pendingBlocksForCommittee == null)
            return lvi.getBlockState(block.hash(), consensus).isPresent();
        
        return pendingBlocksForCommittee.containsKey(block.obj().header().getPrevHash())
            || lvi.getBlockState(block.hash(), consensus).isPresent();
    }

    public void processBlock(HashedObject<HyFlexChainBlock> block)
    {
        final Bytes previousHash = block.obj().header().getPrevHash();
        final CommitteeId committeeId = block.obj().header().getMetaHeader().getCommitteeId();

        final var pendingBlocksForCommittee = getOrCreatePendingBlocks(committeeId);

        // if found next block
        if (lvi.getLastBlockHash(consensus).equals(previousHash))
        {
            ledger.writeOrderedBlock(block, consensus);
            updateLedger(pendingBlocksForCommittee, block.hash());
        }
        else
            pendingBlocksForCommittee.put(previousHash, block);
    }

    public void processLastBlock(HashedObject<HyFlexChainBlock> block)
    {
        processBlock(block);

        final CommitteeId committeeId = block.obj().header().getMetaHeader().getCommitteeId();

        final var pendingBlocksForCommittee = getPendingBlocks(committeeId);

        if (pendingBlocksForCommittee.isEmpty())
            removePendingBlocks(committeeId);
    }

    private void updateLedger(Map<Bytes, HashedObject<HyFlexChainBlock>> pendingBlocks, Bytes lastBlockHash)
    {
        HashedObject<HyFlexChainBlock> block;

        while ( (block = pendingBlocks.remove(lastBlockHash)) != null ) {
            ledger.writeOrderedBlock(block, consensus);
            lastBlockHash = block.hash();
        }
    }
}
