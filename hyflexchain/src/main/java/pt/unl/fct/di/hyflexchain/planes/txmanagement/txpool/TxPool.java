package pt.unl.fct.di.hyflexchain.planes.txmanagement.txpool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.thavam.util.concurrent.blockingMap.BlockingHashMap;
import org.thavam.util.concurrent.blockingMap.BlockingMap;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.SerializedTx;
import pt.unl.fct.di.hyflexchain.util.ResetInterface;

/**
 * A transaction pool of pending transactions for a specific consensus
 * mechanism.
 */
public class TxPool implements ResetInterface
{
	public static final int WAIT_MILLIS = 10;
	public static final int PENDING_INIT_SIZE = 1000;

	/**
	 * The consensus mechanism of this Tx pool.
	 */
	private final ConsensusMechanism consensus;

	/**
	 * A map of tx hash and result. It represents the
	 * result of a processed transaction.
	 */
	private BlockingMap<Bytes, Boolean> finished;

	private final Map<Bytes, SerializedTx> waitingForFinalization;

	/**
	 * A map of pending transactions with insertion order.
	 */
	private final LinkedHashMap<Bytes, SerializedTx> pending;

	private final Lock lock;

	/**
	 * Create a new instance of a Tx pool.
	 * @param consensus The consensus mechanism of this Tx pool.
	 */
	public TxPool(ConsensusMechanism consensus) {
		this.consensus = consensus;
		this.waitingForFinalization = new HashMap<>(PENDING_INIT_SIZE);
		this.pending = new LinkedHashMap<>(PENDING_INIT_SIZE);
		this.finished = new BlockingHashMap<>();

		this.lock = new ReentrantLock();
	}

	@Override
	public void reset() {
		this.lock.lock();

		this.waitingForFinalization.clear();
		this.pending.clear();
		this.finished = new BlockingHashMap<>();
		
		this.lock.unlock();
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
	public boolean addTxIfAbsent(SerializedTx tx)
	{
		this.lock.lock();

		boolean inserted = this.pending.putIfAbsent(tx.hash(), tx) == null;

		this.lock.unlock();

		return inserted;
	}

	/**
	 * Add a pending tx only if it does not already exists.
	 * <p> This method returns when the transaction was ordered.
	 * @param tx The tx to add.
	 * @return true if it was sucessfully ordered.
	 * @throws InterruptedException
	 */
	public boolean addTxIfAbsentAndWait(SerializedTx tx, Logger log) throws InterruptedException
	{
		this.lock.lock();

		boolean inserted = this.pending.putIfAbsent(tx.hash(), tx) == null;

		this.lock.unlock();

		if (!inserted)
		{
			return false;
		}

		log.info("Submited tx [{}] with {} bytes size & hash: {}", consensus.getConsensus(),
		tx.serialized().length ,tx.hash());

		return this.finished.take(tx.hash());
	}

	/**
	 * Get a list of n transactions.
	 * The map is sorted by the insertion order of transactions.
	 * This method waits for the specified number of txs to be available.
	 * @param n The minimum number of transactions
	 * @return A list of transactions.
	 */
	public List<SerializedTx> waitForMinPendingTxs(int n)
	{
		while (true) {
			while (this.pending.size() < n) {
				// wait for min number of txs in tx pool
		
				try {
					Thread.sleep(WAIT_MILLIS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			this.lock.lock();

			if (this.pending.size() < n)
			{
				this.lock.unlock();
				continue;
			}

			List<SerializedTx> list = new ArrayList<>(n);

			var it = this.pending.entrySet().iterator();
			for (int i = 0; i < n && it.hasNext(); i++) {
				var tx = it.next();
				it.remove();

				list.add(tx.getValue());
				waitingForFinalization.put(tx.getKey(), tx.getValue());
			}

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

	/**
	 * Get a list of n transactions.
	 * The map is sorted by the insertion order of transactions.
	 * This method waits for the specified number of txs to be available
	 * or when the specified time expires with at least 1 transaction.
	 * @param n The minimum number of transactions
	 * @param millis The maximum time to wait in ms
	 * @return A list of transactions.
	 */
	public List<SerializedTx> waitForMinPendingTxs(int n, long millis)
	{
		while (true) {
			while (
				this.pending.isEmpty() ||
				(this.pending.size() < n && millis >= WAIT_MILLIS)
			)
			{
				// wait for min number of txs in tx pool
		
				try {
					Thread.sleep(WAIT_MILLIS);
					millis -= WAIT_MILLIS;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			this.lock.lock();

			if (this.pending.isEmpty() ||
				(this.pending.size() < n && millis >= WAIT_MILLIS))
			{
				this.lock.unlock();
				continue;
			}

			List<SerializedTx> list = new ArrayList<>(n);

			var it = this.pending.entrySet().iterator();
			for (int i = 0; i < n && it.hasNext(); i++) {
				var tx = it.next();
				it.remove();

				list.add(tx.getValue());
				waitingForFinalization.put(tx.getKey(), tx.getValue());
			}

			this.lock.unlock();

			return list;
		}
	}

	/**
	 * Get a list of n transactions.
	 * The map is sorted by the insertion order of transactions.
	 * This method waits for the specified number of txs to be available
	 * or when the specified time expires with at least 1 transaction.
	 * @param n The minimum number of transactions
	 * @param millis The maximum time to wait in ms
	 * @return A list of transactions.
	 */
	public List<SerializedTx> getAllPendingTxs()
	{
		while (true) {
			while (this.pending.isEmpty())
			{
				// wait for min number of txs in tx pool
		
				try {
					Thread.sleep(WAIT_MILLIS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			this.lock.lock();

			if (this.pending.isEmpty())
			{
				this.lock.unlock();
				continue;
			}

			List<SerializedTx> list = new ArrayList<>(pending.size());

			pending.forEach((txHash, tx) -> {
				list.add(tx);
				waitingForFinalization.put(txHash, tx);
			});

			this.pending.clear();

			this.lock.unlock();

			return list;
		}
	}

	public Optional<SerializedTx> getPendingTx(Bytes txHash)
	{
		return Optional.ofNullable(this.pending.get(txHash));
	}

	public boolean txExists(Bytes txHash)
	{
		return this.pending.containsKey(txHash) ||
			this.waitingForFinalization.containsKey(txHash);
	}

	/**
	 * Remove all the specified pending transactions
	 * and notify all threads that are waiting.
	 * @param txs The processed transactions
	 * @param result The result
	 */
	public void removePendingTxsAndNotify(Collection<Bytes> txs, boolean result)
	{
		List<Bytes> txsToNotify = new ArrayList<>(txs.size());

		this.lock.lock();

		for (var txHash : txs) {
			if (pending.remove(txHash) != null ||
				waitingForFinalization.remove(txHash) != null)
				txsToNotify.add(txHash);
		}

		this.lock.unlock();

		// notify waiting threads

		for (var txHash : txsToNotify) {
			finished.put(txHash, result);
		}
	}

	/**
	 * Remove the specified pending transaction
	 * and notify all threads that are waiting.
	 * @param txHash The hash of the transaction
	 * @param result The result of the transaction:
	 * true if successfull.
	 */
	public void removePendingTxAndNotify(Bytes txHash, boolean result)
	{
		// remove tx

		this.lock.lock();

		this.pending.remove(txHash);
		this.waitingForFinalization.remove(txHash);

		this.lock.unlock();

		// notify waiting threads
		this.finished.put(txHash, result);
	}
}
