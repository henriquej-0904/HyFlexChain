package pt.unl.fct.di.hyflexchain.planes.application.lvi.consensus;

import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;

import java.util.Map.Entry;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.BlockFilter;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeId;
import pt.unl.fct.di.hyflexchain.planes.data.block.BlockState;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.LedgerState;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionState;
import pt.unl.fct.di.hyflexchain.util.crypto.HashedObject;

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
	Optional<HyFlexChainTransaction> getTransaction(Bytes id);

	/**
	 * Get the state of a transaction.
	 * @param id The id of the transaction
	 * @return The state of the transaction.
	 */
	TransactionState getTransactionState(Bytes id);

	/**
	 * Get all transactions where the specified account is the origin.
	 * @param originPubKey The public key of the account
	 * @return All transactions where the specified account is the origin.
	 */
	// Set<HyFlexChainTransaction> getTransactionsByOriginAccount(String originPubKey);

	/**
	 * Get all transactions where the specified account is the destination.
	 * @param destPubKey The public key of the account
	 * @return All transactions where the specified account is the destination.
	 */
	// Set<HyFlexChainTransaction> getTransactionsByDestAccount(String destPubKey);

	/**
	 * Get all transactions according to the specified filter
	 * where the specified account is the origin.
	 * @param pubKey The public key of the account
	 * @param filter The filter
	 * @return All filtered transactions where the specified account is the origin.
	 */
	// Set<HyFlexChainTransaction> getTransactionsByOriginAccount(String originPubKey, TransactionFilter filter);

	/**
	 * Get all transactions according to the specified filter
	 * where the specified account is the destination.
	 * @param pubKey The public key of the account
	 * @param filter The filter
	 * @return All filtered transactions where the specified account is the destination.
	 */
	// Set<HyFlexChainTransaction> getTransactionsByDestAccount(String destPubKey, TransactionFilter filter);

	/**
	 * Get all transactions according to the specified filter.
	 * @param filter The filter
	 * @return All filtered transactions.
	 */
	// Set<HyFlexChainTransaction> getTransactions(TransactionFilter filter);

	//#endregion


	//#region Blocks

	/**
	 * Get a block.
	 * @param id The id of the block
	 * @return The block.
	 */
	Optional<HyFlexChainBlock> getBlock(Bytes id);

	/**
	 * Get the state of a block.
	 * @param id The id of the block
	 * @return The state of the block.
	 */
	Optional<BlockState> getBlockState(Bytes id);

	/**
	 * Get all blocks according to the specified filter.
	 * @param filter The filter
	 * @return All filtered blocks.
	 */
	List<HashedObject<HyFlexChainBlock>> getBlocks(BlockFilter filter);

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
	// UTXOset getLedgerViewUTXOset();

	/**
	 * Get the currently active committee.
	 * @return The currently active committee.
	 */
	Entry<CommitteeId, ? extends Committee> getActiveCommittee();

	/**
	 * Get a ledger view of previous committees.
	 * 
	 * @param lastN The previous N committees
	 * @return Previous Committees.
	 */
	List<? extends Committee> getLedgerViewPreviousCommittees(int lastN);

	/**
	 * Get the next committee after the current one.
	 * @return
	 */
	Optional<Entry<CommitteeId, ? extends Committee>> getNextCommittee();
}
