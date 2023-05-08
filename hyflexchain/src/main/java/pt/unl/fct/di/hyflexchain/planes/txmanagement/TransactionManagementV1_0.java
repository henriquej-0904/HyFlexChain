package pt.unl.fct.di.hyflexchain.planes.txmanagement;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

/**
 * An implementation of the TransactionManagement Interface
 * in which corresponds to the V1_0 version of SystemVersion,
 * i.e., there is only one consensus mechanism and
 * there are no smart contracts.
 */
public class TransactionManagementV1_0 implements TransactionManagement
{
	private static final Logger LOGGER = LogManager.getLogger();

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

	@Override
	public String dispatchTransaction(HyFlexChainTransaction tx) throws InvalidTransactionException {
		verifyTx(tx);
		// TODO: add tx to tx pool. 
	}
	
}
