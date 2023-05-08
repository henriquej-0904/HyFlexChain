package pt.unl.fct.di.hyflexchain.planes.txmanagement.txpool;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

/**
 * A transaction pool of pending transactions for a specific consensus
 * mechanism.
 */
public class TxPool
{
	public static final int PENDING_INIT_SIZE = 1000;

	/**
	 * The consensus mechanism of this Tx pool.
	 */
	private final ConsensusMechanism consensus;

	/**
	 * A map of pending transactions with insertion order.
	 */
	private final LinkedHashMap<String, HyFlexChainTransaction> pending;

	private final Lock lock;

	/**
	 * Create a new instance of a Tx pool.
	 * @param consensus The consensus mechanism of this Tx pool.
	 */
	public TxPool(ConsensusMechanism consensus) {
		this.consensus = consensus;
		this.pending = new LinkedHashMap<>(PENDING_INIT_SIZE);
		this.lock = new ReentrantLock();
	}

	/**
	 * The consensus mechanism of this Tx pool.
	 * @return the consensus
	 */
	public ConsensusMechanism getConsensus() {
		return consensus;
	}

	/**
	 * Get the current number pending txs.
	 * @return the current number pending txs.
	 */
	public int size()
	{
		return this.pending.size();
	}

	/**
	 * Add a pending tx only if it does not already exists.
	 * @param tx The tx to add.
	 * @return true if the tx was added.
	 */
	public boolean addTxIfAbsent(HyFlexChainTransaction tx)
	{
		this.lock.lock();

		boolean inserted = this.pending.putIfAbsent(tx.getHash(), tx) == null;

		this.lock.unlock();

		return inserted;
	}

	/**
	 * Get a list of n transactions.
	 * The map is sorted by the insertion order of transactions.
	 * @param n The number of transactions
	 * @return A list of transactions.
	 */
	public LinkedHashMap<String, HyFlexChainTransaction> getTxs(int n)
	{
		this.lock.lock();

		var list = this.pending.entrySet().stream()
			.limit(n)
			.collect(
				Collectors.toMap(
					Entry::getKey,
					Entry::getValue,
					(x1, x2) -> x1,
					() -> new LinkedHashMap<>(n)
				)
			);

		this.lock.unlock();

		return list;
	}
}
