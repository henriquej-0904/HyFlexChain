package pt.unl.fct.di.hyflexchain.util.config;

import java.util.EnumMap;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.util.SystemVersion;

/**
 * The parameters for the ledger.
 */
public class MultiLedgerConfig
{
	protected static MultiLedgerConfig config;

	/**
	 * Initialize with the specified configurations.
	 * 
	 * @param generalConfig A general config of the ledger
	 * @param configsPerConsensusType A config
	 * for each consensus type.
	 */
	public static void init(Properties generalConfig,
		EnumMap<ConsensusMechanism, Properties> configsPerConsensusType)
	{
		config = new MultiLedgerConfig(generalConfig, configsPerConsensusType);
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

	protected final Properties generalConfig;

	protected final EnumMap<ConsensusMechanism, Properties> configsPerConsensusType;

	protected final EnumMap<ConsensusMechanism, LedgerConfig> ledgerConfigs;

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
			return SystemVersion.valueOf(getConfigValue(GENERAL_CONFIG.SYSTEM_VERSION));
		} catch (Exception e) {
			throw new Error("Configuration: SYSTEM_VERSION is required!");
		}
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
