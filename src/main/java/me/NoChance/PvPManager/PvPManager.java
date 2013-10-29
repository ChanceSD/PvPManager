package me.NoChance.PvPManager;

import java.util.HashMap;
import java.util.HashSet;

import me.NoChance.PvPManager.Updater.UpdateResult;
import me.NoChance.PvPManager.Commands.*;
import me.NoChance.PvPManager.Config.*;
import me.NoChance.PvPManager.Listeners.*;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public final class PvPManager extends JavaPlugin {

	public HashSet<String> playersStatusOff = new HashSet<String>();
	public ConfigManager configM;
	public Variables variables;
	public HashSet<String> newbies = new HashSet<String>();
	public HashMap<String, PvPTimer> schedulers = new HashMap<String, PvPTimer>();
	public boolean update;
	public String newVersion;
	private GlobalManager globalManager;

	@Override
	public void onEnable() {
		loadFiles();
		this.globalManager = new GlobalManager(this);
		getCommand("pvp").setExecutor(new PvP(this));
		getCommand("pm").setExecutor(new PM(this));
		new CustomGraph(this);
		if (Variables.updateCheck) {
			getLogger().info("Checking for updates...");
			Updater updater = new Updater(this, 63773, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, true);
			if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
				update = true;
				newVersion = updater.getLatestName();
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

	public boolean hasPvpEnabled(String name) {
		for (String n : playersStatusOff) {
			if (n.equalsIgnoreCase(name))
				return false;
		}
		return true;
	}
}