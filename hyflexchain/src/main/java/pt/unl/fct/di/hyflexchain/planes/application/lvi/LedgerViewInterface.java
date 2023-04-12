package pt.unl.fct.di.hyflexchain.planes.application.lvi;

import java.security.PublicKey;
import java.util.Set;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.HistoryPreviousCommittees;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.UTXOset;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusType;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockId;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockState;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.LedgerState;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionId;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionState;

/**
 * The Ledger View Interface is responsible for exposing the Ledger State.
 * It is possible to get the full ledger, check the state of a transaction/block,
 * get transactions/blocks according to some filter and also, to create/get current views of the ledger.
 */
public interface LedgerViewInterface {
	

	//#region Transactions

	/**
	 * Get a transaction.
	 * @param id The id of the transaction
	 * @param consensus The consensus mechanism
	 * @return The transaction.
	 */
	HyFlexChainTransaction getTransaction(TransactionId id, ConsensusType consensus);

	/**
	 * Get the state of a transaction.
	 * @param id The id of the transaction
	 * @param consensus The consensus mechanism
	 * @return The state of the transaction.
	 */
	TransactionState getTransactionState(TransactionId id, ConsensusType consensus);

	/**
	 * Get all transactions where the specified account is the origin.
	 * @param originPubKey The public key of the account
	 * @param consensus The consensus mechanism
	 * @return All transactions where the specified account is the origin.
	 */
	Set<HyFlexChainTransaction> getTransactionsByOriginAccount(PublicKey originPubKey, ConsensusType consensus);

	/**
	 * Get all transactions where the specified account is the destination.
	 * @param destPubKey The public key of the account
	 * @param consensus The consensus mechanism
	 * @return All transactions where the specified account is the destination.
	 */
	Set<HyFlexChainTransaction> getTransactionsByDestAccount(PublicKey destPubKey, ConsensusType consensus);

	/**
	 * Get all transactions according to the specified filter
	 * where the specified account is the origin.
	 * @param pubKey The public key of the account
	 * @param filter The filter
	 * @param consensus The consensus mechanism
	 * @return All filtered transactions where the specified account is the origin.
	 */
	Set<HyFlexChainTransaction> getTransactionsByOriginAccount(PublicKey originPubKey, TransactionFilter filter, ConsensusType consensus);

	/**
	 * Get all transactions according to the specified filter
	 * where the specified account is the destination.
	 * @param pubKey The public key of the account
	 * @param filter The filter
	 * @param consensus The consensus mechanism
	 * @return All filtered transactions where the specified account is the destination.
	 */
	Set<HyFlexChainTransaction> getTransactionsByDestAccount(PublicKey destPubKey, TransactionFilter filter, ConsensusType consensus);

	/**
	 * Get all transactions according to the specified filter.
	 * @param filter The filter
	 * @param consensus The consensus mechanism
	 * @return All filtered transactions.
	 */
	Set<HyFlexChainTransaction> getTransactions(TransactionFilter filter, ConsensusType consensus);

	//#endregion


	//#region Blocks

	/**
	 * Get a block.
	 * @param id The id of the block
	 * @param consensus The consensus mechanism
	 * @return The block.
	 */
	HyFlexChainBlock getBlock(BlockId id, ConsensusType consensus);

	/**
	 * Get the state of a block.
	 * @param id The id of the block
	 * @param consensus The consensus mechanism
	 * @return The state of the block.
	 */
	BlockState getBlockState(BlockId id, ConsensusType consensus);

	/**
	 * Get all blocks according to the specified filter.
	 * @param filter The filter
	 * @param consensus The consensus mechanism
	 * @return All filtered blocks.
	 */
	Set<HyFlexChainBlock> getBlocks(BlockFilter filter, ConsensusType consensus);

	//#endregion

	/**
	 * Get the full Ledger State.
	 * @return The full Ledger State.
	 */
	LedgerState getLedger();

	/**
	 * Get the full Ledger State.
	 * @param consensus The consensus mechanism
	 * @return The full Ledger State.
	 */
	LedgerState getLedger(ConsensusType consensus);

	/**
	 * Get a ledger view of the UTXO set.
	 * @param consensus The consensus mechanism
	 * @return UTXO set
	 */
	UTXOset getLedgerViewUTXOset(ConsensusType consensus);

	/**
	 * Get a ledger view of previous committees.
	 * 
	 * @param lastN The previous N committees
	 * @param consensus The consensus mechanism
	 * @return Previous Committees.
	 */
	HistoryPreviousCommittees getLedgerViewPreviousCommittees(int lastN, ConsensusType consensus);

}
