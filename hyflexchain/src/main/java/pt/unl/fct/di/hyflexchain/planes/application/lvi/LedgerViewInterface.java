package pt.unl.fct.di.hyflexchain.planes.application.lvi;

import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.tuweni.bytes.Bytes;

import java.util.Optional;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.consensus.LedgerViewConsensusInterface;
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
 */
public interface LedgerViewInterface {
	
	public static LedgerViewInterface getInstance()
	{
		return SimpleLVI.getInstance();
	}

	LedgerViewConsensusInterface getLVI(ConsensusMechanism consensus);

	//#region Transactions

	/**
	 * Get a transaction.
	 * @param id The id of the transaction
	 * @param consensus The consensus mechanism
	 * @return The transaction.
	 */
	Optional<HyFlexChainTransaction> getTransaction(Bytes id, ConsensusMechanism consensus);

	/**
	 * Get the state of a transaction.
	 * @param id The id of the transaction
	 * @param consensus The consensus mechanism
	 * @return The state of the transaction.
	 */
	TransactionState getTransactionState(Bytes id, ConsensusMechanism consensus);

	/**
	 * Get all transactions where the specified account is the origin.
	 * @param originPubKey The public key of the account
	 * @param consensus The consensus mechanism
	 * @return All transactions where the specified account is the origin.
	 */
	// Set<HyFlexChainTransaction> getTransactionsByOriginAccount(String originPubKey, ConsensusMechanism consensus);

	/**
	 * Get all transactions where the specified account is the destination.
	 * @param destPubKey The public key of the account
	 * @param consensus The consensus mechanism
	 * @return All transactions where the specified account is the destination.
	 */
	// Set<HyFlexChainTransaction> getTransactionsByDestAccount(String destPubKey, ConsensusMechanism consensus);

	/**
	 * Get all transactions according to the specified filter
	 * where the specified account is the origin.
	 * @param pubKey The public key of the account
	 * @param filter The filter
	 * @param consensus The consensus mechanism
	 * @return All filtered transactions where the specified account is the origin.
	 */
	// Set<HyFlexChainTransaction> getTransactionsByOriginAccount(String originPubKey, TransactionFilter filter, ConsensusMechanism consensus);

	/**
	 * Get all transactions according to the specified filter
	 * where the specified account is the destination.
	 * @param pubKey The public key of the account
	 * @param filter The filter
	 * @param consensus The consensus mechanism
	 * @return All filtered transactions where the specified account is the destination.
	 */
	// Set<HyFlexChainTransaction> getTransactionsByDestAccount(String destPubKey, TransactionFilter filter, ConsensusMechanism consensus);

	/**
	 * Get all transactions according to the specified filter.
	 * @param filter The filter
	 * @param consensus The consensus mechanism
	 * @return All filtered transactions.
	 */
	// Set<HyFlexChainTransaction> getTransactions(TransactionFilter filter, ConsensusMechanism consensus);

	//#endregion


	//#region Blocks

	/**
	 * Get a block.
	 * @param id The id of the block
	 * @param consensus The consensus mechanism
	 * @return The block.
	 */
	Optional<HyFlexChainBlock> getBlock(Bytes id, ConsensusMechanism consensus);

	/**
	 * Get the state of a block.
	 * @param id The id of the block
	 * @param consensus The consensus mechanism
	 * @return The state of the block.
	 */
	Optional<BlockState> getBlockState(Bytes id, ConsensusMechanism consensus);

	/**
	 * Get all blocks according to the specified filter.
	 * @param filter The filter
	 * @param consensus The consensus mechanism
	 * @return All filtered blocks.
	 */
	List<HashedObject<HyFlexChainBlock>> getBlocks(BlockFilter filter, ConsensusMechanism consensus);

	/**
	 * Get the hash of the last block.
	 * @param consensus The consensus mechanism
	 * @return The hash of the last block.
	 */
	Bytes getLastBlockHash(ConsensusMechanism consensus);

	/**
	 * Get the number of blocks in the blockchain.
	 * @param consensus The consensus mechanism
	 * @return the number of blocks in the blockchain.
	 */
	long getBlockchainSize(ConsensusMechanism consensus);

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
	// UTXOset getLedgerViewUTXOset(ConsensusMechanism consensus);

	/**
	 * Get the currently active committee.
	 * @param consensus The consensus mechanism of the committee
	 * @return The currently active committee.
	 */
	Optional<Entry<CommitteeId, ? extends Committee>> getActiveCommittee(ConsensusMechanism consensus);

	/**
	 * Get a ledger view of previous defined committees.
	 * 
	 * @param lastN The previous N committees
	 * @param consensus The consensus mechanism
	 * @return Previous Committees.
	 */
	List<? extends Committee> getLedgerViewPreviousCommittees(int lastN, ConsensusMechanism consensus);

	/**
	 * Get the next committee after the currently active one.
	 * @param consensus
	 * @return
	 */
	Optional<Entry<CommitteeId, ? extends Committee>> getNextCommittee(ConsensusMechanism consensus);

}
