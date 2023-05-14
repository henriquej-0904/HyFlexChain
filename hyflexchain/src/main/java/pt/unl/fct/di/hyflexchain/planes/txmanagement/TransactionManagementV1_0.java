package pt.unl.fct.di.hyflexchain.planes.txmanagement;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.EnumMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.txpool.TxPool;

/**
 * An implementation of the TransactionManagement Interface
 * in which corresponds to the V1_0 version of SystemVersion,
 * i.e., there is only one consensus mechanism (PoW) and
 * there are no smart contracts.
 */
public class TransactionManagementV1_0 implements TransactionManagement
{
	private static final Logger LOGGER = LogManager.getLogger();

	protected final EnumMap<ConsensusMechanism, TxPool> txPools;

	/**
	 * @param txPool
	 */
	public TransactionManagementV1_0() {
		this.txPools = new EnumMap<>(ConsensusMechanism.class);
		this.txPools.put(ConsensusMechanism.PoW, new TxPool(ConsensusMechanism.PoW));
	}

	@Override
	public TxPool getTxPool(ConsensusMechanism consensus) {
		switch (consensus) {
			case PoW:
				return this.txPools.get(consensus);
			default:
				throw new Error("There are only one TxPool for PoW consensus mechanism.");
		}
	}

	/**
	 * Verifies if a transaction is valid, the cryptographic signature is correct
	 * and other aspects.
	 * @param tx The transaction to verify
	 * @throws InvalidTransactionException if the transaction was not verified.
	 */
	protected void verifyTx(HyFlexChainTransaction tx) throws InvalidTransactionException
	{
		if (! tx.verifyHash())
		{
			var msg = "Transaction invalid hash";
			LOGGER.info(msg);
			throw new InvalidTransactionException(msg);
		}

		try {
			if (! tx.verifySignature())
			{
				var msg = "Transaction invalid signature";
				LOGGER.info(msg);
				throw new InvalidTransactionException(msg);
			}
				
		} catch (InvalidKeyException | InvalidKeySpecException |
				NoSuchAlgorithmException | SignatureException e) {
			LOGGER.info(e.getMessage());
			throw new InvalidTransactionException(e.getMessage(), e);
		}
	}

	/**
	 * Add a pending transaction to the pool of transactions.
	 * @param pool The pool of transactions
	 * @param tx The pending transaction to add
	 * @throws InvalidTransactionException If the transaction was already submitted.
	 */
	protected void addPendingTx(TxPool pool, HyFlexChainTransaction tx) throws InvalidTransactionException
	{
		if ( ! pool.addTxIfAbsent(tx) )
		{
			var msg = "Invalid Transaction: Already submitted.";
			LOGGER.info(msg);
			throw new InvalidTransactionException(msg);
		}
	}

	@Override
	public String dispatchTransaction(HyFlexChainTransaction tx) throws InvalidTransactionException
	{
		verifyTx(tx);
		addPendingTx(getTxPool(ConsensusMechanism.PoW), tx);
		return tx.getHash();  
	}

	@Override
	public String dispatchTransactionAndWait(HyFlexChainTransaction tx) throws InvalidTransactionException
	{
		var hash = dispatchTransaction(tx);
		try {
			tx.wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return hash;
	}
	
}
