package pt.unl.fct.di.hyflexchain.util;

import java.util.EnumMap;

/**
 * Represents a group of properties for filters or configuration parameters
 * where all the possible types are defined an Enum.
 */
public class TypedProperties<K extends Enum<K>> extends EnumMap<K, Object>
{
	public TypedProperties(Class<K> type)
	{
		super(type);
	}

	/**
	 * @param m
	 */
	public TypedProperties(EnumMap<K, ? extends Object> m) {
		super(m);
	}

	
}
