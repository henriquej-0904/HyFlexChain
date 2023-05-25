package pt.unl.fct.di.hyflexchain.planes.application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.cli.ParseException;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.application.ti.TransactionInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusPlaneConfig;
import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;

/**
 * This class is responsible for separating the HyFlexChain internals from
 * the application that is using it.
 * The creation of an object of this class initializes
 * the HyFlexChain System. <p>
 * 
 * There must be only one instance of this class per java process.
 */
public class ApplicationInterface
{
	protected final LedgerViewInterface lvi;

	protected final TransactionInterface ti;

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
		MultiLedgerConfig config = MultiLedgerConfig.init(configFolder);
		config.addOverridenConfigs(overridenConfigs);

		this.lvi = LedgerViewInterface.getInstance();
		this.ti = TransactionInterface.getInstance();

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

	
}
