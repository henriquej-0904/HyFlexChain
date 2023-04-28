package pt.unl.fct.di.hyflexchain.planes.data;

import java.util.EnumMap;
import java.util.Optional;
import java.util.Set;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.BlockFilter;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.HistoryPreviousCommittees;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.UTXOset;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockState;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.LedgerState;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionState;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;

/**
 * Represents the Data Plane with operations to add a block to the chain
 * and query already ordered transactions and blocks.
 * Additionally, it provides other information (UTXO set, previous committees, etc).
 */
public interface DataPlane {

	/**
	 * Get the applied Ledger parameters
	 * @return The applied Ledger parameters
	 */
	MultiLedgerConfig getLedgerConfig();

	/**
	 * Write an ordered block to the Ledger.
	 * @param block The ordered block
	 * @param consensusType The type of consensus mechanism used to order the block
	 */
	void writeOrderedBlock(HyFlexChainBlock block, ConsensusMechanism consensusType);

	/**
	 * Dispatch an unordered block to the Blockmess Layer to be ordered
	 * by the specified consensus mechanism.
	 * @param block The unordered block to dispatch to Blockmess
	 * @param consensusType The type of consensus mechanism to be used to order the block
	 */
	// void dispatchUnorderedBlockToBlockmess(HyFlexChainBlock block, ConsensusType consensusType);

	/**
	 * Dispatch an unordered transaction to the Blockmess Layer to be ordered
	 * by the specified consensus mechanism.
	 * @param tx The unordered transaction to dispatch to Blockmess
	 * @param consensusType The type of consensus mechanism to be used to order the transaction
	 */
	// void dispatchUnorderedTransactionToBlockmess(HyFlexChainTransaction tx, ConsensusType consensusType);


	//#region Transactions

	/**
	 * Get a transaction.
	 * @param id The id of the transaction
	 * @param consensus The consensus mechanism
	 * @return The transaction.
	 */
	Optional<HyFlexChainTransaction> getTransaction(String id, ConsensusMechanism consensus);

	/**
	 * Get the state of a transaction.
	 * @param id The id of the transaction
	 * @param consensus The consensus mechanism
	 * @return The state of the transaction.
	 */
	Optional<TransactionState> getTransactionState(String id, ConsensusMechanism consensus);

	/**
	 * Get all transactions where the specified account is the origin.
	 * @param originPubKey The public key of the account
	 * @param consensus The consensus mechanism
	 * @return All transactions where the specified account is the origin.
	 */
	Set<HyFlexChainTransaction> getTransactionsByOriginAccount(String originPubKey, ConsensusMechanism consensus);

	/**
	 * Get all transactions where the specified account is the destination.
	 * @param destPubKey The public key of the account
	 * @param consensus The consensus mechanism
	 * @return All transactions where the specified account is the destination.
	 */
	Set<HyFlexChainTransaction> getTransactionsByDestAccount(String destPubKey, ConsensusMechanism consensus);

	/**
	 * Get all transactions according to the specified filter
	 * where the specified account is the origin.
	 * @param pubKey The public key of the account
	 * @param filter The filter
	 * @param consensus The consensus mechanism
	 * @return All filtered transactions where the specified account is the origin.
	 */
	Set<HyFlexChainTransaction> getTransactionsByOriginAccount(String originPubKey, TransactionFilter filter, ConsensusMechanism consensus);

	/**
	 * Get all transactions according to the specified filter
	 * where the specified account is the destination.
	 * @param pubKey The public key of the account
	 * @param filter The filter
	 * @param consensus The consensus mechanism
	 * @return All filtered transactions where the specified account is the destination.
	 */
	Set<HyFlexChainTransaction> getTransactionsByDestAccount(String destPubKey, TransactionFilter filter, ConsensusMechanism consensus);

	/**
	 * Get all transactions according to the specified filter.
	 * @param filter The filter
	 * @param consensus The consensus mechanism
	 * @return All filtered transactions.
	 */
	Set<HyFlexChainTransaction> getTransactions(TransactionFilter filter, ConsensusMechanism consensus);

	//#endregion


	//#region Blocks

	/**
	 * Get a block.
	 * @param id The id of the block
	 * @param consensus The consensus mechanism
	 * @return The block.
	 */
	Optional<HyFlexChainBlock> getBlock(String id, ConsensusMechanism consensus);

	/**
	 * Get the state of a block.
	 * @param id The id of the block
	 * @param consensus The consensus mechanism
	 * @return The state of the block.
	 */
	Optional<BlockState> getBlockState(String id, ConsensusMechanism consensus);

	/**
	 * Get all blocks according to the specified filter.
	 * @param filter The filter
	 * @param consensus The consensus mechanism
	 * @return All filtered blocks.
	 */
	Set<HyFlexChainBlock> getBlocks(BlockFilter filter, ConsensusMechanism consensus);

	//#endregion

	/**
	 * Get the full Ledger State.
	 * @return The full Ledger State.
	 */
	EnumMap<ConsensusMechanism, LedgerState> getLedger();

	/**
	 * Get the full Ledger State.
	 * @param consensus The consensus mechanism
	 * @return The full Ledger State.
	 */
	LedgerState getLedger(ConsensusMechanism consensus);

	/**
	 * Get a ledger view of the UTXO set.
	 * @param consensus The consensus mechanism
	 * @return UTXO set
	 */
	UTXOset getLedgerViewUTXOset(ConsensusMechanism consensus);

	/**
	 * Get the currently active committee.
	 * @param consensus The consensus mechanism of the committee
	 * @return The currently active committee.
	 */
	Committee getActiveCommittee(ConsensusMechanism consensus);

	/**
	 * Get a ledger view of previous committees.
	 * 
	 * @param lastN The previous N committees
	 * @param consensus The consensus mechanism
	 * @return Previous Committees.
	 */
	HistoryPreviousCommittees getLedgerViewPreviousCommittees(int lastN, ConsensusMechanism consensus);
}
