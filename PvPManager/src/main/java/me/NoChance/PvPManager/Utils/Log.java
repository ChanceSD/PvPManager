package me.NoChance.PvPManager.Utils;

import java.util.logging.Logger;

import me.NoChance.PvPManager.Settings.Settings;

public final class Log {

	private static Logger logger;

	private Log() {
	}

	public static void setup(final Logger log) {
		Log.logger = log;
	}

	public static void info(final String message) {
		logger.info(message);
	}

	public static void severe(final String message) {
		logger.severe(message);
	}

	public static void warning(final String message) {
		logger.warning(message);
	}

	public static void debug(final String message) {
		if (Settings.DEBUG) {
			logger.info(message);
		}
	}

}
