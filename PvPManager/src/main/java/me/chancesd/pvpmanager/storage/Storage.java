package me.chancesd.pvpmanager.storage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Utils.Log;
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

	public void saveUserData(UUID uuid, Map<String, Object> userData);

	public default void saveUserData(final PvPlayer player) {
		final long start = System.currentTimeMillis();
		Log.debug("Started async player save for " + player);
		saveUserData(player.getUUID(), player.getUserData());
		Log.debug("Finished saving " + player + " - " + (System.currentTimeMillis() - start) + " ms");
	}

	public List<Map<String, Object>> getAllUserData();

	public void increment(final String field, final UUID uuid);

	public void shutdown();

	public DatabaseType getDatabaseType();

}
