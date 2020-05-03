package me.NoChance.PvPManager.Utils;

import java.util.logging.Logger;

import me.NoChance.PvPManager.Settings.Settings;

public final class Log {

	private static Logger log;

	private Log() {
	}

	public static void setup(final Logger log) {
		Log.log = log;
	}

	public static void info(final String message) {
		log.info(message);
	}

	public static void severe(final String message) {
		log.severe(message);
	}

	public static void warning(final String message) {
		log.warning(message);
	}

	public static void debug(final String message) {
		if (Settings.DEBUG) {
			log.info(message);
		}
	}

}
