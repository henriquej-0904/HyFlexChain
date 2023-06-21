package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.pow;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import applicationInterface.ApplicationInterface;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.DataPlane;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockBody;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockHeader;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockMetaHeader;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.TransactionManagement;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;

public class PowConsensus extends ConsensusInterface
{
	protected static final Logger LOG = LoggerFactory.getLogger(PowConsensus.class);

    private static final byte[] TRUE = new byte[] {1};
	private static final byte[] FALSE = new byte[] {0};

	private static final int DIFF_TARGET = 0;
	private static final String[] VALIDATORS = new String[0];
	private static final String COMMITTEE_ID = "";
	private static final String COMMITTEE_BLOCK_HASH = "";

	protected static final ConsensusMechanism POW = ConsensusMechanism.PoW;

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

    public PowConsensus(LedgerViewInterface lvi)
	{
		super(ConsensusMechanism.PoW, lvi);
	}

	@Override
	public void init() {

		var config = MultiLedgerConfig.getInstance();

		this.blockmess = new BlockmessConnector();
		
		new Thread(
			new PowConsensusThread(this, config.getLedgerConfig(this.consensus)),
			"PoW-Consensus-Thread")
		.start();
	}

	@Override
	public void orderBlock(HyFlexChainBlock block)
	{
		LOG.info("Order block: " + block.header().getMetaHeader().getHash());

		try {
			byte[] requestBytes = Utils.json.writeValueAsBytes(block);
			blockmess.invokeAsyncOperation(requestBytes,
			(reply) -> {

					boolean res = Arrays.equals(TRUE, reply.getLeft());

					Map<String, Boolean> mapTxRes =
						block.body().getTransactions().keySet().stream()
						.collect(Collectors.toUnmodifiableMap(
							(tx) -> tx, (tx) -> res)
						);

					try {
						TransactionManagement.getInstance().getTxPool(POW)
							.removePendingTxsAndNotify(mapTxRes);
					} catch (InvalidTransactionException e) {
						LOG.info(e.getMessage());
					}

					// LOG.info("blockmess reply: {}", reply);
			});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public HyFlexChainBlock createBlock(LinkedHashMap<String, HyFlexChainTransaction> txs) {
		BlockBody body = BlockBody.from(txs);
		BlockMetaHeader metaHeader = new BlockMetaHeader(ConsensusMechanism.PoW, DIFF_TARGET, VALIDATORS,
				COMMITTEE_ID, COMMITTEE_BLOCK_HASH);

		BlockHeader header = BlockHeader.create(metaHeader, lvi.getLastBlockHash(POW),
				body.getMerkleTree().getRoot().hash(),
				lvi.getBlockchainSize(POW) + 1);

		HyFlexChainBlock block = new HyFlexChainBlock(header, body);
		block.calcAndSetHash();

		return block;
	}

	@Override
	protected boolean verifyMetaHeader(BlockMetaHeader metaHeader)
	{
		return super.verifyMetaHeader(metaHeader) &&
			metaHeader.getDifficultyTarget() == DIFF_TARGET &&
			Arrays.equals(VALIDATORS, metaHeader.getValidators()) &&
			metaHeader.getCommitteeId().equalsIgnoreCase(COMMITTEE_ID) &&
			metaHeader.getCommitteeBlockHash().equalsIgnoreCase(COMMITTEE_BLOCK_HASH);
	}
	

	public class BlockmessConnector extends ApplicationInterface
    {
		protected static final Logger LOG = LoggerFactory.getLogger(BlockmessConnector.class);

        public BlockmessConnector() {
            super(defaultBlockmessProperties());
        }

        public BlockmessConnector(String[] blockmessProperties) {
            super(concat(defaultBlockmessProperties(), blockmessProperties));
        }

        public static String[] defaultBlockmessProperties()
        {
			var config = MultiLedgerConfig.getInstance().getLedgerConfig(ConsensusMechanism.PoW);
			
			var port = config.getConfigValue("BLOCKMESS_PORT");
			if (port == null)
				throw new Error("BLOCKMESS_PORT not defined.");

			var address = config.getConfigValue("BLOCKMESS_ADDRESS");
			if (address == null)
				throw new Error("BLOCKMESS_ADDRESS not defined.");

			var contact = config.getConfigValue("BLOCKMESS_CONTACT");
			if (contact == null)
					throw new Error("BLOCKMESS_CONTACT not defined.");

            var blockmessConfig = new String[]
            {
				"contact=" + contact,
                "port=" + port,
				"address=" + address,
                // "redirectFile=blockmess-logs/pow.log",
                "genesisUUID=" + UUID.randomUUID()
            };

			LOG.info("Blockmess POW config: " + Arrays.toString(blockmessConfig));

			return blockmessConfig;
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

			// LOG.info("Blockmess processOperation");

            try {
    
				HyFlexChainBlock block = Utils.json.readValue(operation, HyFlexChainBlock.class);
				
				if (!verifyBlock(block))
				{
					LOG.info("Invalid block: " + block.header().getMetaHeader().getHash());
					return FALSE;
				}

				// process new valid block
				DataPlane.getInstance().writeOrderedBlock(block, POW);
    
				LOG.info("Appended valid block with size=" + operation.length + " & hash: " +
					block.header().getMetaHeader().getHash());

				return TRUE;
                
            } catch (Exception e) {
                Utils.logError(e, LOG);
                return null;
            }
        }
        
    }
}

