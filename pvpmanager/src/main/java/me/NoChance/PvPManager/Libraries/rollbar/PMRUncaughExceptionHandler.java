package me.NoChance.PvPManager.Libraries.rollbar;

import java.lang.Thread.UncaughtExceptionHandler;

import me.chancesd.sdutils.utils.Log;

public class PMRUncaughExceptionHandler implements UncaughtExceptionHandler {

	@Override
	public void uncaughtException(final Thread t, final Throwable e) {
		Log.severe(e.getMessage(), e);
	}

}
