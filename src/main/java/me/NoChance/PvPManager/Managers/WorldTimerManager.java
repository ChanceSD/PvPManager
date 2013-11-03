package me.NoChance.PvPManager.Managers;

import java.util.HashMap;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPTimer;
import me.NoChance.PvPManager.Config.Variables;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class WorldTimerManager {

	private PvPManager plugin;
	private HashMap<String, PvPTimer> schedulers = new HashMap<String, PvPTimer>();

	public WorldTimerManager(PvPManager plugin) {
		this.plugin = plugin;
		enablePvPScheduler();
	}

	public PvPTimer getPvpTimer(World w) {
		return schedulers.get(w.getName().toLowerCase());
	}

	public PvPTimer getPvpTimer(String worldName) {
		return schedulers.get(worldName.toLowerCase());
	}

	public boolean isPvpTimerWorld(World w) {
		if (schedulers.containsKey(w.getName().toLowerCase()))
			return true;
		else
			return false;
	}
	
	public boolean isTimeForPvp(World w) {
		if (getPvpTimer(w).isPvpTime())
			return true;
		else
			return false;
	}

	public boolean isPvpTimerWorld(String worldName) {
		if (schedulers.containsKey(worldName.toLowerCase()))
			return true;
		else
			return false;
	}

	public void reloadPvpTimers() {
		for (PvPTimer a : schedulers.values()) {
			if (Variables.pvpTimerEnabled)
				a.reload();
			else if (!Variables.pvpTimerEnabled)
				a.cancelAllTasks();
		}
		if (!Variables.pvpTimerEnabled)
			schedulers.clear();
	}

	private void enablePvPScheduler() {
		for (World w : plugin.getServer().getWorlds()) {
			if (!Variables.worldsExcluded.contains(w.getName())) {
				if (plugin.getConfig().getConfigurationSection("PvP Timer." + w.getName()) == null) {
					ConfigurationSection world = plugin.getConfig().getConfigurationSection("PvP Timer").createSection(w.getName());
					world.set("Start PvP", 13000);
					world.set("End PvP", 0);
					if (Variables.announcePvpOnWorldChange) {
						world.set("On World Change.On", "&4PvP is currently enabled in " + w.getName());
						world.set("On World Change.Off", "&2PvP is currently disabled in " + w.getName());
					}
					plugin.saveConfig();
				}
				if (!plugin.getConfig().isSet("PvP Timer." + w.getName() + ".On World Change")) {
					plugin.getConfig().set("PvP Timer." + w.getName() + ".On World Change.On",
							"&4PvP is currently enabled in " + w.getName());
					plugin.getConfig().set("PvP Timer." + w.getName() + ".On World Change.Off",
							"&2PvP is currently disabled in " + w.getName());
					plugin.saveConfig();
				}

				if (!schedulers.containsKey(w.getName().toLowerCase()))
					schedulers.put(w.getName().toLowerCase(), new PvPTimer(plugin, w));
			}
		}
	}

}
