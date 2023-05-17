package pt.unl.fct.di.hyflexchain.api.rest.impl.server.resources;

import jakarta.inject.Singleton;
import jakarta.ws.rs.BadRequestException;
import pt.unl.fct.di.hyflexchain.api.rest.TransactionInterfaceRest;
import pt.unl.fct.di.hyflexchain.planes.application.ApplicationInterface;
import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

@Singleton
public class HyFlexChainResource implements TransactionInterfaceRest {
	
	private static ApplicationInterface hyflexchainInterface;

	/**
	 * @param hyflexchainInterface the hyflexchainInterface to set
	 */
	public static void setHyflexchainInterface(ApplicationInterface hyflexchainInterface) {
		HyFlexChainResource.hyflexchainInterface = hyflexchainInterface;
	}

	@Override
	public String sendTransactionAndWait(HyFlexChainTransaction tx)
	{
		try {
			return hyflexchainInterface.getTi().sendTransactionAndWait(tx);
		} catch (InvalidTransactionException e) {
			throw new BadRequestException(e.getMessage(), e);
		}
	}

	

}
