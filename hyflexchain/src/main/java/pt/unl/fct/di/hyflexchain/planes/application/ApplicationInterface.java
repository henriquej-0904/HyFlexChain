package pt.unl.fct.di.hyflexchain.planes.application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.application.scmi.SmartContractManagementInterface;
import pt.unl.fct.di.hyflexchain.planes.application.ti.TransactionInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusPlaneConfig;
import pt.unl.fct.di.hyflexchain.planes.data.DataPlane;
import pt.unl.fct.di.hyflexchain.planes.txmanagement.TransactionManagement;
import pt.unl.fct.di.hyflexchain.util.ResetInterface;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;
import pt.unl.fct.di.hyflexchain.util.stats.BlockStats;

/**
 * This class is responsible for separating the HyFlexChain internals from
 * the application that is using it.
 * The creation of an object of this class initializes
 * the HyFlexChain System. <p>
 * 
 * There must be only one instance of this class per java process.
 */
public class ApplicationInterface implements ResetInterface
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationInterface.class);

	protected final LedgerViewInterface lvi;

	protected final TransactionInterface ti;

	protected final SmartContractManagementInterface scmi;

	protected final MultiLedgerConfig config;

	private final ConsensusPlaneConfig consensusPlaneConfig;

	/**
	 * Initialize the HyFLexChain System.
	 * @param configFolder A directory where to find the
	 * configuration files for HyFlexChain
	 * @param overridenConfigs an array of options to override the configurations
	 * @see MultiLedgerConfig    
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ParseException
	 */
	public ApplicationInterface(File configFolder, String[] overridenConfigs) throws FileNotFoundException, IOException, ParseException
	{
		this.config = MultiLedgerConfig.init(configFolder, overridenConfigs);

		this.lvi = LedgerViewInterface.getInstance();
		this.ti = TransactionInterface.getInstance();
		this.scmi = SmartContractManagementInterface.getInstance();

		// init consensus plane
		this.consensusPlaneConfig = new ConsensusPlaneConfig(config);
		this.consensusPlaneConfig.init();
	}

	/**
	 * @return the lvi
	 */
	public LedgerViewInterface getLvi() {
		return lvi;
	}

	/**
	 * @return the ti
	 */
	public TransactionInterface getTi() {
		return ti;
	}

	/**
	 * @return the scmi
	 */
	public SmartContractManagementInterface getScmi() {
		return scmi;
	}	

	/**
	 * @return the config
	 */
	public MultiLedgerConfig getConfig() {
		return config;
	}

	@Override
	public synchronized void reset() {
		LOGGER.info("Resetting state...");
		BlockStats.reset();
		((ResetInterface) DataPlane.getInstance()).reset();
		((ResetInterface) TransactionManagement.getInstance()).reset();
		((ResetInterface) this.lvi).reset();
		((ResetInterface) this.consensusPlaneConfig).reset();
		LOGGER.info("Reset complete with success!");
	}
}
