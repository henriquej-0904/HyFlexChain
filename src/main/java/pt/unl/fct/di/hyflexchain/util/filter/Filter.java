package pt.unl.fct.di.hyflexchain.util.filter;

import java.util.EnumMap;

/**
 * Represents a Filter object used to group many filters when calling a function.
 * This filter is based on the EnumMap implementation.
 */
public class Filter<K extends Enum<K>, V> extends EnumMap<K, V> {

	public Filter(Class<K> type)
	{
		super(type);
	}

	/**
	 * @param m
	 */
	public Filter(EnumMap<K, ? extends V> m) {
		super(m);
	}
	
	
}
