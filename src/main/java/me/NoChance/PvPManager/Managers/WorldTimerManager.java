package me.NoChance.PvPManager.Managers;

import java.util.HashMap;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPTimer;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Others.SimpleConfig;
import me.NoChance.PvPManager.Others.Utils;
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
		return schedulers.containsKey(w.getName().toLowerCase());
	}

	public boolean isTimeForPvp(String world) {
		return getPvpTimer(world).isPvpTime();
	}

	public boolean isPvpTimerWorld(String worldName) {
		return schedulers.containsKey(worldName.toLowerCase());
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
		SimpleConfig config = plugin.getConfigM().getPvpTimer();
		for (World w : plugin.getServer().getWorlds()) {
			if (Utils.PMAllowed(w.getName())) {
				if (config.getConfigurationSection("Worlds." + w.getName()) == null) {
					ConfigurationSection world = config.getConfigurationSection("Worlds").createSection(w.getName());
					world.set("Start PvP", 13000);
					world.set("Start Difficulty", "NORMAL");
					world.set("End PvP", 0);
					world.set("End Difficulty", "EASY");
					if (Variables.announcePvpOnWorldChange) {
						world.set("On World Change.On", "&4PvP is currently enabled in " + w.getName());
						world.set("On World Change.Off", "&2PvP is currently disabled in " + w.getName());
					}
					config.saveConfig();
				}
				if (!schedulers.containsKey(w.getName().toLowerCase()))
					schedulers.put(w.getName().toLowerCase(), new PvPTimer(plugin, w));
			}
		}
	}

}
