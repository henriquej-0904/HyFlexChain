package pt.unl.fct.di.hyflexchain.util.config;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;

/**
 * A configuration for a ledger with a specific consensus mechanism.
 */
public class LedgerConfig {
	
	/**
	 * All configurations.
	 */
	private final MultiLedgerConfig allConfigs;

	/**
	 * The consensus mechanism this ledger config corresponds to.
	 */
	public final ConsensusMechanism consensus;

	/**
	 * @param allConfigs
	 * @param consensus
	 */
	protected LedgerConfig(MultiLedgerConfig allConfigs, ConsensusMechanism consensus) {
		this.allConfigs = allConfigs;
		this.consensus = consensus;
	}

	
	/**
	 * Get the value of the specified key
	 * @param key The key to search for the value
	 * @return The value associated with the specified key or null.
	 */
	public String getConfigValue(String key)
	{
		return this.allConfigs.getConfigValue(key, this.consensus);
	}

	/**
	 * Get the value of the specified key
	 * @param key The key to search for the value
	 * @return The value associated with the specified key or null.
	 */
	public String getConfigValue(MultiLedgerConfig.GENERAL_CONFIG key)
	{
		return getConfigValue(key.toString());
	}

	/**
	 * The number of transactions inside a block.
	 * @return The number of transactions inside a block.
	 */
	public int getNumTxsInBlock()
	{
		try {
			return Integer.valueOf(getConfigValue(CONFIG.N_TXS_IN_BLOCK.toString()));
		} catch (Exception e) {
			throw new Error("Configuration: N_TXS_IN_BLOCK is required!");
		}
	}

	public static enum CONFIG
	{
		/**
		 * The number of transactions inside a block.
		 */
		N_TXS_IN_BLOCK
	}
	

}
