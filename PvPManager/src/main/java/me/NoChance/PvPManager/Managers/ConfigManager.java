package me.NoChance.PvPManager.Managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Version;
import me.NoChance.PvPManager.Config.Config;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Utils.Log;

public class ConfigManager {

	private final PvPManager plugin;
	private final File usersFile;
	private final YamlConfiguration users;
	private Config config;
	private final int configVersion;

	public ConfigManager(final PvPManager plugin) {
		this.plugin = plugin;
		this.users = new YamlConfiguration();
		this.usersFile = new File(plugin.getDataFolder(), "users.yml");
		configVersion = plugin.getConfig().getInt("Config Version", 0);
		loadConfig();
		loadUsers();
	}

	private void loadConfig() {
		final File configFile = new File(plugin.getDataFolder(), "config.yml");
		// This version can't be auto updated, so let's backup
		if (getConfigVersion() < 38) {
			if (configFile.exists()) {
				try {
					Files.move(configFile.toPath(), configFile.toPath().resolveSibling("config.old.yml"), StandardCopyOption.REPLACE_EXISTING);
				} catch (final IOException e) {
					e.printStackTrace();
				}
				initConfig();
				Messages.getMessageQueue().add("§6[§fPvPManager§6] " + "§2Configuration file was updated to version §e" + configVersion);
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
				Variables.updateDefaultConfig(config, configVersion);
				Messages.getMessageQueue().add("§6[§fPvPManager§6] " + "§2Configuration file was updated to version §e" + configVersion);
				Messages.getMessageQueue().add("§6[§fPvPManager§6] " + "§2It's recommended that you check for changes and adjust them");
			}
		} else
			initConfig();
		if (Variables.isUpdateCheck())
			new BukkitRunnable() {
				@Override
				public void run() {
					plugin.checkForUpdates();
				}
			}.runTaskTimer(plugin, 0, 360000);
	}

	private void initConfig() {
		config = new Config(plugin, "config.yml");
		Variables.initizalizeVariables(config);
		plugin.reloadConfig();
	}

	private void loadUsers() {
		try {
			if (!usersFile.exists()) {
				plugin.saveResource("users.yml", false);
				Log.info("New Users File Created Successfully!");
				return;
			}
			users.load(usersFile);
		} catch (final Exception e) {
			Log.severe("Error loading users file! Error: ");
			e.printStackTrace();
		}
	}

	public final void saveUser(final UUID uuid, final boolean save) {
		final String id = uuid.toString();
		final List<String> userList = users.getStringList("players");
		if (save && userList.contains(id) || !save && !userList.contains(id))
			return;
		if (!save && userList.contains(id))
			userList.remove(id);
		if (save && !userList.contains(id))
			userList.add(id);

		users.set("players", userList);
		try {
			users.save(usersFile);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public final FileConfiguration getConfig() {
		return config;
	}

	public final YamlConfiguration getUserFile() {
		return users;
	}

	public final int getConfigVersion() {
		return plugin.getConfig().getInt("Config Version", 0);
	}

}
