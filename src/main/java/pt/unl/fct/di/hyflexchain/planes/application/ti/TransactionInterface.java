package pt.unl.fct.di.hyflexchain.planes.application.ti;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

public interface TransactionInterface {
	
	void sendTransaction(HyFlexChainTransaction tx);

}
