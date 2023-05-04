package pt.unl.fct.di.hyflexchain.planes.txmanagement;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

/**
 * An implementation of the TransactionManagement Interface
 * in which corresponds to the V1_0 version of SystemVersion,
 * i.e., there is only one consensus mechanism and
 * there are no smart contracts.
 */
public class TransactionManagementV1_0 implements TransactionManagement {

	@Override
	public String dispatchTransaction(HyFlexChainTransaction tx) {

		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'dispatchTransaction'");
	}
	
}
