package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.pow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import applicationInterface.ApplicationInterface;
import io.netty.buffer.Unpooled;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeId;
import pt.unl.fct.di.hyflexchain.planes.data.DataPlane;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockBody;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockHeader;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockMetaHeader;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.block.MerkleTree;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.SerializedTx;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.TransactionManagement;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;
import pt.unl.fct.di.hyflexchain.util.crypto.Crypto;
import pt.unl.fct.di.hyflexchain.util.crypto.HashedObject;
import pt.unl.fct.di.hyflexchain.util.crypto.HyFlexChainSignature;

public class PowConsensus extends ConsensusInterface
{
	protected static final Logger LOG = LoggerFactory.getLogger(PowConsensus.class);

    private static final byte[] TRUE = new byte[] {1};
	private static final byte[] FALSE = new byte[] {0};

	private static final int DIFF_TARGET = 0;
	private static final HyFlexChainSignature[] VALIDATORS = new HyFlexChainSignature[0];
	private static final CommitteeId COMMITTEE_ID = PowCommittee.COMMITTEE_ID;

	protected static final ConsensusMechanism POW = ConsensusMechanism.PoW;

	protected static final BlockMetaHeader PROPOSAL_BLOCK_META_HEADER =
		new BlockMetaHeader(POW, DIFF_TARGET, VALIDATORS, COMMITTEE_ID);

	protected static final BlockMetaHeader BLOCK_META_HEADER =
		new BlockMetaHeader(POW, DIFF_TARGET, VALIDATORS, COMMITTEE_ID);

	protected static final byte[] PROPOSAL_BLOCK_META_HEADER_SERIALIZED =
		new byte[PROPOSAL_BLOCK_META_HEADER.serializedSize()];

	protected static final byte[] BLOCK_META_HEADER_SERIALIZED = PROPOSAL_BLOCK_META_HEADER_SERIALIZED;

