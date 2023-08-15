package pt.unl.fct.di.hyflexchain.planes.application.ti;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction.Version;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.wrapper.TxWrapper;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.TransactionManagement;

/**
 * A transaction interface implementation in which transactions
 * must have a smart contract.
 */
public class TransactionInterfaceV2_0 implements TransactionInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionInterfaceV1_0.class);

    @Override
	public void verifyTx(HyFlexChainTransaction tx) throws InvalidTransactionException
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

		if (tx.getSender() == null ||
			tx.getSignatureType() == null || tx.getSignature() == null
            || tx.getSmartContract() == null || tx.getTransactionType() == null || tx.getData() == null)
		{
			var exc = new InvalidTransactionException("At least one field is null");
			LOGGER.info(exc.getMessage());
			throw exc;
		}
	}

	@Override
	public String sendTransaction(TxWrapper tx) throws InvalidTransactionException
	{
		verifyTx(tx.tx());
		return TransactionManagement.getInstance().dispatchTransaction(tx);	
	}

	@Override
	public String sendTransactionAndWait(TxWrapper tx) throws InvalidTransactionException
	{
		verifyTx(tx.tx());
		return TransactionManagement.getInstance().dispatchTransactionAndWait(tx);
	}
	
}
