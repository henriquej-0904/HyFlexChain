package pt.unl.fct.di.hyflexchain.planes.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Properties;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.application.ti.TransactionInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
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

	/**
	 * Initialize the HyFLexChain System.
	 * @param configFolder A directory where to find the
	 * configuration files for HyFlexChain.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public ApplicationInterface(File configFolder, ConsensusMechanism[] activeConsensus) throws FileNotFoundException, IOException
	{
		initConfig(configFolder, activeConsensus);

		this.lvi = LedgerViewInterface.getInstance();
		this.ti = TransactionInterface.getInstance();
	}

	protected void initConfig(File configFolder, ConsensusMechanism[] activeConsensus) throws FileNotFoundException, IOException
	{
		var generalConfig = getProperties(new File(configFolder, "hyflexchain-general-config.properties"));
		EnumMap<ConsensusMechanism, Properties> consensusProps = new EnumMap<>(ConsensusMechanism.class);

		for (ConsensusMechanism consensusMechanism : activeConsensus) {
			var consensus = consensusMechanism.toString().toLowerCase();
			var props = getProperties(new File(consensus, "hyflexchain-" + consensus + "-config.properties"));
			consensusProps.put(consensusMechanism, props);
		}

		MultiLedgerConfig.init(generalConfig, consensusProps);
	}

	protected Properties getProperties(File file) throws FileNotFoundException, IOException
	{
		Properties props = new Properties();

		try (var in = new FileInputStream(file)) {
			props.load(in);
		}
		
		return props;
	}
}
