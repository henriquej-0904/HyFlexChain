package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.account;

import java.util.List;
import java.util.Optional;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.UTXOset;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.TxFinder;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.TxFinderList;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionId;

/**
 * An interface for interacting with accounts in the ledger.
 */
public interface Accounts
{
	/**
	 * Get the consensus mechanism of this ledger implementation.
	 * @return The consensus mechanism
	 */
	ConsensusMechanism getConsensusMechanism();

	/**
	 * Process a new block added to the chain.
	 * @param block The block to process.
	 */
	void processNewBlock(HyFlexChainBlock block);

	/**
	 * Locate a transaction given its id
	 * @param id The id of the transaction
	 * @return The transaction finder object.
	 */
	Optional<TxFinder> locateTransaction(TransactionId id);

	/**
	 * Get all transactions where the specified account is the origin.
	 * @param address The address of the account
	 * @return All transactions where the specified account is the origin.
	 */
	List<TxFinderList> locateTransactionsByOriginAccount(String address);

	/**
	 * Get all transactions where the specified account is the destination.
	 * @param address The address of the account
	 * @return All transactions where the specified account is the destination.
	 */
	List<TxFinderList> locateTransactionsByDestAccount(String address);

	/**
	 * Get all transactions according to the specified filter
	 * where the specified account is the origin.
	 * @param address The address of the account
	 * @param filter The filter
	 * @return All filtered transactions where the specified account is the origin.
	 */
	// List<TxFinder> getTransactionsByOriginAccount(String address, TransactionFilter filter);

	/**
	 * Get all transactions according to the specified filter
	 * where the specified account is the destination.
	 * @param pubKey The address of the account
	 * @param filter The filter
	 * @return All filtered transactions where the specified account is the destination.
	 */
	// Set<HyFlexChainTransaction> getTransactionsByDestAccount(String address, TransactionFilter filter);

	/**
	 * Get a ledger view of the UTXO set.
	 * @return UTXO set
	 */
	UTXOset getLedgerViewUTXOset();
}
