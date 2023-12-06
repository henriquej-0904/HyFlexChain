package pt.unl.fct.di.hyflexchain.planes.data;

import java.util.function.IntPredicate;

import pt.unl.fct.di.hyflexchain.util.TimeInterval;

/**
 * A Filter for Transactions used in the Data Service Plane
 */
public class TransactionFilter {
	/**
	 * Create a filter
	 */
	public TransactionFilter() {
		//super(TransactionFilter.Type.class);
	}

	/**
	 * The type of each filter value
	 */
	public static enum Type
	{
		/**
		 * The last N transactions
		 */
		LAST_N(Integer.class),

		/**
		 * Transaction between a time interval
		 */
		TIME_INTERVAL(TimeInterval.class),

		/**
		 * Transactions filtered by value
		 */
		VALUE(IntPredicate.class);


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
