package me.chancesd.pvpmanager.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.player.world.CombatWorld;
import me.chancesd.pvpmanager.player.world.CombatWorld.WorldOptionState;
import me.chancesd.pvpmanager.storage.Storage;
import me.chancesd.pvpmanager.storage.fields.WorldDataFields;
import me.chancesd.pvpmanager.utils.ScheduleUtils;
import me.chancesd.sdutils.utils.Log;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldManager {

	private final Map<String, CombatWorld> worlds = new HashMap<>();
	private final Storage storage;

	public WorldManager(final PvPManager plugin) {
		this.storage = plugin.getStorageManager().getStorage();
		loadWorlds();
	}

	private void loadWorlds() {
		for (final World world : Bukkit.getWorlds()) {
			loadWorld(world);
		}
	}

	@NotNull
	private CombatWorld loadWorld(final World world) {
		final Map<String, Object> worldData = storage.getWorldData(world.getName());
		CombatWorld combatWorld;
		if (worldData.isEmpty()) {
			combatWorld = createDefaultCombatWorld(world);
		} else {
			combatWorld = loadWorldData(world, worldData);
		}
		worlds.put(world.getName(), combatWorld);
		return combatWorld;
	}

	@NotNull
	private CombatWorld loadWorldData(final World world, final Map<String, Object> worldData) {
		boolean pvpState = true;
		WorldOptionState forcePVP = WorldOptionState.NONE;

		final Object pvpstate = worldData.get(WorldDataFields.PVP);
		if (pvpstate instanceof Integer) {
			pvpState = (int) pvpstate != 0;
		} else if (pvpstate instanceof Boolean) {
			pvpState = (boolean) pvpstate;
		}

		final Object forcePVPOption = worldData.get(WorldDataFields.FORCEDPVP);
		if (forcePVPOption instanceof final String string) {
			forcePVP = WorldOptionState.valueOf(string);
		}
		return new CombatWorld(world.getUID(), world.getName(), pvpState, forcePVP);
	}

	@NotNull
	private CombatWorld createDefaultCombatWorld(final World world) {
		return new CombatWorld(world.getUID(), world.getName(), true, WorldOptionState.NONE);
	}

	public void saveWorldData(final CombatWorld combatWorld) {
		ScheduleUtils.runAsync(() -> storage.saveWorldData(combatWorld.getName(), combatWorld.getWorldData()));
	}

	@NotNull
	public CombatWorld getWorld(final World world) {
		final CombatWorld combatWorld = getWorld(world.getName());
		if (combatWorld == null) {
			Log.infoColor(ChatColor.GREEN + "Loaded new combat world: " + ChatColor.GOLD + world.getName());
			return loadWorld(world);
		}
		return combatWorld;
	}

	@Nullable
	public CombatWorld getWorld(final String worldName) {
		return worlds.get(worldName);
	}

	public Collection<CombatWorld> getWorlds() {
		return worlds.values();
	}
}
