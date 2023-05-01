package me.NoChance.PvPManager.Managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Libraries.Config.ConfigUpdater;
import me.NoChance.PvPManager.Settings.Config;
import me.NoChance.PvPManager.Settings.LogFile;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.Log;

public class ConfigManager {

	private final PvPManager plugin;
	private final File configFile;
	private Config config;
	private LogFile log;

	public ConfigManager(final PvPManager plugin) {
		this.plugin = plugin;
		this.configFile = new File(plugin.getDataFolder(), "config.yml");
		loadConfig();
		if (Settings.isLogToFile()) {
			log = new LogFile(new File(plugin.getDataFolder(), "combatlogs.log"));
		}
	}

	private void loadConfig() {
		checkConfig();
		initConfig();
	}

	private void checkConfig() {
		if (!configFile.exists())
			return;

		plugin.reloadConfig();
		final Configuration defaults = plugin.getConfig().getDefaults();
		final int oldVersion = plugin.getConfig().getInt("Config Version", 0);
		final int currentVersion = defaults != null ? defaults.getInt("Config Version") : 0;

		if (oldVersion == 0) {
			resetConfig();
			return;
		}
		if (oldVersion < currentVersion) {
			try {
				ConfigUpdater.update(plugin, "config.yml", configFile, Arrays.asList("Config Version", "Metrics", "Update Check.Enabled"));
				Log.warning("Config file updated from version " + oldVersion + " to version " + currentVersion);
				Log.warning("Checking the config file and adjusting the new settings is highly recommended");
				Messages.queueAdminMsg(Messages.PREFIXMSG + " §aConfiguration updated from version §c" + oldVersion + " §ato §c" + currentVersion);
				Messages.queueAdminMsg(Messages.PREFIXMSG + " §aChecking the config file and adjusting the new settings is highly recommended");
			} catch (final IOException e) {
				Log.severe("Error reading the config file!", e);
				resetConfig();
			}
		}
	}

	private void initConfig() {
		try {
			config = new Config(plugin, "config.yml");
		} catch (final FileNotFoundException e) {
			Log.severe("Config file not found", e);
		}
		Settings.initizalizeVariables(config);
	}

	private void resetConfig() {
		try {
			Files.move(configFile.toPath(), configFile.toPath().resolveSibling("config.old.yml"), StandardCopyOption.REPLACE_EXISTING);
		} catch (final IOException e) {
			Log.severe("Error resetting config file", e);
		}
		initConfig();
		Log.warning("Due to an error reading the config, it was reset to default settings");
		Log.warning("This was likely caused by a mistake while you changed settings, like an extra space or missing quotes");
		Log.warning("The broken config was renamed to config.old.yml, you can copy your old settings manually if you need them");
		Messages.queueAdminMsg(Messages.PREFIXMSG + " §cDue to an error reading the config, it was reset to default settings"
		        + "\n§cThis was likely caused by a mistake while you changed settings, like an extra space or missing quotes");
		Messages.queueAdminMsg(Messages.PREFIXMSG + "§cThe broken config was renamed to config.old.yml, you can copy your old settings manually if you need them");
	}

	public final FileConfiguration getConfig() {
		return config;
	}

	public LogFile getLog() {
		return log;
	}

}
