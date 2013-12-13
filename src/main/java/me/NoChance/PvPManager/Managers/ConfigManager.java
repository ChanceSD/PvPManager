package me.NoChance.PvPManager.Managers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Others.SimpleConfig;
import me.NoChance.PvPManager.Others.SimpleConfigManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {

	private PvPManager plugin;
	private File usersFile;
	private YamlConfiguration users;
	private int configVersion;
	private SimpleConfigManager manager;
	private SimpleConfig config;
	private SimpleConfig pvpTimer;

	public ConfigManager(PvPManager plugin) {
		this.plugin = plugin;
		this.users = new YamlConfiguration();
		this.usersFile = new File(plugin.getDataFolder(), "users.yml");
		this.manager = new SimpleConfigManager(plugin);
		pvpTimer = manager.getNewConfig("PvPTimer.yml");
		configVersion = getConfig().getInt("Config Version", 0);
		if (configVersion < 9) {
			File configFile = new File(plugin.getDataFolder(), "config.yml");
			if (configFile.exists()) {
				new Variables(this);
				configFile.delete();
				config = manager.getNewConfig("config.yml");
				updateDefaultConfig();
			}
		}
		config = manager.getNewConfig("config.yml");
		new Variables(this);
	}

	// If PvPTimer.yml ever needs to be updated
	// private void updatePvpTimerConfig() {
	// pvpTimer.set("PvP Timer.Enabled", Variables.pvpTimerEnabled);
	// pvpTimer.set("PvP Timer.Sound.PvP Off Sound", Variables.pvpOffSound);
	// pvpTimer.set("PvP Timer.Sound.PvP On Sound", Variables.pvpOnSound);
	// pvpTimer.set("PvP Timer.Sound.Enabled", Variables.enableSound);
	// pvpTimer.set("PvP Timer.Announce On World Change",
	// Variables.announcePvpOnWorldChange);
	// }

	private void updateDefaultConfig() {
		this.config.set("Config Version", 9);
		this.config.set("PvP Blood", Variables.pvpBlood);
		this.config.set("Disable Fly", Variables.disableFly);
		this.config.set("Disable GameMode", Variables.disableGamemode);

		this.config.set("In Combat.Enabled", Variables.inCombatEnabled);
		this.config.set("In Combat.Time(seconds)", Variables.timeInCombat);
		this.config.set("In Combat.Only Tag Attacker", Variables.onlyTagAttacker);
		this.config.set("In Combat.Stop Commands.Enabled", Variables.stopCommands);
		this.config.set("In Combat.Stop Commands.Allowed Commands", Variables.commandsAllowed);
		this.config.set("In Combat.Punishments.Enabled", Variables.punishmentsEnabled);
		this.config.set("In Combat.Punishments.Broadcast PvPLog", Variables.broadcastPvpLog);
		this.config.set("In Combat.Punishments.Kill on Logout", Variables.killOnLogout);
		this.config.set("In Combat.Punishments.Drops.Inventory", Variables.dropInventory);
		this.config.set("In Combat.Punishments.Drops.Experience", Variables.dropExp);
		this.config.set("In Combat.Punishments.Drops.Armor", Variables.dropArmor);
		this.config.set("In Combat.Punishments.Fine.Enabled", Variables.fineEnabled);
		this.config.set("In Combat.Punishments.Fine.Amount", Variables.fineAmount);

		this.config.set("Toggle Signs.Enabled", Variables.toggleSignsEnabled);
		this.config.set("Toggle Signs.Disable Toggle Command", Variables.disableToggleCommand);

		this.config.set("Newbie Protection.Enabled", Variables.newbieProtectionEnabled);
		this.config.set("Newbie Protection.Time(minutes)", Variables.newbieProtectionTime);

		this.config.set("Update Check.Enabled", Variables.updateCheck);
		this.config.set("Update Check.Auto Update", Variables.autoUpdate);

		this.config.set("World Exclusions", Variables.worldsExcluded);
		this.config.saveConfig();
	}

	public void load() {
		try {
			if (!usersFile.exists()) {
				plugin.saveResource("users.yml", false);
				getDefaults();
				plugin.getLogger().info("New Users File Created Successfully!");
				return;
			}
			users.load(usersFile);
			save();
		} catch (Exception e) {
		}
	}

	public void getDefaults() {
		InputStream defConfigStream = plugin.getResource("users.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			users.setDefaults(defConfig);
			users.options().copyHeader(true);
			users.options().copyDefaults(true);
		}
	}

	public void loadUsers() {
		List<String> store = new ArrayList<String>();
		store = users.getStringList("players");
		for (String a : store) {
			plugin.getCm().disablePvp(a);
		}
		store.clear();
	}

	public void save() {
		try {
			users.save(usersFile);
		} catch (Exception e) {
			System.out.println(("Config Failed to save, returned error: " + e.getMessage()));
		}
	}

	public void saveUsers() {
		List<String> store = new ArrayList<String>();
		for (String a : plugin.getCm().getPlayersStatusOff()) {
			store.add(a);
		}
		try {
			users.set("players", store);
			users.save(usersFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public FileConfiguration getConfig() {
		if (configVersion < 9)
			return getPlugin().getConfig();
		return config;
	}

	public SimpleConfig getPvpTimer() {
		return pvpTimer;
	}

	public PvPManager getPlugin() {
		return plugin;
	}
}
