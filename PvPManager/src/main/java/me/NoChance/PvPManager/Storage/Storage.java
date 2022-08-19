package me.NoChance.PvPManager.Storage;

import java.util.Map;
import java.util.UUID;

import me.NoChance.PvPManager.PvPlayer;

public interface Storage {

	public boolean userExists(final UUID uuid);

	public Map<String, Object> getUserData(final PvPlayer player);

	public void saveUser(final PvPlayer player);

	public void increment(final String toUpdate, final UUID uuid);

	public void shutDown();

}
