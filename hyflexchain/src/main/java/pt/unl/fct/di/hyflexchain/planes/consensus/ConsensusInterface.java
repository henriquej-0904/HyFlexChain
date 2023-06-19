package pt.unl.fct.di.hyflexchain.planes.consensus;

import java.util.LinkedHashMap;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.application.ti.TransactionInterface;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockBody;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockHeader;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockMetaHeader;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionState;
import pt.unl.fct.di.hyflexchain.planes.execution.ExecutionPlane;
import pt.unl.fct.di.hyflexchain.planes.execution.contracts.InvalidSmartContractException;
import pt.unl.fct.di.hyflexchain.planes.execution.contracts.TransactionParamsContract.TransactionParamsContractResult;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.TransactionManagement;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;

/**
 * Represents the interface for interacting with a specific
 * consensus mechanism.
 */
public abstract class ConsensusInterface
{
	protected static final Logger LOG = LoggerFactory.getLogger(ConsensusInterface.class);

	protected final ConsensusMechanism consensus;

	protected final LedgerViewInterface lvi;

	private final Predicate<BlockBody> verifyBlockBodyPred;

	/**
	 * Create a new Consensus interface.
	 * @param consensus The consensus mechanism
	 * @param lvi The ledger view interface
	 */
	protected ConsensusInterface(ConsensusMechanism consensus, LedgerViewInterface lvi) {
		this.consensus = consensus;
		this.lvi = lvi;

		switch (MultiLedgerConfig.getInstance()
		.getSystemVersion()) {
			case V1_0:
				this.verifyBlockBodyPred = this::verifyBody1_0;
				break;
			case V2_0:
				this.verifyBlockBodyPred = this::verifyBody2_0;
				break;
			default:
				this.verifyBlockBodyPred = this::verifyBody2_0;
				break;			
		}
	}

	/**
	 * Initialize the consensus implementation.
	 */
	public abstract void init();

	/**
	 * Order a block according to this specific
	 * consensus implementation.
	 * @param block The block to order.
	 */
	public abstract void orderBlock(HyFlexChainBlock block);

	/**
	 * Create a block for this consensus mechanism ready
	 * to be proposed for ordering.
	 * @param txs The list of transactions that will be included in the created block.
	 * @return A new created block with the specified list of transactions.
	 */
	protected abstract HyFlexChainBlock createBlock(LinkedHashMap<String, HyFlexChainTransaction> txs);

	/**
	 * Verify a block when for integrity and all necessary
	 * checks.
	 * @param block The block to verify
	 * @return true if it is valid, otherwise false.
	 */
	protected boolean verifyBlock(HyFlexChainBlock block)
	{
		if (!block.verifyBlock())
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

		return verifyBody(block.body());
	}

	protected boolean verifyMetaHeader(BlockMetaHeader metaHeader)
	{
		return metaHeader.getConsensus() == this.consensus;
	}

	protected boolean verifyHeader(BlockHeader header, BlockBody body)
	{
		var lvi = LedgerViewInterface.getInstance();
		
		if (! header.getPrevHash().equalsIgnoreCase(lvi.getLastBlockHash(this.consensus)))
		{
			LOG.info("Invalid block header: prev hash");
			return false;
		}

		/* if ( header.getNonce() != lvi.getBlockchainSize(POW) + 1)
		{
			LOG.info("Invalid block header: invalid nonce");
			return false;
		} */

		if ( ! header.getMerkleRoot().equalsIgnoreCase(body.getMerkleTree().getRoot().hash()))
		{
			LOG.info("Invalid block header: invalid  merkle root");
			return false;
		}
		
		return true;
	}

	protected boolean verifyBody(BlockBody body)
	{
		return this.verifyBlockBodyPred.test(body);
	}

	private boolean verifyBody1_0(BlockBody body)
	{
		var lvi = LedgerViewInterface.getInstance();

		return body.getTransactions().values().stream()
			.allMatch((tx) -> {
				if (lvi.getTransactionState(tx.getHash(), this.consensus) == TransactionState.FINALIZED)
				{
					LOG.info("Invalid tx - is already finalized");
					return false;
				}

				return true;
			});
	}

	private boolean verifyBody2_0(BlockBody body)
	{
		var lvi = LedgerViewInterface.getInstance();
		var ti = TransactionInterface.getInstance();
		var txmanagement = TransactionManagement.getInstance();
		var execution = ExecutionPlane.getInstance();

		return body.getTransactions().values().stream()
			.allMatch((tx) -> {
				if (lvi.getTransactionState(tx.getHash(), this.consensus) == TransactionState.FINALIZED)
				{
					LOG.info("Invalid tx - is already finalized");
					return false;
				}
					
				try {
					ti.verifyTx(tx);
					txmanagement.verifyTx(tx);

					final TransactionParamsContractResult txParams =
						execution.callGetTransactionParams(tx);

					if (txParams.getConsensus() != this.consensus)
					{
						LOG.info("Invalid tx - smart contract exec failed -> consensus {} does not match the one for this block {}", txParams.getConsensus(), this.consensus);
						return false;
					}

					return true;
				} catch (InvalidTransactionException | InvalidSmartContractException e) {
					return false;
				}
			});
	}

	/**
	 * Get the consensus mechanism of this class implementation.
	 * @return the consensus
	 */
	public ConsensusMechanism getConsensus() {
		return consensus;
	}

}
