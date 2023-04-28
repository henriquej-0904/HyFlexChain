package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated;

import java.util.Optional;
import java.util.Set;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.BlockFilter;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.HistoryPreviousCommittees;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.UTXOset;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.data.TransactionFilter;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockState;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.LedgerState;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionState;
import pt.unl.fct.di.hyflexchain.util.config.LedgerConfig;

/**
 * An interface for a consensus specific ledger implementation.
 */
public interface ConsensusSpecificLedger
{
	/**
	 * Initialize the ledger with the provided configurations.
	 * @param config The configuration of the ledger.
	 * @return The initialized object
	 */
	ConsensusSpecificLedger init(LedgerConfig config);

	/**
	 * Get the consensus mechanism of this ledger implementation.
	 * @return The consensus mechanism
	 */
	ConsensusMechanism getConsensusMechanism();

	/**
	 * Write an ordered block to the Ledger.
	 * @param block The ordered block
	 */
	void writeOrderedBlock(HyFlexChainBlock block);

	/**
	 * Get a transaction.
	 * @param id The id of the transaction
	 * @return The transaction.
	 */
	Optional<HyFlexChainTransaction> getTransaction(String id);

	/**
	 * Get the state of a transaction.
	 * @param id The id of the transaction
	 * @return The state of the transaction.
	 */
	Optional<TransactionState> getTransactionState(String id);

	/**
	 * Get all transactions where the specified account is the origin.
	 * @param originPubKey The public key of the account
	 * @return All transactions where the specified account is the origin.
	 */
	Set<HyFlexChainTransaction> getTransactionsByOriginAccount(String originPubKey);

	/**
	 * Get all transactions where the specified account is the destination.
	 * @param destPubKey The public key of the account
	 * @return All transactions where the specified account is the destination.
	 */
	Set<HyFlexChainTransaction> getTransactionsByDestAccount(String destPubKey);

	/**
	 * Get all transactions according to the specified filter
	 * where the specified account is the origin.
	 * @param pubKey The public key of the account
	 * @param filter The filter
	 * @return All filtered transactions where the specified account is the origin.
	 */
	Set<HyFlexChainTransaction> getTransactionsByOriginAccount(String originPubKey, TransactionFilter filter);

	/**
	 * Get all transactions according to the specified filter
	 * where the specified account is the destination.
	 * @param pubKey The public key of the account
	 * @param filter The filter
	 * @return All filtered transactions where the specified account is the destination.
	 */
	Set<HyFlexChainTransaction> getTransactionsByDestAccount(String destPubKey, TransactionFilter filter);

	/**
	 * Get all transactions according to the specified filter.
	 * @param filter The filter
	 * @return All filtered transactions.
	 */
	Set<HyFlexChainTransaction> getTransactions(TransactionFilter filter);

	//#endregion


	//#region Blocks

	/**
	 * Get a block.
	 * @param id The id of the block
	 * @return The block.
	 */
	Optional<HyFlexChainBlock> getBlock(String id);

	/**
	 * Get the state of a block.
	 * @param id The id of the block
	 * @return The state of the block.
	 */
	Optional<BlockState> getBlockState(String id);

	/**
	 * Get all blocks according to the specified filter.
	 * @param filter The filter
	 * @return All filtered blocks.
	 */
	Set<HyFlexChainBlock> getBlocks(BlockFilter filter);

	//#endregion

	/**
	 * Get the full Ledger State.
	 * @return The full Ledger State.
	 */
	LedgerState getLedger();

	/**
	 * Get a ledger view of the UTXO set.
	 * @return UTXO set
	 */
	UTXOset getLedgerViewUTXOset();

	/**
	 * Get the currently active committee.
	 * @return The currently active committee.
	 */
	Committee getActiveCommittee();

	/**
	 * Get a ledger view of previous committees.
	 * 
	 * @param lastN The previous N committees
	 * @return Previous Committees.
	 */
	HistoryPreviousCommittees getLedgerViewPreviousCommittees(int lastN);
}
