package me.NoChance.PvPManager.Managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Version;
import me.NoChance.PvPManager.Config.Config;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.NoChance.PvPManager.Utils.Log;

public class ConfigManager {

	private final PvPManager plugin;
	private final File usersFile;
	private final YamlConfiguration users;
	private Config config;

	public ConfigManager(final PvPManager plugin) {
		this.plugin = plugin;
		this.users = new YamlConfiguration();
		this.usersFile = new File(plugin.getDataFolder(), "users.yml");
		loadConfig();
		loadUsersFile();
	}

	private void loadConfig() {
		final File configFile = new File(plugin.getDataFolder(), "config.yml");
		plugin.reloadConfig();
		// This version can't be auto updated, so let's backup
		if (getConfigVersion() < 38) {
			if (configFile.exists()) {
				try {
					Files.move(configFile.toPath(), configFile.toPath().resolveSibling("config.old.yml"), StandardCopyOption.REPLACE_EXISTING);
				} catch (final IOException e) {
					e.printStackTrace();
				}
				initConfig();
				Log.warning("Configuration file updated to version: " + Variables.getConfigVersion());
				Log.warning("Due to big changes, your config file was renamed to config.old.yml");
				Log.warning("Please copy your settings manually to the new config file");
				Messages.getMessageQueue().add("§6[§fPvPManager§6] " + "§2Configuration file updated to version §e" + Variables.getConfigVersion());
				Messages.getMessageQueue().add("§6[§fPvPManager§6] " + "§2Due to big changes, your config file was renamed to config.old.yml");
				Messages.getMessageQueue().add("§6[§fPvPManager§6] " + "§2Please copy your settings manually to the new config file");
			} else {
				Log.info("New Config File Created Successfully!");
				initConfig();
				return;
			}
		} else if (getConfigVersion() < Integer.parseInt(Version.getConfigVersion())) {
			if (configFile.exists()) {
				initConfig();
				configFile.delete();
				config = new Config(plugin, "config.yml");
				Variables.updateDefaultConfig(config, Integer.parseInt(Version.getConfigVersion()));
				Log.warning("Configuration file updated to version: " + Variables.getConfigVersion());
				Log.warning("It's recommended that you check the file and adjust the new settings");
				Messages.getMessageQueue().add("§6[§fPvPManager§6] " + "§2Configuration file updated to version §e" + Variables.getConfigVersion());
				Messages.getMessageQueue().add("§6[§fPvPManager§6] " + "§2It's recommended that you check the file and adjust the new settings");
			}
		} else {
			initConfig();
		}
		if (Variables.isUpdateCheck()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					plugin.checkForUpdates();
				}
			}.runTaskTimer(plugin, 0, 360000);
		}
	}

	private void initConfig() {
		config = new Config(plugin, "config.yml");
		Variables.initizalizeVariables(config);
	}

	private void loadUsersFile() {
		try {
			if (!usersFile.exists()) {
				plugin.saveResource("users.yml", false);
				Log.info("New Users File Created Successfully!");
			}
			users.load(usersFile);
			// replace old users file
			if (users.get("players") instanceof List) {
				plugin.saveResource("users.yml", true);
				users.load(usersFile);
			}
		} catch (final Exception e) {
			Log.severe("Error loading users file! Error: ");
			e.printStackTrace();
		}
	}

	public final void saveUser(final PvPlayer player) {
		// check if we really need to save this player
		if (player.hasPvPEnabled() == Variables.isDefaultPvp() && CombatUtils.hasTimePassed(player.getToggleTime(), Variables.getToggleCooldown())) {
			// clear entry for this user if there is one
			if (getUserStorage().contains(player.getUUID().toString())) {
				getUserStorage().set(player.getUUID().toString(), null);
				saveUsersToDisk();
			}
			return;
		}

		final Map<String, Object> userData = new HashMap<>();
		userData.put("pvpstatus", player.hasPvPEnabled());
		userData.put("toggletime", player.getToggleTime());

		getUserStorage().set(player.getUUID().toString(), userData);
		saveUsersToDisk();
	}

	private void saveUsersToDisk() {
		try {
			users.save(usersFile);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public final FileConfiguration getConfig() {
		return config;
	}

	public Map<String, Object> getUserData(final UUID uuid) {
		return getUserStorage().getConfigurationSection(uuid.toString()).getValues(false);
	}

	public ConfigurationSection getUserStorage() {
		return users.getConfigurationSection("players");
	}

	public final int getConfigVersion() {
		return plugin.getConfig().getInt("Config Version", 0);
	}

}
