package me.chancesd.pvpmanager.utils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

/**
 * Utility class that adds functionality to java's Duration
 * <br>
 * Since we're forced to use java 8 for now, this class replicates some methods added in java 9
 *
 */
public class NCDuration {

	private static final int HOURS_PER_DAY = 24;
	private static final int MINUTES_PER_HOUR = 60;
	private static final int SECONDS_PER_MINUTE = 60;
	private static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
	private static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;
	private final Duration duration;

	private NCDuration(final Duration duration) {
		this.duration = duration;
	}

	public static NCDuration between(final Temporal startInclusive, final Temporal endExclusive) {
		return new NCDuration(Duration.between(startInclusive, endExclusive));
	}

	public int toDaysPart() {
		return (int) (duration.getSeconds() / SECONDS_PER_DAY);
	}

	public int toHoursPart() {
		return (int) (duration.toHours() % 24);
	}

	public int toMinutesPart() {
		return (int) (duration.toMinutes() % MINUTES_PER_HOUR);
	}

	public int toSecondsPart() {
		return (int) (duration.getSeconds() % SECONDS_PER_MINUTE);
	}

	public int toMillisPart() {
		return duration.getNano() / 1000_000;
	}

	public NCDuration plusMillis(final long milis) {
		return new NCDuration(duration.plusMillis(milis));
	}

	public int get(final ChronoUnit unit) {
		switch (unit) {
		case DAYS:
			return toDaysPart();
		case HOURS:
			return toHoursPart();
		case MILLIS:
			return toMillisPart();
		case MINUTES:
			return toMinutesPart();
		case SECONDS:
			return toSecondsPart();
		default:
			return 0;
		}
	}

	public Duration getDuration() {
		return duration;
	}

}