	static {
		try {
			BlockMetaHeader.SERIALIZER.serialize(PROPOSAL_BLOCK_META_HEADER,
				Unpooled.wrappedBuffer(PROPOSAL_BLOCK_META_HEADER_SERIALIZED).setIndex(0, 0));
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	public void reset() {}

	/* @Override
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

					TransactionManagement.getInstance().getTxPool(POW)
						.removePendingTxsAndNotify(mapTxRes);

					// LOG.info("blockmess reply: {}", reply);
			});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	} */

	// @Override
	// public void orderTxs(BlockBody txs)
	// {
	// 	LOG.info("Order block body -> merkle tree: " + txs.getMerkleTree().getRoot().hash());

	// 	try {
	// 		byte[] requestBytes = Utils.json.writeValueAsBytes(txs);
	// 		blockmess.invokeAsyncOperation(requestBytes,
	// 		(reply) -> {

	// 				boolean res = Arrays.equals(TRUE, reply.getLeft());

	// 				Map<String, Boolean> mapTxRes =
	// 					txs.findTransactions().keySet().stream()
	// 					.collect(Collectors.toUnmodifiableMap(
	// 						(tx) -> tx, (tx) -> res)
	// 					);

	// 				TransactionManagement.getInstance().getTxPool(POW)
	// 					.removePendingTxsAndNotify(mapTxRes);

	// 				// LOG.info("blockmess reply: {}", reply);
	// 		});
	// 	} catch (JsonProcessingException e) {
	// 		e.printStackTrace();
	// 	}
	// }

	@Override
	public void orderTxs(Collection<SerializedTx> txs) {

		List<Bytes> txHashes = txs.stream()
			.map(SerializedTx::hash)
			.map(Bytes::wrap)
			.collect(Collectors.toCollection(() -> new ArrayList<>(txs.size())));
		
		MerkleTree merkleTree = MerkleTree.createMerkleTree(txHashes);

		LOG.info("Propose block -> merkle tree: " + merkleTree.getMerkleRootHash());

		byte[] requestBytes = createSerializedBlockProposal(txs, merkleTree);

		final long before = System.currentTimeMillis();

		blockmess.invokeAsyncOperation(requestBytes,
				(reply) -> {
					final long after = System.currentTimeMillis();

					boolean res = Arrays.equals(TRUE, reply.getLeft());

					TransactionManagement.getInstance().getTxPool(POW)
							.removePendingTxsAndNotify(txHashes, res);

					if (res)
						LOG.info("[{}] Block latency (ms): {}", consensus, after - before);
				});
	}

	protected byte[] createSerializedBlockProposal(Collection<SerializedTx> txs, MerkleTree merkleTree)
	{
		var header = BlockHeader.create(PROPOSAL_BLOCK_META_HEADER, Bytes.EMPTY, merkleTree.getMerkleRootHash(), 0);

		int headerSize = header.serializedSize(PROPOSAL_BLOCK_META_HEADER_SERIALIZED.length);
		int bodySize = BlockBody.serializedSize(txs);

		int size = HyFlexChainBlock.serializedSize(headerSize, bodySize);
		
		byte[] block = new byte[size];
		var buff = Unpooled.wrappedBuffer(block).setIndex(0, 0);

		buff.writeInt(headerSize + bodySize);
		buff.writeBytes(PROPOSAL_BLOCK_META_HEADER_SERIALIZED);
		try {
			BlockHeader.SERIALIZER.serializeAllButMetaHeader(header, buff);
			BlockBody.SERIALIZER.serialize(txs, buff);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return block;
	}

	protected HashedObject<HyFlexChainBlock> createBlock(final BlockBody body,
		final MerkleTree merkleTree, final Bytes serializedBody) throws IOException {

		final BlockHeader header = BlockHeader.create(BLOCK_META_HEADER, lvi.getLastBlockHash(POW),
				merkleTree.getMerkleRootHash(), lvi.getBlockchainSize(POW) + 1);

		final int headerSizeWithoutMetaHeader = header.serializedSize(0);

		final HyFlexChainBlock block = new HyFlexChainBlock(
			BLOCK_META_HEADER_SERIALIZED.length + headerSizeWithoutMetaHeader + serializedBody.size(),
			header, body);

		// hash block
		final var md = Crypto.getSha256Digest();

		// hash block size
		md.update(Utils.toBytes(block.size()));

		// since block meta header is always the same
		md.update(BLOCK_META_HEADER_SERIALIZED);

		// serialize and hash header
		// size = bytes only from header excluding meta header
		final byte[] headerBytes = new byte[headerSizeWithoutMetaHeader];
		BlockHeader.SERIALIZER.serializeAllButMetaHeader(header, Unpooled.wrappedBuffer(headerBytes).setIndex(0, 0));
		md.update(headerBytes);

		// hash body
		serializedBody.update(md);

		return new HashedObject<>(Bytes.wrap(md.digest()), block);
	}

	@Override
	protected boolean verifyMetaHeader(HyFlexChainBlock block)
	{
		var metaHeader = block.header().getMetaHeader();
		return super.verifyMetaHeader(block) &&
			metaHeader.getDifficultyTarget() == DIFF_TARGET &&
			Arrays.equals(VALIDATORS, metaHeader.getValidators()) &&
			metaHeader.getCommitteeId().equals(COMMITTEE_ID);
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

        /* @Override
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
        } */

		// @Override
        // public byte[] processOperation(byte[] operation) {

		// 	// LOG.info("Blockmess processOperation");

        //     try {
    
		// 		BlockBody blockBody = Utils.json.readValue(operation, BlockBody.class);
				
		// 		if (!verifyBody(blockBody))
		// 		{
		// 			LOG.info("Invalid block body -> merkle tree: " + blockBody.getMerkleTree().getRoot().hash());
		// 			return FALSE;
		// 		}

		// 		// process new valid block
		// 		var block = createBlock(blockBody);
		// 		DataPlane.getInstance().writeOrderedBlock(block, POW);
    
		// 		LOG.info("Appended valid block with size=" + operation.length + " & hash: " +
		// 			block.header().getMetaHeader().getHash());

		// 		return TRUE;
                
        //     } catch (Exception e) {
        //         Utils.logError(e, LOG);
        //         return null;
        //     }
        // }

		@Override
        public byte[] processOperation(byte[] operation) {

			// LOG.info("Blockmess processOperation");

            try {
    
				var buff = Unpooled.wrappedBuffer(operation);

				// discard block size bytes
				buff.skipBytes(Integer.BYTES);

				BlockHeader header = BlockHeader.SERIALIZER.deserialize(buff);

				int bodyStartIndex = buff.readerIndex();

				BlockBody blockBody = BlockBody.SERIALIZER.deserialize(buff);

				// corrupted msg
				if (buff.readerIndex() != operation.length)
				{
					LOG.info("Received corrupted block proposal: unknown {} bytes at the end.",
						operation.length - buff.readerIndex());
					return FALSE;
				}
				
				if (!verifyBody(blockBody))
				{
					LOG.info("Invalid block body");
					return FALSE;
				}

				MerkleTree merkleTree = MerkleTree.createMerkleTree(blockBody.findTransactions().keySet());

				if (!merkleTree.getMerkleRootHash().equals(header.getMerkleRoot()))
				{
					LOG.info("Invalid block merkle root");
					return FALSE;
				}

				// process new valid block
				var block = createBlock(blockBody, merkleTree,
					Bytes.wrap(operation, bodyStartIndex, operation.length - bodyStartIndex));

				TransactionManagement.getInstance().executeTransactions(
					block.obj().body().findTransactions().values());

				DataPlane.getInstance().writeOrderedBlock(block, POW);
    
				LOG.info("[{}] Appended valid block with size={} & hash={}", consensus,
					block.obj().serializedSize(), block.hash());

				return TRUE;
                
            } catch (Exception e) {
                Utils.logError(e, LOG);
                return FALSE;
            }
        }
        
    }
}

