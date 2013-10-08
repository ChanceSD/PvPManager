package me.NoChance.PvPManager;

import java.util.HashMap;
import java.util.HashSet;

import me.NoChance.PvPManager.Updater.UpdateResult;
import me.NoChance.PvPManager.Commands.*;
import me.NoChance.PvPManager.Config.*;
import me.NoChance.PvPManager.Listeners.*;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class PvPManager extends JavaPlugin {

	public HashSet<String> playersStatusOff = new HashSet<String>();
	public HashSet<String> inCombat = new HashSet<String>();
	public ConfigManager configM;
	public Variables variables;
	public HashSet<String> newbies = new HashSet<String>();
	public HashMap<String, PvPTimer> schedulers = new HashMap<String, PvPTimer>();
	public boolean update;
	public String newVersion;

	@Override
	public void onEnable() {
		loadFiles();
		if ((Variables.stopCommands && Variables.inCombatEnabled) || Variables.pvpTimerEnabled) {
			new CommandListener(this);
		}
		if (Variables.pvpTimerEnabled) {
			enablePvPScheduler();
		}
		new DamageListener(this);
		new PlayerListener(this);
		if (Variables.toggleSignsEnabled) {
			new SignListener(this);
		}
		getCommand("pvp").setExecutor(new PvP(this));
		getCommand("pm").setExecutor(new PM(this));
		new CustomGraph(this);
		if (Variables.updateCheck) {
			getLogger().info("Checking for updates...");
			Updater updater = new Updater(this, "pvpmanager", this.getFile(), Updater.UpdateType.NO_DOWNLOAD, true);
			if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
				update = true;
				newVersion = updater.getLatestVersionString();
				getLogger().info("Update Available: " + newVersion);
				getLogger().info("Link: http://dev.bukkit.org/bukkit-plugins/pvpmanager/");
			} else
				getLogger().info("No update found");
		}
	}

	@Override
	public void onDisable() {
		this.configM.saveUsers();
		this.configM.save();
	}

	public void loadFiles() {
		new Messages(this);
		if (getConfig().getInt("Config Version") == 0 || getConfig().getInt("Config Version") < 7) {
			getConfig().options().copyDefaults(true);
			getConfig().set("Config Version", 7);
			this.saveConfig();
		}
		this.saveDefaultConfig();
		this.reloadConfig();
		this.configM = new ConfigManager(this);
		this.configM.load();
		this.configM.loadUsers();
		variables = new Variables(this);
	}

	public void enablePvPScheduler() {
		for (World w : getServer().getWorlds()) {
			if (!Variables.worldsExcluded.contains(w.getName())) {
				if (getConfig().getConfigurationSection("PvP Timer." + w.getName()) == null) {
					getConfig().getConfigurationSection("PvP Timer").createSection(w.getName());
					getConfig().set("PvP Timer." + w.getName() + ".Start PvP", 13000);
					getConfig().set("PvP Timer." + w.getName() + ".End PvP", 0);
					this.saveConfig();
				}

				if (!schedulers.containsKey(w.getName().toLowerCase()))
					schedulers.put(w.getName().toLowerCase(), new PvPTimer(this, w));
			}
		}
	}

	public boolean hasPvpEnabled(String name) {
		for (String n : playersStatusOff) {
			if (n.equalsIgnoreCase(name))
				return false;
		}
		return true;
	}
}