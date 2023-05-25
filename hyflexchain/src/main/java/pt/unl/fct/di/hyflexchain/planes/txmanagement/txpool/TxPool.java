package pt.unl.fct.di.hyflexchain.planes.txmanagement.txpool;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

/**
 * A transaction pool of pending transactions for a specific consensus
 * mechanism.
 */
public class TxPool
{
	public static final int WAIT_MILLIS = 10;
	public static final int PENDING_INIT_SIZE = 1000;

	/**
	 * The consensus mechanism of this Tx pool.
	 */
	private final ConsensusMechanism consensus;

	/**
	 * A map of transactions that were already
	 * proposed and are waiting to be finalized
	 */
	private final Map<String, HyFlexChainTransaction> waitingForFinalization;

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
		this.waitingForFinalization = new LinkedHashMap<>(PENDING_INIT_SIZE);

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
	 * Add a pending tx only if it does not already exists.
	 * <p> This method returns when the transaction was ordered.
	 * @param tx The tx to add.
	 * @return true if it was sucessfully ordered.
	 */
	public boolean addTxIfAbsentAndWait(HyFlexChainTransaction tx, Logger log)
	{
		this.lock.lock();

		boolean inserted = this.pending.putIfAbsent(tx.getHash(), tx) == null;

		this.lock.unlock();

		if (!inserted)
		{
			return false;
		}

		log.info("Submited tx: " + tx.getHash());

		synchronized(tx)
		{
			try {
				tx.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return inserted;
	}

	/**
	 * Get a list of n transactions.
	 * The map is sorted by the insertion order of transactions.
	 * This method waits for the specified number of txs to be available.
	 * @param n The minimum number of transactions
	 * @return A list of transactions.
	 */
	public LinkedHashMap<String, HyFlexChainTransaction> waitForMinPendingTxs(int n)
	{
		while (true) {
			while (this.pending.size() < n) {
				// wait for min number of txs in tx pool
		
				try {
					Thread.sleep(WAIT_MILLIS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			this.lock.lock();

			if (this.pending.size() < n)
			{
				this.lock.unlock();
				continue;
			}

			LinkedHashMap<String, HyFlexChainTransaction> list =
				new LinkedHashMap<>(n);

			var it = this.pending.entrySet().iterator();
			for (int i = 0; i < n && it.hasNext(); i++) {
				var tx = it.next();
				it.remove();

				list.put(tx.getKey(), tx.getValue());
			}

			this.waitingForFinalization.putAll(list);

			this.lock.unlock();

			return list;
		}

		/* var list = this.pending.entrySet().stream()
			.limit(n)
			.collect(
				Collectors.toMap(
					Entry::getKey,
					Entry::getValue,
					(x1, x2) -> x1,
					() -> new LinkedHashMap<>(n)
				)
			); */
	}

	public Optional<HyFlexChainTransaction> getPendingTx(String txHash)
	{
		return Optional.ofNullable(this.pending.get(txHash));
	}

	public boolean txExists(String txHash)
	{
		return this.pending.containsKey(txHash) ||
			this.waitingForFinalization.containsKey(txHash);
	}

	/**
	 * Remove all the specified pending transactions
	 * and notify all threads that are waiting.
	 * @param mapTxRes A map of Transaction hash, Result.
	 */
	public void removePendingTxsAndNotify(Map<String, Boolean> mapTxRes)
	{
		List<HyFlexChainTransaction> txs
			= new ArrayList<>(mapTxRes.size());

		// remove txs

		this.lock.lock();

		for (var txRes : mapTxRes.entrySet()) {
			var tx = this.pending.remove(txRes.getKey());
			if (tx != null)
				txs.add(tx);

			tx = this.waitingForFinalization.remove(txRes.getKey());
			if (tx != null)
				txs.add(tx);
		}

		this.lock.unlock();

		// notify waiting threads

		for (HyFlexChainTransaction tx : txs) {
			synchronized(tx)
			{
				tx.notifyAll();
			}
		}
	}
}
