package pt.unl.fct.di.hyflexchain.planes.consensus;

import java.util.Collection;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.application.ti.TransactionInterface;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockBody;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.block.MerkleTree;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.SerializedTx;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionState;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionType;
import pt.unl.fct.di.hyflexchain.planes.execution.ExecutionPlane;
import pt.unl.fct.di.hyflexchain.planes.execution.contracts.InvalidSmartContractException;
import pt.unl.fct.di.hyflexchain.planes.execution.contracts.TransactionParamsContract.TransactionParamsContractResult;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.TransactionManagement;
import pt.unl.fct.di.hyflexchain.util.ResetInterface;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;

/**
 * Represents the interface for interacting with a specific
 * consensus mechanism.
 */
public abstract class ConsensusInterface implements ResetInterface
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
	// public abstract void orderBlock(HyFlexChainBlock block);

	/**
	 * Order a block body (set of transactions) according to this specific
	 * consensus implementation.
	 * @param blockBody The block body to order.
	 */
	// public abstract void orderTxs(BlockBody blockBody);

	/**
	 * Order a block body (set of serialized transactions) according to this specific
	 * consensus implementation.
	 * @param txs A collection of transactions to order.
	 */
	public abstract void orderTxs(Collection<SerializedTx> txs);

	/**
	 * Create a block for this consensus mechanism ready
	 * to be proposed for ordering.
	 * @param body The body of the block which includes the transactions.
	 * @return A new created block with the specified block body
	 */
	// protected abstract HyFlexChainBlock createBlock(BlockBody body);

	/**
	 * Create a block for this consensus mechanism ready
	 * to be proposed for ordering.
	 * @param previous The previous block
	 * @param body The body of the block which includes the transactions.
	 * @return A new created block with the specified block body
	 */
	// protected abstract HyFlexChainBlock createBlock(String previous, BlockBody body);

	/**
	 * Create a block for this consensus mechanism ready
	 * to be proposed for ordering.
	 * @param txs The list of transactions that will be included in the created block.
	 * @return A new created block with the specified list of transactions.
	 */
	// protected HyFlexChainBlock createBlock(LinkedHashMap<String, HyFlexChainTransaction> txs)
	// {
	// 	return createBlock(BlockBody.from(txs));
	// }

	/**
	 * Verify a block when for integrity and all necessary
	 * checks.
	 * @param block The block to verify
	 * @return true if it is valid, otherwise false.
	 */
	protected boolean verifyBlock(HyFlexChainBlock block, MerkleTree merkleTree)
	{
		if (! verifyMetaHeader(block))
		{
			LOG.info("Invalid block meta header");
			return false;
		}

		if (! verifyHeader(block, merkleTree))
		{
			LOG.info("Invalid block header");
			return false;
		}

		return verifyBody(block.body());
	}

	protected boolean verifyMetaHeader(HyFlexChainBlock block)
	{
		return block.header().getMetaHeader().getConsensus() == this.consensus;
	}

	protected boolean verifyHeader(HyFlexChainBlock block, MerkleTree merkleTree)
	{
		var header = block.header();
		var lvi = LedgerViewInterface.getInstance();
		
		if (! header.getPrevHash().equals(lvi.getLastBlockHash(this.consensus)))
		{
			LOG.info("Invalid block header: prev hash");
			return false;
		}

		if ( ! header.getMerkleRoot().equals(merkleTree.getMerkleRootHash()))
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

		return body.findTransactions().entrySet().stream()
			.allMatch((tx) -> {
				if (lvi.getTransactionState(tx.getKey(), this.consensus) == TransactionState.FINALIZED)
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

		return body.findTransactions().entrySet().stream()
			.allMatch((tx) -> {
				if (lvi.getTransactionState(tx.getKey(), this.consensus) == TransactionState.FINALIZED)
				{
					LOG.info("Invalid tx - is already finalized");
					return false;
				}
					
				try {
					ti.verifyTx(tx.getValue());
					txmanagement.verifyTx(tx.getValue());

					if (tx.getValue().getTransactionType() == TransactionType.TRANSFER) {
						final TransactionParamsContractResult txParams = execution
								.executeSmartContract(tx.getValue());
						
						if (txParams.getConsensus() != this.consensus) {
							LOG.info(
									"Invalid tx - smart contract exec failed -> consensus {} does not match the one for this block {}",
									txParams.getConsensus(), this.consensus);
							return false;
						}
					} else if (tx.getValue().getTransactionType() == TransactionType.CONTRACT_CREATE ||
						tx.getValue().getTransactionType() == TransactionType.CONTRACT_REVOKE)
					{
						if (ConsensusMechanism.PoW != this.consensus) {
							LOG.info(
									"Invalid tx type for PoW mechanism: " + tx.getValue().getTransactionType());
							return false;
						}
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
