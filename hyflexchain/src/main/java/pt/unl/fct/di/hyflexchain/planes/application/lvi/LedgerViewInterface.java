package pt.unl.fct.di.hyflexchain.planes.application.lvi;

import java.security.PublicKey;
import java.util.Set;

import pt.unl.fct.di.hyflexchain.planes.data.ledger.LedgerState;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionId;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionState;
import pt.unl.fct.di.hyflexchain.planes.ledgerview.views.LedgerView;

/**
 * The Ledger View Interface is responsible for exposing the Ledger State.
 * It is possible to get the full ledger, check the state of a transaction,
 * get transactions according to some filter and also, to create/get current views of the ledger.
 */
public interface LedgerViewInterface {
	
	/**
	 * Get the state of a transaction.
	 * @param id The id of the transaction
	 * @return The state of the transaction.
	 */
	TransactionState getTransactionState(TransactionId id);

	/**
	 * Get all transactions where the specified account is the origin.
	 * @param originPubKey The public key of the account
	 * @return All transactions where the specified account is the origin.
	 */
	Set<HyFlexChainTransaction> getTransactionsByOriginAccount(PublicKey originPubKey);

	/**
	 * Get all transactions where the specified account is the destination.
	 * @param destPubKey The public key of the account
	 * @return All transactions where the specified account is the destination.
	 */
	Set<HyFlexChainTransaction> getTransactionsByDestAccount(PublicKey destPubKey);

	/**
	 * Get all transactions according to the specified filter
	 * where the specified account is the origin.
	 * @param pubKey The public key of the account
	 * @param filter The filter
	 * @return All filtered transactions where the specified account is the origin.
	 */
	Set<HyFlexChainTransaction> getTransactionsByOriginAccount(PublicKey originPubKey, TransactionFilter filter);

	/**
	 * Get all transactions according to the specified filter
	 * where the specified account is the destination.
	 * @param pubKey The public key of the account
	 * @param filter The filter
	 * @return All filtered transactions where the specified account is the destination.
	 */
	Set<HyFlexChainTransaction> getTransactionsByDestAccount(PublicKey destPubKey, TransactionFilter filter);

	/**
	 * Get all transactions according to the specified filter.
	 * @param filter The filter
	 * @return All filtered transactions.
	 */
	Set<HyFlexChainTransaction> getTransactions(TransactionFilter filter);

	/**
	 * Get the full Ledger State.
	 * @return The full Ledger State.
	 */
	LedgerState getLedger();

	/**
	 * Get the ledger view.
	 * @return the ledger view
	 */
	LedgerView getLedgerView();

}
