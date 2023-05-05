package pt.unl.fct.di.hyflexchain.util;

import java.util.Date;

/**
 * Represents a time interval
 * 
 * @param from from this point (in milliseconds)
 * @param to to this point (in milliseconds)
 */
public record TimeInterval(long from, long to) {
	
	/**
	 * Represents a time interval
	 * 
	 * @param from from this point (in milliseconds)
	 * @param to   to this point (in milliseconds)
	 */
	public TimeInterval(long from, long to)
	{
		if (from < 0 || to < 0)
			throw new IllegalArgumentException("A time interval cannot be defined by negative milliseconds.");

		if (to < from)
			throw new IllegalArgumentException("The 'to' field in Time Interval must not be before than the 'after' field.");

		this.from = from;
		this.to = to;
	}

	/**
	 * Construct a TimeInterval from Date objects
	 * @param from from this point
	 * @param to   to this point
	 * @return A new TimeInterval
	 */
	public static TimeInterval fromDates(Date from, Date to)
	{
		return new TimeInterval(from.getTime(), to.getTime());
	}

}
