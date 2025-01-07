package me.chancesd.pvpmanager.storage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.chancesd.sdutils.database.DatabaseConfigBuilder.DatabaseType;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.pvpmanager.player.CombatPlayer;

public interface Storage {

	public boolean userExists(final UUID uuid);

	public default boolean userExists(final CombatPlayer player) {
		return userExists(player.getUUID());
	}

	public Map<String, Object> getUserData(final UUID uuid);

	public default Map<String, Object> getUserData(final CombatPlayer player) {
		return getUserData(player.getUUID());
	}

	public void removeUserData(final UUID uuid);

	public default void removeUserData(final CombatPlayer player) {
		removeUserData(player.getUUID());
	}

	public boolean saveUserData(UUID uuid, Map<String, Object> userData);

	public default void saveUserData(final CombatPlayer player) {
		final long start = System.nanoTime();
		saveUserData(player.getUUID(), player.getUserData());
		Log.debug("Finished saving " + player + " - " + (System.nanoTime() - start) / 1_000_000D + " ms");
	}

	public List<Map<String, Object>> getAllUserData();

	public void increment(final String field, final UUID uuid);

	public Map<String, Object> getWorldData(String name);

	public boolean saveWorldData(String name, Map<String, Object> worldData);

	public void shutdown();

	public DatabaseType getDatabaseType();

}
