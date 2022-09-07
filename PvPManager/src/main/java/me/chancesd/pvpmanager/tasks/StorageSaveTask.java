package me.chancesd.pvpmanager.tasks;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Utils.Log;
import me.chancesd.pvpmanager.storage.Storage;

public class StorageSaveTask implements Runnable {

	private final PvPManager plugin;
	private final Storage storage;

	public StorageSaveTask(final PvPManager plugin, final Storage storage) {
		this.plugin = plugin;
		this.storage = storage;
	}

	@Override
	public void run() {
		final long start = System.currentTimeMillis();
		Log.debug("Saving all player data to storage");
		plugin.getPlayerHandler().getPlayers().values().forEach(storage::saveUserData);
		Log.debug("Finished saving all player data" + " - " + (System.currentTimeMillis() - start) + " ms");
	}
}
