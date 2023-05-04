package pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.inmemory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.blockchain.TxFinderRec;
import pt.unl.fct.di.hyflexchain.planes.data.ledger.separated.blockchain.TxFinderList;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.TxInput;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.UTXO;

/**
 * Represents an account on the system.
 * For efficiency, instead of saving TxFinder
 * (pointers to txs) it saves the txs.
 */
public class Account
{
	private static final int TXS_INIT_SIZE = 100;

	public final String address;

	protected Map<String, HyFlexChainTransaction> sentTxs;

	protected Map<String, HyFlexChainTransaction> receivedTxs;

	protected Map<TxInput, UTXO> utxos;

	/**
	 * Create an account with the specified address.
	 * @param address The address of the account.
	 */
	public Account(String address) {
		this.address = address;
		this.sentTxs = new HashMap<>(TXS_INIT_SIZE);
		this.receivedTxs = new HashMap<>(TXS_INIT_SIZE);
		this.utxos = new HashMap<>(TXS_INIT_SIZE);
	}

	/**
	 * Locate a transaction given its id and return it
	 * @param txHash The hash of the transaction
	 * @return The transaction.
	 */
	Optional<HyFlexChainTransaction> getTransaction(String txHash)
	{
		return Optional.ofNullable(this.sentTxs.get(txHash));
	}

	/**
	 * Get all sent transactions.
	 * @return All sent transactions.
	 */
	Collection<HyFlexChainTransaction> getSentTransactions()
	{
		return Collections.unmodifiableCollection(this.sentTxs.values());
	}

	/**
	 * Get all received transactions.
	 * @return All received transactions.
	 */
	Collection<HyFlexChainTransaction> getReceivedTransactions()
	{
		return Collections.unmodifiableCollection(this.receivedTxs.values());
	}


}
