package me.chancesd.pvpmanager.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.chancesd.sdutils.utils.Log;
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
		final Map<UUID, PvPlayer> players = new HashMap<>(plugin.getPlayerHandler().getPlayers());

		storage.saveUserDataBatch(players.values().stream().filter(p -> p.isLoaded()).collect(Collectors.toList()));
		Log.debug("Finished saving data for " + players.size() + " players - " + (System.nanoTime() - start) / 1_000_000D + " ms");
	}
}
