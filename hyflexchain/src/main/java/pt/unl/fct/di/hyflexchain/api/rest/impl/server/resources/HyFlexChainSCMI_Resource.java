package pt.unl.fct.di.hyflexchain.api.rest.impl.server.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import jakarta.ws.rs.BadRequestException;
import pt.unl.fct.di.hyflexchain.api.rest.SmartContractManagementInterfaceRest;
import pt.unl.fct.di.hyflexchain.planes.application.ApplicationInterface;
import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;

@Singleton
public class HyFlexChainSCMI_Resource implements SmartContractManagementInterfaceRest {

    Logger LOG = LoggerFactory.getLogger(HyFlexChainSCMI_Resource.class);

	private static ApplicationInterface hyflexchainInterface;

    public HyFlexChainSCMI_Resource()
    {

    }

    /**
	 * @param hyflexchainInterface the hyflexchainInterface to set
	 */
	public static void setHyflexchainInterface(ApplicationInterface hyflexchainInterface) {
		HyFlexChainSCMI_Resource.hyflexchainInterface = hyflexchainInterface;
	}

    @Override
    public String installSmartContract(byte[] contractCode) {
        try {
			return hyflexchainInterface.getScmi().installSmartContract(contractCode);
		} catch (InvalidTransactionException e) {
			LOG.error(e.getMessage(), e);
			throw new BadRequestException(e.getMessage(), e);
		}
    }

    @Override
    public String revokeSmartContract(Address contractAddress) {
        try {
			return hyflexchainInterface.getScmi().revokeSmartContract(contractAddress);
		} catch (InvalidTransactionException e) {
			LOG.info(e.getMessage());
			throw new BadRequestException(e.getMessage(), e);
		}
    }
    
}
