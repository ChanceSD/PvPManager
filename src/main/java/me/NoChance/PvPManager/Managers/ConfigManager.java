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

import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {

	private PvPManager plugin;
	private File usersFile;
	private YamlConfiguration users;
	public SimpleConfigManager manager;
	public SimpleConfig config;
	public SimpleConfig messages;

	public ConfigManager(PvPManager plugin) {
		this.plugin = plugin;
		this.users = new YamlConfiguration();
		this.usersFile = new File(plugin.getDataFolder(), "users.yml");
		this.manager = new SimpleConfigManager(plugin);
		this.config = manager.getNewConfig("config.yml", new String[]{"hello", "hello2"});
		new Variables(this);
		if (getConfig().getInt("Config Version", 0) < 9) {
			updateConfig();
		}
	}

	private void updateConfig() {
		this.config.set("Config Version", 9,
				"Informs PvPManager if the config is updated, do not touch it unless you want to reset the config");
		this.config.set("PvP Blood", Variables.pvpBlood, "Should blood animation on PvP be enabled");

		this.config.set("In Combat.Enabled", Variables.inCombatEnabled,
				"This section defines everything that will happen when a player is tagged in combat");
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

		this.config.set("Toggle Signs.Enabled", Variables.toggleSignsEnabled, new String[] {
				"Should toggle PvP signs be enabled? (Similar to /pvp command but by clicking signs)",
				"Disable Toggle Command - Should /pvp command be disabled in order to force using of signs by players?" });
		this.config.set("Toggle Signs.Disable Toggle Command", Variables.disableToggleCommand);

		this.config.set("Newbie Protection.Enabled", Variables.newbieProtectionEnabled, new String[] {
				"Should new players on your server be protected from PvP",
				"Time(minutes) - If so for how many minutes should protection last" });
		this.config.set("Newbie Protection.Time(minutes)", Variables.newbieProtectionTime);

		this.config.set("Update Check.Enabled", Variables.updateCheck, new String[] {
				"Should PvPManager be allowed to check for updates and tell you about new ones",
				"Auto Update - After checking should we download it automatically for you?" });
		this.config.set("Update Check.Auto Update", Variables.autoUpdate);

		this.config.set("World Exclusions", Variables.worldsExcluded, "In these worlds PvPManager will not have any effect");

		// this.config.set("In Combat.Disable Fly", Variables.disableFly,
		// "fly");

		// this.config.set("In Combat.Disable GameMode");

		// pvpTimerEnabled = getBoolean("PvP Timer.Enabled");

		// pvpOffSound = getString("PvP Timer.Sound.PvP Off Sound");
		// pvpOnSound = getString("PvP Timer.Sound.PvP On Sound");
		// enableSound = getBoolean("PvP Timer.Sound.Enabled");
		// announcePvpOnWorldChange =
		// getBoolean("PvP Timer.Announce On World Change");

		// String[] comments = { "Multiple lines", "Of nice comments",
		// "Are supported !" };
		// String[] header = { "This is super simple",
		// "And highly customizable", "new and fresh SimpleConfig !" };
		// File file = new File("PvPTimer.yml");
		// file.delete();

		// this.config.reloadConfig();
		// this.config.set("path1", "value1", comments);
		// this.config.set("path0", "value2", "Ola");
		// this.config.set("path1", "value1", "ola2");
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

	public SimpleConfig getConfig() {
		return config;
	}

	public PvPManager getPlugin() {
		return plugin;
	}
}
