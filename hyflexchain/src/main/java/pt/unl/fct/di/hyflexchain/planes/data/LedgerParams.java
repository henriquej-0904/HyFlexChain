package pt.unl.fct.di.hyflexchain.planes.data;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusType;

/**
 * The parameters for the ledger.
 */
public class LedgerParams
{
	/**
	 * Create a Ledger params object
	 */
	public LedgerParams() {
	}

	/**
	 * Max number of parallel chains for a specific consensus mechanism.
	 * 
	 * @param maxChains The max number of chains
	 * @param consensusType The type of consensus
	 */
	public static record MaxParallelChains(int maxChains, ConsensusType consensusType) {}

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
