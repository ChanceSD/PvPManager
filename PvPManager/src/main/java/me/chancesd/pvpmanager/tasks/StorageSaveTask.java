package me.chancesd.pvpmanager.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
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
		Log.debug("Saving all player data to storage asynchronously");
		final Map<UUID, PvPlayer> players = new HashMap<>(plugin.getPlayerHandler().getPlayers());
		players.values().forEach(storage::saveUserData);
		Log.debug("Finished saving all player data" + " - " + (System.currentTimeMillis() - start) + " ms");
	}
}
