package me.chancesd.pvpmanager.managers;

import java.util.HashMap;
import java.util.Map;

import me.NoChance.PvPManager.PvPManager;
import me.chancesd.pvpmanager.world.CombatWorld;

public class WorldManager {

	private final PvPManager plugin;
	private final Map<String, CombatWorld> worlds = new HashMap<>();

	public WorldManager(final PvPManager plugin) {
		this.plugin = plugin;
	}

	public CombatWorld getWorld(final String name) {
		return worlds.get(name);
	}

}
