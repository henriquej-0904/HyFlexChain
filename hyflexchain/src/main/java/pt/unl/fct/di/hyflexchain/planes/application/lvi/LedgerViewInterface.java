package pt.unl.fct.di.hyflexchain.planes.application.lvi;

import java.util.EnumMap;
import java.util.Set;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.HistoryPreviousCommittees;
import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.UTXOset;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockState;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.LedgerState;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
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
	HyFlexChainTransaction getTransaction(String id, ConsensusMechanism consensus);

	/**
	 * Get the state of a transaction.
	 * @param id The id of the transaction
	 * @param consensus The consensus mechanism
	 * @return The state of the transaction.
	 */
	TransactionState getTransactionState(String id, ConsensusMechanism consensus);

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
	HyFlexChainBlock getBlock(String id, ConsensusMechanism consensus);

	/**
	 * Get the state of a block.
	 * @param id The id of the block
	 * @param consensus The consensus mechanism
	 * @return The state of the block.
	 */
	BlockState getBlockState(String id, ConsensusMechanism consensus);

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
	 * Get a ledger view of previous committees.
	 * 
	 * @param lastN The previous N committees
	 * @param consensus The consensus mechanism
	 * @return Previous Committees.
	 */
	HistoryPreviousCommittees getLedgerViewPreviousCommittees(int lastN, ConsensusMechanism consensus);

}
