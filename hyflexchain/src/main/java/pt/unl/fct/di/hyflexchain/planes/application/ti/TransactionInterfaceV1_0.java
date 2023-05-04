package pt.unl.fct.di.hyflexchain.planes.application.ti;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction.Version;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.TransactionManagement;

/**
 * A transaction interface implementation in ehich transactions
 * do not have a smart contract.
 */
public class TransactionInterfaceV1_0 implements TransactionInterface {

	@Override
	public String sendTransaction(HyFlexChainTransaction tx) throws InvalidTransactionException
	{
		HyFlexChainTransaction.Version version;
		try {
			version = HyFlexChainTransaction.Version.valueOf(tx.getVersion());
		} catch (Exception e) {
			throw InvalidTransactionException.invalidVersion(tx.getVersion());
		}

		if (version != Version.V1_0)
			throw InvalidTransactionException.invalidVersion(tx.getVersion());

		if (tx.getAddress() == null || tx.getHash() == null ||
			tx.getSignatureType() == null || tx.getSignature() == null)
			throw new InvalidTransactionException("At least one field is null");
		
		return TransactionManagement.getInstance().dispatchTransaction(tx);	
	}
	
}
