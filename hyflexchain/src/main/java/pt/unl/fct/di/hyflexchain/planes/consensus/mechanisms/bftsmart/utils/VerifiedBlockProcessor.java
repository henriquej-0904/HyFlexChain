package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.utils;

import java.util.HashMap;
import java.util.Map;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.DataPlane;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;

/**
 * A class that implements a Data Structure to temporary
 * save verified blocks. When there are consecutive blocks
 * they are written to the ledger.
 */
public class VerifiedBlockProcessor {
    
    private final ConsensusMechanism consensus;
    
    private final LedgerViewInterface lvi;

    private final DataPlane ledger;

    /**
     * A map in which the key is the committee ID and the value is a
     * map with the previous block hash
     * of the associated block
     */
    private final Map<String, Map<String, HyFlexChainBlock>> pendingBlocks;

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

    protected Map<String, HyFlexChainBlock> getPendingBlocks(int committeeId,
        String committeeBlockHash)
    {
        return this.pendingBlocks.get(committeeBlockHash + "_" + committeeId);
    }

    protected Map<String, HyFlexChainBlock> removePendingBlocks(int committeeId,
        String committeeBlockHash)
    {
        return this.pendingBlocks.remove(committeeBlockHash + "_" + committeeId);
    }

    protected Map<String, HyFlexChainBlock> getOrCreatePendingBlocks(int committeeId,
        String committeeBlockHash)
    {
        return this.pendingBlocks.computeIfAbsent(committeeBlockHash + "_" + committeeId,
            (k) -> HashMap.newHashMap(20));
    }

    public boolean alreadyProcessed(HyFlexChainBlock block)
    {
        var pendingBlocksForCommittee = getPendingBlocks(block.header().getMetaHeader().getCommitteeId(),
            block.header().getMetaHeader().getCommitteeBlockHash());

        if (pendingBlocksForCommittee == null)
            return lvi.getBlockState(block.header().getMetaHeader().getHash(), consensus).isPresent();
        
        return pendingBlocksForCommittee.containsKey(block.header().getPrevHash())
            || lvi.getBlockState(block.header().getMetaHeader().getHash(), consensus).isPresent();
    }

    public void processBlock(HyFlexChainBlock block)
    {
        final String hash = block.header().getMetaHeader().getHash();
        final String previousHash = block.header().getPrevHash();

        final int committeeId = block.header().getMetaHeader().getCommitteeId();
        final String committeeBlockHash = block.header().getMetaHeader().getCommitteeBlockHash();
        final var pendingBlocksForCommittee = getOrCreatePendingBlocks(committeeId, committeeBlockHash);

        // if found next block
        if (lvi.getLastBlockHash(consensus).equals(previousHash))
        {
            ledger.writeOrderedBlock(block, consensus);
            updateLedger(pendingBlocksForCommittee, hash);
        }
        else
            pendingBlocksForCommittee.put(previousHash, block);
    }

    public void processLastBlock(HyFlexChainBlock block)
    {
        processBlock(block);

        final int committeeId = block.header().getMetaHeader().getCommitteeId();
        final String committeeBlockHash = block.header().getMetaHeader().getCommitteeBlockHash();
        final var pendingBlocksForCommittee = getPendingBlocks(committeeId, committeeBlockHash);

        if (pendingBlocksForCommittee.isEmpty())
            removePendingBlocks(committeeId, committeeBlockHash);
    }

    private void updateLedger(Map<String, HyFlexChainBlock> pendingBlocks, String lastBlockHash)
    {
        HyFlexChainBlock block;

        while ( (block = pendingBlocks.remove(lastBlockHash)) != null ) {
            ledger.writeOrderedBlock(block, consensus);
            lastBlockHash = block.header().getMetaHeader().getHash();
        }
    }


}
