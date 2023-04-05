package me.NoChance.PvPManager.Utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import me.NoChance.PvPManager.Settings.Messages;

public final class TimeUtil {

	private static final ChronoUnit[] types = new ChronoUnit[] { ChronoUnit.DAYS, ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS };

	private TimeUtil() {
	}

	public static String getDiffMsg(final long date) {
		return TimeUtil.getDiffMsg(Instant.now(), Instant.ofEpochMilli(date));
	}

	public static String getDiffMsg(final Instant from, final Instant to) {
		boolean future = false;
		if (to.equals(from))
			return Messages.getTime(ChronoUnit.FOREVER);
		if (to.isAfter(from)) {
			future = true;
		}
		final NCDuration duration = NCDuration.between(from, to).plusMillis(future ? 50 : -50);
		final StringBuilder sb = new StringBuilder();
		int accuracy = 0;
		for (int i = 0; i < types.length; i++) {
			if (accuracy > 2) {
				break;
			}
			final int value = duration.get(types[i]);
			if (value > 0) {
				accuracy++;
				sb.append(" ").append(value).append(" ").append(Messages.getTime(types[i]));
			}
		}
		if (sb.length() == 0)
			return Messages.getTime(ChronoUnit.FOREVER);
		return sb.toString().trim();
	}

}
