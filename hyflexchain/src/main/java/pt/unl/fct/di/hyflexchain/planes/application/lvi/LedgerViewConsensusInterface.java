package pt.unl.fct.di.hyflexchain.planes.application.lvi;

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
 * 
 * This type of interface is associated with a specific consensus mechanism.
 */
public interface LedgerViewConsensusInterface
{
	/**
	 * The consensus mechanism of this interface.
	 * @return The consensus mechanism of this interface.
	 */
	public ConsensusMechanism getConsensusMechanism();

	//#region Transactions

	/**
	 * Get a transaction.
	 * @param id The id of the transaction
	 * @return The transaction.
	 */
	HyFlexChainTransaction getTransaction(String id);

	/**
	 * Get the state of a transaction.
	 * @param id The id of the transaction
	 * @return The state of the transaction.
	 */
	TransactionState getTransactionState(String id);

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
	HyFlexChainBlock getBlock(String id);

	/**
	 * Get the state of a block.
	 * @param id The id of the block
	 * @return The state of the block.
	 */
	BlockState getBlockState(String id);

	/**
	 * Get all blocks according to the specified filter.
	 * @param filter The filter
	 * @return All filtered blocks.
	 */
	Set<HyFlexChainBlock> getBlocks(BlockFilter filter);

	//#endregion

	/**
	 * Get the full Ledger State for this type of consensus.
	 * @return The full Ledger State.
	 */
	LedgerState getLedger();

	/**
	 * Get a ledger view of the UTXO set for this type of consensus.
	 * @return UTXO set
	 */
	UTXOset getLedgerViewUTXOset();

	/**
	 * Get a ledger view of previous committees for this type of consensus.
	 * 
	 * @param lastN The previous N committees
	 * @return Previous Committees.
	 */
	HistoryPreviousCommittees getLedgerViewPreviousCommittees(int last);
}
