package me.chancesd.pvpmanager.storage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.NoChance.PvPManager.PvPlayer;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.pvpmanager.storage.DatabaseConfigBuilder.DatabaseType;

public interface Storage {

	public boolean userExists(final UUID uuid);

	public default boolean userExists(final PvPlayer player) {
		return userExists(player.getUUID());
	}

	public Map<String, Object> getUserData(final UUID uuid);

	public default Map<String, Object> getUserData(final PvPlayer player) {
		return getUserData(player.getUUID());
	}

	public void removeUserData(final UUID uuid);

	public default void removeUserData(final PvPlayer player) {
		removeUserData(player.getUUID());
	}

	public boolean saveUserData(UUID uuid, Map<String, Object> userData);

	public default void saveUserData(final PvPlayer player) {
		final long start = System.nanoTime();
		saveUserData(player.getUUID(), player.getUserData());
		Log.debug("Finished saving " + player + " - " + (System.nanoTime() - start) / 1_000_000D + " ms");
	}

	public List<Map<String, Object>> getAllUserData();

	public void saveUserDataBatch(final Collection<PvPlayer> players);

	public void increment(final String field, final UUID uuid);

	public Map<String, Object> getWorldData(String name);

	public boolean saveWorldData(String name, Map<String, Object> worldData);

	public void shutdown();

	public DatabaseType getDatabaseType();

}
