package pt.unl.fct.di.hyflexchain.planes.application.lvi;

import java.util.function.IntPredicate;

import pt.unl.fct.di.hyflexchain.util.TimeInterval;

public class BlockFilter {
	
	private Type type;

	private Object value;

	//TODO: check block filter

	/**
	 * 
	 */
	public BlockFilter() {
	}



	/**
	 * The type of each filter value
	 */
	public static enum Type
	{
		/**
		 * The last N blocks
		 */
		LAST_N(Integer.class),

		/**
		 * Blocks between a time interval
		 */
		TIME_INTERVAL(TimeInterval.class),

		/**
		 * Blocks that contain transactions filtered by value.
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



	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}



	/**
	 * @param type the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}



	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}



	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	public void setFilter(Type t, Object value)
	{
		if (! t.filterType.isInstance(value) )
			throw new IllegalArgumentException("Invalid filter type or value.");

		this.type = t;
		this.value = value;
	}

	public boolean isFilterValid()
	{
		if (type == null)
			return false;

		return type.filterType.isInstance(value);
	}
}
