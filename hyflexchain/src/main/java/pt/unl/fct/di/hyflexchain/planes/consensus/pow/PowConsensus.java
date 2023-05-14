package pt.unl.fct.di.hyflexchain.planes.consensus.pow;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

import applicationInterface.ApplicationInterface;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.DataPlane;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockBody;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockHeader;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockMetaHeader;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionState;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.TransactionManagement;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;

public class PowConsensus
{
	protected static final Logger LOG = LogManager.getLogger();

    private static final byte[] TRUE = new byte[] {1};
	private static final byte[] FALSE = new byte[] {0};

	private static final int DIFF_TARGET = 0;
	private static final String[] VALIDATORS = new String[0];
	private static final String COMMITTEE_ID = "";
	private static final String COMMITTEE_BLOCK_HASH = "";

	private static final ConsensusMechanism POW = ConsensusMechanism.PoW;

    /**
     * @return the blockmess
     */
    public BlockmessConnector getBlockmess() {
        return blockmess;
    }

    public void setBlockmess(BlockmessConnector blockmess)
    {
        this.blockmess = blockmess;
    }

	protected BlockmessConnector blockmess;

    public PowConsensus()
	{
		
	}

	public void orderBlock(HyFlexChainBlock block)
	{
		try {
			byte[] requestBytes = Utils.json.writeValueAsBytes(block);
			
			blockmess.invokeAsyncOperation(requestBytes,
			(reply) -> {
					// do nothing
			});
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public HyFlexChainBlock createBlock(LedgerViewInterface lvi,
		LinkedHashMap<String, HyFlexChainTransaction> txs) {
		BlockBody body = BlockBody.from(txs);
		BlockMetaHeader metaHeader = new BlockMetaHeader(ConsensusMechanism.PoW, DIFF_TARGET, VALIDATORS,
				COMMITTEE_ID, COMMITTEE_BLOCK_HASH);

		BlockHeader header = BlockHeader.create(metaHeader, lvi.getLastBlockHash(POW),
				body.getMerkleTree().getRoot().hash(),
				lvi.getBlockchainSize(POW) + 1);

		HyFlexChainBlock block = new HyFlexChainBlock(header, body);
		block.calcHash();

		return block;
	}

	public boolean verifyBlock(HyFlexChainBlock block)
	{
		if (!block.verifyBlock(LOG))
			return false;

		var header = block.header();
		var metaHeader = header.getMetaHeader();

		if (! verifyMetaHeader(metaHeader))
		{
			LOG.info("Invalid block meta header");
			return false;
		}

		if (! verifyHeader(header, block.body()))
		{
			LOG.info("Invalid block header");
			return false;
		}
		
		var lvi = LedgerViewInterface.getInstance();

		boolean validTxs = block.body().getTransactions().keySet().stream()
			.map((hash) -> lvi.getTransactionState(hash, POW))
			.noneMatch((s) -> s == TransactionState.FINALIZED);
		
		if (!validTxs)
		{
			LOG.info("Invalid txs - block has already finalized txs.");
			return false;
		}

		return true;
	}

	public boolean verifyMetaHeader(BlockMetaHeader metaHeader)
	{
		return metaHeader.getConsensus() == POW &&
			metaHeader.getDifficultyTarget() == DIFF_TARGET &&
			Arrays.equals(VALIDATORS, metaHeader.getValidators()) &&
			metaHeader.getCommitteeId().equalsIgnoreCase(COMMITTEE_ID) &&
			metaHeader.getCommitteeBlockHash().equalsIgnoreCase(COMMITTEE_BLOCK_HASH);
	}

	public boolean verifyHeader(BlockHeader header, BlockBody body)
	{
		var lvi = LedgerViewInterface.getInstance();
		
		return header.getPrevHash().equalsIgnoreCase(lvi.getLastBlockHash(POW))
		&& header.getNonce() == lvi.getBlockchainSize(POW) + 1
		&& header.getMerkleRoot().equalsIgnoreCase(body.getMerkleTree().getRoot().hash());
	}
	
	

	public class BlockmessConnector extends ApplicationInterface
    {

        public BlockmessConnector() {
            super(defaultBlockmessProperties());
        }

        public BlockmessConnector(String[] blockmessProperties) {
            super(concat(defaultBlockmessProperties(), blockmessProperties));
        }

        public static String[] defaultBlockmessProperties()
        {
			var config = MultiLedgerConfig.getInstance().getLedgerConfig(ConsensusMechanism.PoW);
			
			var port = config.getConfigValue("POW_BLOCKMESS_PORT");
			if (port == null)
				throw new Error("POW_BLOCKMESS_PORT not defined.");

            return new String[]
            {
                "port=" + port,
                "redirectFile=blockmess-logs/pow.log",
                "genesisUUID=" + UUID.randomUUID()
            };
        }

        protected static String[] concat(String[] array1, String[] array2)
        {
            var res = new String[array1.length + array2.length];
            System.arraycopy(array1, 0, res, 0, array1.length);
            System.arraycopy(array2, 0, res, array1.length, array2.length);
            return res;
        }

        @Override
        public byte[] processOperation(byte[] operation) {
            try {
    
				HyFlexChainBlock block = Utils.json.readValue(operation, HyFlexChainBlock.class);
				
				if (!verifyBlock(block))
				{
					LOG.info("Invalid block: " + new String(operation));
					return FALSE;
				}

				TransactionManagement.getInstance().getTxPool(POW)
					.removePendingTxsAndNotify(block.body().getTransactions().keySet());
				
				DataPlane.getInstance().writeOrderedBlock(block, POW);
    
				LOG.info("Appended valid block with hash: " +
					block.header().getMetaHeader().getHash());

				return TRUE;
                
            } catch (Exception e) {
                Utils.logError(e, LOG);
                return null;
            }
        }
        
    }
}

