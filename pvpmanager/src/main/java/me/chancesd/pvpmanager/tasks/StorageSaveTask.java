package me.chancesd.pvpmanager.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.chancesd.sdutils.utils.Log;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
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
		final long start = System.nanoTime();
		Log.debug("Saving all player data to storage asynchronously");
		final Map<UUID, CombatPlayer> players = new HashMap<>(plugin.getPlayerHandler().getPlayers());
		players.values().forEach(storage::saveUserData);
		Log.debug("Finished saving all player data" + " - " + (System.nanoTime() - start) / 1_000_000D + " ms");
	}
}
