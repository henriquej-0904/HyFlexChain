package pt.unl.fct.di.hyflexchain.planes.consensus.committees;

import java.util.Calendar;
import java.util.function.Predicate;

import pt.unl.fct.di.hyflexchain.util.TimeInterval;

/**
 * An interface to verify if a committee is still valid
 */
public interface CommitteeValidityPredicate extends Predicate<Committee> {
	
	/**
	 * Create a new CommitteeValidity based on the specified time interval.
	 * 
	 * @param interval The interval where the committee is valid.
	 * @return The CommitteeValidity object
	 */
	public static CommitteeValidityPredicate fromInterval(TimeInterval interval) {
		return new CommitteeValidityTimeInterval(interval);
	}

	/**
	 * An implementation of the CommitteeValidity Interface based on a time interval.
	 */
	public static class CommitteeValidityTimeInterval
		implements CommitteeValidityPredicate
	{

		private final TimeInterval interval;

		/**
		 * Create a new CommitteeValidity based on the specified time interval.
		 * @param interval The interval where the committee is valid.
		 */
		public CommitteeValidityTimeInterval(TimeInterval interval)
		{
			this.interval = interval;
		}

		@Override
		public boolean test(Committee c) {
			var now = Calendar.getInstance().getTimeInMillis();
			return now >= this.interval.from() &&
				now <= this.interval.to();
		}

		/**
		 * The valid interval of time for a committee.
		 * @return the interval
		 */
		public TimeInterval getInterval() {
			return interval;
		}
	}
}
