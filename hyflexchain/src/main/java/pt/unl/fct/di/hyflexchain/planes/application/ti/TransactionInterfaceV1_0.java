package pt.unl.fct.di.hyflexchain.planes.application.ti;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction.Version;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.TransactionManagement;

/**
 * A transaction interface implementation in ehich transactions
 * do not have a smart contract.
 */
public class TransactionInterfaceV1_0 implements TransactionInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionInterfaceV1_0.class.getSimpleName());


	protected void verifyTx(HyFlexChainTransaction tx) throws InvalidTransactionException
	{
		HyFlexChainTransaction.Version version;
		try {
			version = HyFlexChainTransaction.Version.valueOf(tx.getVersion());
		} catch (Exception e) {
			var exc = InvalidTransactionException.invalidVersion(tx.getVersion());
			LOGGER.info(exc.getMessage());
			throw exc;
		}

		if (version != Version.V1_0)
		{
			var exc = InvalidTransactionException.invalidVersion(tx.getVersion());
			LOGGER.info(exc.getMessage());
			throw exc;
		}

		if (tx.getAddress() == null || tx.getHash() == null ||
			tx.getSignatureType() == null || tx.getSignature() == null)
		{
			var exc = new InvalidTransactionException("At least one field is null");
			LOGGER.info(exc.getMessage());
			throw exc;
		}
	}

	@Override
	public String sendTransaction(HyFlexChainTransaction tx) throws InvalidTransactionException
	{
		verifyTx(tx);
		return TransactionManagement.getInstance().dispatchTransaction(tx);	
	}

	@Override
	public String sendTransactionAndWait(HyFlexChainTransaction tx) throws InvalidTransactionException
	{
		verifyTx(tx);
		return TransactionManagement.getInstance().dispatchTransactionAndWait(tx);
	}
	
}
