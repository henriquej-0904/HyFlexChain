package pt.unl.fct.di.hyflexchain.api.rest.impl.server.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import jakarta.ws.rs.BadRequestException;
import pt.unl.fct.di.hyflexchain.api.rest.TransactionInterfaceRest;
import pt.unl.fct.di.hyflexchain.planes.application.ApplicationInterface;
import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

@Singleton
public class HyFlexChainResource implements TransactionInterfaceRest {
	
	Logger LOG = LoggerFactory.getLogger(HyFlexChainResource.class.getSimpleName());

	private static ApplicationInterface hyflexchainInterface;

	public HyFlexChainResource()
	{
		
	}

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
			LOG.info(e.getMessage());
			throw new BadRequestException(e.getMessage(), e);
		}
	}

	

}
