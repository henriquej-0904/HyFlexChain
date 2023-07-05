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
     * A map in which the key is the previous block hash
     * of the associated block
     */
    private final Map<String, HyFlexChainBlock> pendingBlocks;

    /**
     * @param lvi
     * @param ledger
     * @param pendingBlocks
     */
    public VerifiedBlockProcessor(LedgerViewInterface lvi, DataPlane ledger,
        ConsensusMechanism consensus) {
        this.consensus = consensus;
        this.lvi = lvi;
        this.ledger = ledger;
        this.pendingBlocks = HashMap.newHashMap(100);
    }

    public boolean alreadyProcessed(HyFlexChainBlock block)
    {
        return pendingBlocks.containsKey(block.header().getPrevHash()) ||
            lvi.getBlockState(block.header().getMetaHeader().getHash(), consensus).isPresent();
    }

    public void processBlock(HyFlexChainBlock block)
    {
        String hash = block.header().getMetaHeader().getHash();
        String previousHash = block.header().getPrevHash();

        // if found next block
        if (lvi.getLastBlockHash(consensus).equals(previousHash))
        {
            ledger.writeOrderedBlock(block, consensus);
            updateLedger(hash);
        }
        else
            pendingBlocks.put(previousHash, block);        
    }

    private void updateLedger(String lastBlockHash)
    {
        HyFlexChainBlock block;

        while ( (block = pendingBlocks.remove(lastBlockHash)) != null ) {
            ledger.writeOrderedBlock(block, consensus);
            lastBlockHash = block.header().getMetaHeader().getHash();
        }
    }


}
