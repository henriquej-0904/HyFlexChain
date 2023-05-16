package pt.unl.fct.di.hyflexchain.util.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.util.SystemVersion;

/**
 * The parameters for the ledger.
 */
public class MultiLedgerConfig
{
	public static final String DEFAULT_GENERAL_CONFIG = "hyflexchain-general-config";

	public static final EnumMap<ConsensusMechanism, String> DEFAULT_CONSENSUS_CONFIG =
		Stream.of(ConsensusMechanism.values()).collect(Collectors.toMap(
			(c) -> c,
			(c) -> String.format("hyflexchain-%s-config", c.toString().toLowerCase()),
			(c1, c2) -> c1,
			() -> new EnumMap<>(ConsensusMechanism.class)
		));

	public static final String DEFAULT_FILE_GENERAL_CONFIG = DEFAULT_GENERAL_CONFIG + ".properties";

	public static final EnumMap<ConsensusMechanism, String> DEFAULT_FILE_CONSENSUS_CONFIG =
		DEFAULT_CONSENSUS_CONFIG.entrySet().stream()
			.map((entry) -> Map.entry(entry.getKey(), entry.getValue() + ".properties"))
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				Map.Entry::getValue,
				(c1, c2) -> c1,
				() -> new EnumMap<>(ConsensusMechanism.class)
			));

	protected static MultiLedgerConfig config;

	/**
	 * Initialize the config with the specified config folder
	 * @param configFolder
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static MultiLedgerConfig init(File configFolder) throws FileNotFoundException, IOException
	{
		var generalConfig = getProperties(new File(configFolder, DEFAULT_FILE_GENERAL_CONFIG));
		EnumMap<ConsensusMechanism, Properties> consensusProps = new EnumMap<>(ConsensusMechanism.class);

		for (ConsensusMechanism consensus :
				MultiLedgerConfig.getActiveConsensusMechanisms(generalConfig))
		{
			var consensusFolder = new File(configFolder, consensus.toString().toLowerCase());
			var props = getProperties(new File(consensusFolder, DEFAULT_FILE_CONSENSUS_CONFIG.get(consensus)));
			consensusProps.put(consensus, props);
		}

		return init(generalConfig, consensusProps);
	}

	

	protected static Properties getProperties(File file) throws FileNotFoundException, IOException
	{
		Properties props = new Properties();

		try (var in = new FileInputStream(file)) {
			props.load(in);
		}
		
		return props;
	}

	/**
	 * Initialize with the specified configurations.
	 * 
	 * @param generalConfig A general config of the ledger
	 * @param configsPerConsensusType A config
	 * for each consensus type.
	 */
	public static MultiLedgerConfig init(Properties generalConfig,
		EnumMap<ConsensusMechanism, Properties> configsPerConsensusType)
	{
		config = new MultiLedgerConfig(generalConfig, configsPerConsensusType);
		return config;
	}

	/**
	 * Initialize with the specified configurations.
	 * 
	 * @param generalConfig A general config of the ledger
	 */
	public static void init(Properties generalConfig)
	{
		init(generalConfig, new EnumMap<>(ConsensusMechanism.class));
	}

	/**
	 * Get the config instance.
	 * @return The config instance.
	 */
	public static MultiLedgerConfig getInstance()
	{
		return config;
	}

	public static EnumSet<ConsensusMechanism> getActiveConsensusMechanisms(Properties props)
	{
		String consensusStringList = props.getProperty(GENERAL_CONFIG.ACTIVE_CONSENSUS.toString());
			
		if (consensusStringList == null)
			throw new Error("Configuration: ACTIVE_CONSENSUS is required!");

		String[] consensusList = consensusStringList.split(";");
		EnumSet<ConsensusMechanism> consensusSet = EnumSet.noneOf(ConsensusMechanism.class);
		
		try {
			for (String consensus : consensusList) {
				consensusSet.add(ConsensusMechanism.valueOf(consensus));
			}
		} catch (Exception e) {
			throw new Error("Configuration: ACTIVE_CONSENSUS is required!");
		}

		return consensusSet;
	}

	protected Properties generalConfig;

	protected EnumMap<ConsensusMechanism, Properties> configsPerConsensusType;

	protected EnumMap<ConsensusMechanism, LedgerConfig> ledgerConfigs;

	/**
	 * Create a Ledger config object
	 * 
	 * @param generalConfig A general config of the ledger
	 * @param configsPerConsensusType A config
	 * for each consensus type.
	 */
	protected MultiLedgerConfig(Properties generalConfig,
		EnumMap<ConsensusMechanism, Properties> configsPerConsensusType)
	{
		this.generalConfig = generalConfig;
		this.configsPerConsensusType = configsPerConsensusType;
		this.ledgerConfigs = Stream.of(ConsensusMechanism.values())
			.collect(
				Collectors.toMap(
					(c) -> c,
					(c) -> new LedgerConfig(this, c),
					(c1, c2) -> c1,
					() -> new EnumMap<ConsensusMechanism, LedgerConfig>(ConsensusMechanism.class)
				)
			);
	}

	/**
	 * Override the current configuration by the specified array
	 * of key=value pairs. <p>
	 * For each consensus mechanism, their configurations
	 * must comply with the property pattern, for example: <p>
	 * -for general configs: {@code -Generalkey=value} <p>
	 * -for PoW configs:
	 * {@code -PoWkey=value}
	 * @param configs the configs to override the current ones.
	 * @throws ParseException
	 */
	public void addOverridenConfigs(String[] configs) throws ParseException
	{
		if (configs.length == 0)
			return;

		CommandLineParser parser = new DefaultParser();
		final Options ops = new Options();

		final Option generalOp = Option.builder("General")
			.hasArgs().valueSeparator('=').build();
		ops.addOption(generalOp);

		for (var consensus : ConsensusMechanism.values()) {
			var op = Option.builder(consensus.toString().toLowerCase())
				.hasArgs().valueSeparator('=').build();
			ops.addOption(op);
		}

		CommandLine cmd = parser.parse(ops, configs);
		
		this.generalConfig = new Properties(this.generalConfig);
		this.generalConfig.putAll(cmd.getOptionProperties(generalOp));

		for (var consensus : ConsensusMechanism.values()) {
			var defaultProps = this.configsPerConsensusType.get(consensus);
			var overridenProps = cmd.getOptionProperties(consensus.toString().toLowerCase());
			Properties props = new Properties(defaultProps);
			props.putAll(overridenProps);

			this.configsPerConsensusType.put(consensus, props);
		}
	}

	/**
	 * Get the Ledger config for the specified consensus mechanism
	 * @param consensus The consensus mechanism
	 * @return The Ledger config for the specified consensus mechanism
	 */
	public LedgerConfig getLedgerConfig(ConsensusMechanism consensus)
	{
		return this.ledgerConfigs.get(consensus);
	}

	/**
	 * Get the value of the specified key
	 * @param key The key to search for the value
	 * @return The value associated with the specified key or null.
	 */
	public String getConfigValue(String key)
	{
		return this.generalConfig.getProperty(key);
	}

	/**
	 * Get the value of the specified key
	 * @param key The key to search for the value
	 * @return The value associated with the specified key or null.
	 */
	public String getConfigValue(MultiLedgerConfig.GENERAL_CONFIG key)
	{
		return this.generalConfig.getProperty(key.toString());
	}

	/**
	 * Get the value of the specified key in the
	 * context of a specific consensus mechanism.
	 * If this key is not defined in the configuration
	 * of the specified consensus, then the search is performed
	 * in the general configurations.
	 * @param key The key to search for the value
	 * @param consensus The consensus mechanism
	 * @return The value associated with the specified key or null.
	 */
	public String getConfigValue(String key, ConsensusMechanism consensus)
	{
		var configs = this.configsPerConsensusType.get(consensus);
		if (configs == null)
			return this.generalConfig.getProperty(key);

		String res = configs.getProperty(key);
		return res != null ? res : this.generalConfig.getProperty(key);
	}

	public SystemVersion getSystemVersion()
	{
		try {
			return SystemVersion.parseSystemVersion(getConfigValue(GENERAL_CONFIG.SYSTEM_VERSION));
		} catch (Exception e) {
			throw new Error("Configuration: SYSTEM_VERSION is required!");
		}
	}

	public EnumSet<ConsensusMechanism> getActiveConsensusMechanisms()
	{
		return EnumSet.copyOf(this.ledgerConfigs.keySet());
	}

	/**
	 * An enum of some General configurations and their type.
	 */
	public static enum GENERAL_CONFIG
	{
		/**
		 * The system version
		 */
		SYSTEM_VERSION,

		ACTIVE_CONSENSUS,

		/**
		 * The type of data storage of the ledger
		 */
		LEDGER_DB_TYPE;

		/**
		 * Convert to a value from string to the corresponding type.
		 * @return The converted value
		 */
		public LEDGER_DB_TYPE_VALUES getLedgerDbTypeValue()
		{
			try {
				return LEDGER_DB_TYPE_VALUES.valueOf(MultiLedgerConfig.getInstance().getConfigValue(LEDGER_DB_TYPE));
			} catch (Exception e) {
				throw new Error(String.format("The config parameter %s is not defined or it has " +
				"an incorrect value.", LEDGER_DB_TYPE), e);
			}
		}
	}

	/**
	 * The possible values for the LEDGER_DB_TYPE config.
	 */
	public static enum LEDGER_DB_TYPE_VALUES
	{
		/**
		 * The ledger operates in memory.
		 */
		IN_MEMORY
	}


	/**
	 * Max number of parallel chains for a specific consensus mechanism.
	 * 
	 * @param maxChains The max number of chains
	 * @param consensusType The type of consensus
	 */
	public static record MaxParallelChains(int maxChains, ConsensusMechanism consensusType) {}

	/**
	 * The type of each parameter value
	 */
	public static enum Type
	{
		/**
		 * Max number of parallel chains for a specific consensus mechanism.
		 */
		MAX_PARALLEL_CHAINS(MaxParallelChains.class);

		/**
		 * The type of the filter
		 */
		private final Class<?> filterType;

		/**
		 * Create a type
		 * @param filterType the type of the filter
		 */
		private Type(Class<?> filterType)
		{
			this.filterType = filterType;
		}

		/**
		 * The type of the value of the filter.
		 * @return The type of the value of the filter.
		 */
		public Class<?> getFilterType() {
			return filterType;
		}

	}
}
