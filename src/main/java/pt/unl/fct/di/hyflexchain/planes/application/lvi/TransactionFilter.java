package pt.unl.fct.di.hyflexchain.planes.application.lvi;

import java.util.EnumMap;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.TransactionState;
import pt.unl.fct.di.hyflexchain.util.TimeInterval;
import pt.unl.fct.di.hyflexchain.util.filter.Filter;

/**
 * A Filter for Transactions used in the Ledger View Interface
 */
public class TransactionFilter extends Filter<TransactionFilter.Type, Object> {
	

	/**
	 * @param m
	 */
	public TransactionFilter(EnumMap<Type, ? extends Object> m) {
		super(m);
	}

	/**
	 * @param type
	 */
	public TransactionFilter() {
		super(TransactionFilter.Type.class);
	}

	/**
	 * The type of each filter value
	 */
	public enum Type
	{
		/**
		 * The status of the transaction
		 */
		STATUS(TransactionState.class),

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
		VALUE(Integer.class);


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
