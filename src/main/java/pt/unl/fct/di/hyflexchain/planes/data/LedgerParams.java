package pt.unl.fct.di.hyflexchain.planes.data;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusType;
import pt.unl.fct.di.hyflexchain.util.TypedProperties;

public class LedgerParams extends TypedProperties<LedgerParams.Type>
{
	public LedgerParams() {
		super(LedgerParams.Type.class);
	}

	/**
	 * Max number of parallel chains for a specific consensus mechanism.
	 */
	public static record MaxParallelChains(int n, ConsensusType consensusType) {}

	/**
	 * The type of each parameter value
	 */
	public static enum Type
	{
		/**
		 * Max number of parallel chains for a specific consensus mechanism.
		 */
		MAX_PARALLEL_CHAINS(MaxParallelChains.class);

		private final Class<?> filterType;

		private Type(Class<?> filterType)
		{
			this.filterType = filterType;
		}

		/**
		 * @return The type of the value of the filter.
		 */
		public Class<?> getFilterType() {
			return filterType;
		}

	}
}
