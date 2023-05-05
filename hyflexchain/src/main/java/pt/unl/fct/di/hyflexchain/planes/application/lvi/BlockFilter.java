package pt.unl.fct.di.hyflexchain.planes.application.lvi;

import java.util.function.IntPredicate;

import pt.unl.fct.di.hyflexchain.planes.data.block.BlockState;
import pt.unl.fct.di.hyflexchain.util.TimeInterval;

public class BlockFilter {
	

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
		 * The status of the block
		 */
		STATUS(BlockState.class),

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
}
