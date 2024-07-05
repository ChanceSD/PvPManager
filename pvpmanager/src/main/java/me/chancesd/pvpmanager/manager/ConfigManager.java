package me.chancesd.pvpmanager.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.collect.Lists;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.setting.LogFile;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.sdutils.config.ConfigUpdater;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.Utils;

public class ConfigManager {

	private static final String CONFIG_VERSION = "Config Version";
	private static final String CONFIG_NAME = "config.yml";
	private static final String HOOKS_CONFIG_NAME = "plugin-hooks.yml";
	private final PvPManager plugin;
	private final File configFile;
	private final LogFile log;
	private final File pluginHooksFile;
	private final YamlConfiguration pluginHooksConfig;
	private int oldVersion;

	public ConfigManager(final PvPManager plugin) {
		this.plugin = plugin;
		this.configFile = new File(plugin.getDataFolder(), CONFIG_NAME);
		pluginHooksFile = new File(plugin.getDataFolder(), HOOKS_CONFIG_NAME);
		prepareFile(pluginHooksFile, HOOKS_CONFIG_NAME);
		loadConfig();
		log = new LogFile(new File(plugin.getDataFolder(), "combatlogs.log"));
		pluginHooksConfig = loadYamlConfiguration(pluginHooksFile);
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
		oldVersion = plugin.getConfig().getInt(CONFIG_VERSION, 0);
		final int currentVersion = defaults != null ? defaults.getInt(CONFIG_VERSION) : 0;

		if (oldVersion == 0) {
			Utils.renameFile(configFile, "config_with_errors.yml");
			Log.warning("Due to an error reading the config, it was reset to default settings");
			Log.warning("This was likely caused by a mistake while you changed settings, like an extra space or missing quotes");
			Log.warning("The broken config was renamed to config_with_errors.yml, you can copy your old settings manually if you need them");
			Lang.queueAdminMsg(Lang.PREFIXMSG + " §cDue to an error reading the config, it was reset to default settings"
					+ "\n§cThis was likely caused by a mistake while you changed settings, like an extra space or missing quotes");
			Lang.queueAdminMsg(
					Lang.PREFIXMSG + " §cThe broken config was renamed to config_with_errors.yml, you can copy your old settings manually if needed");
			return;
		}
		if (isMajorVersionUpgrade()) {
			Utils.renameFile(configFile, "config_v3_old.yml");
			Log.warning("The config and messages file were restored to default since v4.0 is a major version update");
			Log.warning("You can find your old files in the plugin folder with _v3_old at the end, like config_v3_old.yml");
			Lang.queueAdminMsg(Lang.PREFIXMSG + " §aThe config and messages were upgraded and set to default since §6v4.0 is a major version update");
			Lang.queueAdminMsg("");
			Lang.queueAdminMsg("§aYou can find your old files in the plugin folder with §e_v3_old§a at the end, like §econfig_v3_old.yml");
			return;
		}
		if (oldVersion < currentVersion) {
			try {
				ConfigUpdater.update(plugin, CONFIG_NAME, configFile, Arrays.asList("Item Cooldowns.Combat", "Item Cooldowns.Global"),
						Arrays.asList(CONFIG_VERSION, "Metrics", "Update Check.Enabled"));
				ConfigUpdater.update(plugin, HOOKS_CONFIG_NAME, pluginHooksFile, Lists.newArrayList(), Lists.newArrayList());
				Log.infoColor("§aConfig file updated from version §c" + oldVersion + " §ato version §c" + currentVersion);
				Log.warning("Checking the config file and adjusting the new settings is highly recommended");
				Lang.queueAdminMsg(Lang.PREFIXMSG + " §aConfiguration updated from version §c" + oldVersion + " §ato §c" + currentVersion);
				Lang.queueAdminMsg(Lang.PREFIXMSG + " §aChecking the config file and adjusting the new settings is highly recommended");
			} catch (final IOException e) {
				Log.severe("Error reading the config file!", e);
				Utils.renameFile(configFile, "config_with_errors.yml");
			}
		}
	}

	private void initConfig() {
		if (!configFile.exists()) {
			this.prepareFile(configFile, CONFIG_NAME);
			Log.infoColor(ChatColor.DARK_GREEN + "New config file created successfully!");
		}
		Conf.initialize(plugin, loadYamlConfiguration(configFile));
	}

	private YamlConfiguration loadYamlConfiguration(final File file) {
		try (InputStreamReader inputStream = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
			return YamlConfiguration.loadConfiguration(inputStream);
		} catch (final IOException e) {
			Log.severe("Config file not found", e);
			return null;
		}
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

	public boolean isMajorVersionUpgrade() {
		return oldVersion <= 79;
	}

	public YamlConfiguration getHooksConfig() {
		return pluginHooksConfig;
	}

	public LogFile getLog() {
		return log;
	}

}
