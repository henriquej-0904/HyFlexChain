package pt.unl.fct.di.hyflexchain.planes.txmanagement;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.EnumMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.InvalidAddressException;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.wrapper.TxWrapper;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.txpool.TxPool;

/**
 * An implementation of the TransactionManagement Interface
 * in which corresponds to the V1_0 version of SystemVersion,
 * i.e., there is only one consensus mechanism (PoW) and
 * there are no smart contracts.
 */
public class TransactionManagementV1_0 implements TransactionManagement
{
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManagementV1_0.class);

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
	@Override
	public void verifyTx(HyFlexChainTransaction tx) throws InvalidTransactionException
	{
		try {
			if (! tx.verifySignature())
			{
				var msg = "Transaction invalid signature";
				LOGGER.info(msg);
				throw new InvalidTransactionException(msg);
			}
				
		} catch (InvalidAddressException | InvalidKeyException |
				NoSuchAlgorithmException | SignatureException e) {
			LOGGER.info(e.getMessage());
			throw new InvalidTransactionException(e.getMessage(), e);
		}
	}

	/**
	 * Check the result of the add pending tx operation.
	 * @param result The result of the add pending tx operation
	 * @throws InvalidTransactionException If the transaction was already submitted.
	 */
	protected void checkAddPendingTx(boolean result) throws InvalidTransactionException
	{
		if ( ! result )
		{
			var msg = "Invalid Transaction: Already submitted.";
			LOGGER.info(msg);
			throw new InvalidTransactionException(msg);
		}
	}

	@Override
	public String dispatchTransaction(TxWrapper tx) throws InvalidTransactionException
	{
		verifyTx(tx.tx());
		checkAddPendingTx(getTxPool(ConsensusMechanism.PoW).addTxIfAbsent(tx.serializedTx()));

		LOGGER.info("Submited tx: " + tx.txHash());
		return tx.txHash().toHexString();
	}

	@Override
	public String dispatchTransactionAndWait(TxWrapper tx) throws InvalidTransactionException
	{
		verifyTx(tx.tx());

		try {
			checkAddPendingTx(getTxPool(ConsensusMechanism.PoW)
				.addTxIfAbsentAndWait(tx.serializedTx(), LOGGER));
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		}

		LOGGER.info("Finalized tx: " + tx.txHash());
		return tx.txHash().toHexString();
	}
	
}
