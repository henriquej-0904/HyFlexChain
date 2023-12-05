package pt.unl.fct.di.hyflexchain.api.rest.impl.server.resources.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import pt.unl.fct.di.hyflexchain.api.rest.settings.PrivateSettingsInterface;
import pt.unl.fct.di.hyflexchain.planes.application.ApplicationInterface;
import pt.unl.fct.di.hyflexchain.util.ResetInterface;

@Singleton
public class PrivateSettingsResource implements PrivateSettingsInterface {

    Logger LOG = LoggerFactory.getLogger(PrivateSettingsResource.class);

    private static ApplicationInterface hyflexchainInterface;

    /**
	 * @param hyflexchainInterface the hyflexchainInterface to set
	 */
	public static void setHyflexchainInterface(ApplicationInterface hyflexchainInterface) {
		PrivateSettingsResource.hyflexchainInterface = hyflexchainInterface;
	}

    public PrivateSettingsResource()
    {
        
    }

    @Override
    public void reset() {
        LOG.info("Received Reset request!");
        ((ResetInterface) hyflexchainInterface).reset();
    }
    
}
