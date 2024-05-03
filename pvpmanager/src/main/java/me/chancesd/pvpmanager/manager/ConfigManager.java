package me.chancesd.pvpmanager.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.setting.LogFile;
import me.chancesd.pvpmanager.setting.Messages;
import me.chancesd.pvpmanager.setting.Settings;
import me.chancesd.sdutils.config.ConfigUpdater;
import me.chancesd.sdutils.utils.Log;

public class ConfigManager {

	private static final String CONFIG_VERSION = "Config Version";
	private static final String CONFIG_NAME = "config.yml";
	private final PvPManager plugin;
	private final File configFile;
	private YamlConfiguration config;
	private LogFile log;

	public ConfigManager(final PvPManager plugin) {
		this.plugin = plugin;
		this.configFile = new File(plugin.getDataFolder(), CONFIG_NAME);
		loadConfig();
		if (Settings.isLogToFile()) {
			log = new LogFile(new File(plugin.getDataFolder(), "combatlogs.log"));
		}
	}

	private void loadConfig() {
		checkConfigVersion();
		initConfig();
	}

	private void checkConfigVersion() {
		if (!configFile.exists())
			return;

		plugin.reloadConfig();
		final Configuration defaults = plugin.getConfig().getDefaults();
		final int oldVersion = plugin.getConfig().getInt(CONFIG_VERSION, 0);
		final int currentVersion = defaults != null ? defaults.getInt(CONFIG_VERSION) : 0;

		if (oldVersion == 0) {
			resetConfig();
			return;
		}
		if (oldVersion < currentVersion) {
			try {
				ConfigUpdater.update(plugin, CONFIG_NAME, configFile, Arrays.asList(CONFIG_VERSION, "Metrics", "Update Check.Enabled"));
				Log.infoColor("§aConfig file updated from version §c" + oldVersion + " §ato version §c" + currentVersion);
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
		if (!configFile.exists()) {
			this.prepareFile(configFile, CONFIG_NAME);
			Log.infoColor(ChatColor.DARK_GREEN + "New config file created successfully!");
		}
		try (InputStreamReader inputStream = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
			this.config = YamlConfiguration.loadConfiguration(inputStream);
		} catch (final IOException e) {
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

	private void prepareFile(final File file, final String resource) {
		try (InputStream stream = plugin.getResource(resource)) {
			file.getParentFile().mkdirs();
			if (file.createNewFile() && resource != null && !resource.isEmpty()) {
				this.copyResource(stream, file);
			}
		} catch (final IOException e) {
			Log.severe("Error creating config file", e);
		}
	}

	private void copyResource(final InputStream resource, final File file1) {
		try (OutputStream out = new FileOutputStream(file1)) {
			int lenght;
			final byte[] buf = new byte[1024];

			while ((lenght = resource.read(buf)) > 0) {
				out.write(buf, 0, lenght);
			}
			resource.close();
		} catch (final Exception e) {
			Log.severe("Error copying config file", e);
		}
	}

	public LogFile getLog() {
		return log;
	}

}
