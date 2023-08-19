package pt.unl.fct.di.hyflexchain.api.rest.impl.server.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import jakarta.ws.rs.BadRequestException;
import pt.unl.fct.di.hyflexchain.api.rest.TransactionInterfaceRest;
import pt.unl.fct.di.hyflexchain.planes.application.ApplicationInterface;
import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.wrapper.TxWrapper;

@Singleton
public class HyFlexChainTI_Resource implements TransactionInterfaceRest {
	
	Logger LOG = LoggerFactory.getLogger(HyFlexChainTI_Resource.class);

	private static ApplicationInterface hyflexchainInterface;

	public HyFlexChainTI_Resource()
	{
		
	}

	/**
	 * @param hyflexchainInterface the hyflexchainInterface to set
	 */
	public static void setHyflexchainInterface(ApplicationInterface hyflexchainInterface) {
		HyFlexChainTI_Resource.hyflexchainInterface = hyflexchainInterface;
	}

	@Override
	public String sendTransactionAndWait(HyFlexChainTransaction tx)
	{
		try {
			return hyflexchainInterface.getTi().sendTransactionAndWait(TxWrapper.from(tx));
		} catch (InvalidTransactionException e) {
			e.printStackTrace();
			throw new BadRequestException(e.getMessage(), e);
		}
	}

	@Override
	public String sendTransactionAndWait(byte[] tx) {
		try {
			return hyflexchainInterface.getTi().sendTransactionAndWait(TxWrapper.from(tx));
		} catch (InvalidTransactionException e) {
			e.printStackTrace();
			throw new BadRequestException(e.getMessage(), e);
		}
	}
}
