package me.NoChance.PvPManager.Managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Version;
import me.NoChance.PvPManager.Libraries.Config.ConfigUpdater;
import me.NoChance.PvPManager.Settings.Config;
import me.NoChance.PvPManager.Settings.LogFile;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.NoChance.PvPManager.Utils.Log;

public class ConfigManager {

	private final PvPManager plugin;
	private final File configFile;
	private final File usersFile;
	private final YamlConfiguration users = new YamlConfiguration();
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final BlockingQueue<PvPlayer> playersToSave = new LinkedBlockingDeque<>();
	private Future<?> lastTask;
	private ConfigurationSection userSection;
	private Config config;
	private LogFile log;

	public ConfigManager(final PvPManager plugin) {
		this.plugin = plugin;
		this.usersFile = new File(plugin.getDataFolder(), "users.yml");
		this.configFile = new File(plugin.getDataFolder(), "config.yml");
		loadConfig();
		loadUsersFile();
		if (Settings.isLogToFile()) {
			log = new LogFile(new File(plugin.getDataFolder(), "combatlogs.log"));
		}
	}

	private void loadConfig() {
		checkConfig();
		initConfig();
		if (Settings.isUpdateCheck()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					plugin.checkForUpdates();
				}
			}.runTaskTimerAsynchronously(plugin, 0, 360000);
		}
	}

	private void checkConfig() {
		if (!configFile.exists())
			return;

		plugin.reloadConfig();
		final int oldVersion = getConfigVersion();
		final int currentVersion = Integer.parseInt(Version.getConfigVersion());

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

	private void loadUsersFile() {
		try {
			if (!usersFile.exists()) {
				plugin.saveResource("users.yml", false);
				Log.info("New users file created successfully!");
			}
			users.load(usersFile);
			// replace old users file
			if (users.get("players") == null || users.get("players") instanceof List) {
				resetUsersFile();
			}
			this.userSection = users.getConfigurationSection("players");
			Log.info("Loaded " + getUserStorage().getKeys(false).size() + " players from users file");
		} catch (final Exception e) {
			Log.severe("Error loading users file!", e);
			resetUsersFile();
		}
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

	private void resetUsersFile() {
		try {
			Files.move(usersFile.toPath(), usersFile.toPath().resolveSibling("users_error.yml"), StandardCopyOption.REPLACE_EXISTING);
			plugin.saveResource("users.yml", true);
			users.load(usersFile);
			Log.warning("Users file was reset due to corruption. A backup was saved as 'users_error.yml'");
			Log.warning("If you believe this error wasn't caused by you please report it on github");
		} catch (IOException | InvalidConfigurationException e) {
			Log.severe("Error loading users file after reset!", e);
		}
	}

	private final void saveUser(final PvPlayer player) {
		// check if we really need to save this player
		if (!player.isNewbie() && player.hasPvPEnabled() == Settings.isDefaultPvp()
		        && CombatUtils.hasTimePassed(player.getToggleTime(), Settings.getToggleCooldown())) {
			// clear entry for this user if there is one
			if (getUserStorage().contains(player.getUUID().toString())) {
				removeUser(player.getUUID().toString());
			}
			return;
		}
		getUserStorage().createSection(player.getUUID().toString(), player.getUserData());
	}

	public final void markForSave(final PvPlayer player) {
		if (playersToSave.offer(player) && (lastTask == null || lastTask.isDone())) {
			try {
				if (lastTask != null && lastTask.isDone()) {
					lastTask.get(50, TimeUnit.MILLISECONDS);
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			} catch (ExecutionException | TimeoutException e) {
				e.printStackTrace();
			}
			triggerSave();
		}
	}

	public final void removeUser(final String id) {
		getUserStorage().set(id, null);
	}

	public final void removeUsers(final List<String> ids) {
		for (final String id : ids) {
			removeUser(id);
		}
		saveUsersToDisk();
	}

	private void triggerSave() {
		lastTask = executor.submit(() -> {
			final long start = System.currentTimeMillis();
			Log.debug("Starting async save...");
			while (playersToSave.peek() != null) {
				saveUser(playersToSave.poll());
			}
			saveUsersToDisk();
			Log.debug("Finished async save - " + (System.currentTimeMillis() - start) + " ms");
			if (!playersToSave.isEmpty()) {
				triggerSave();
			}
		});
	}

	public void awaitSave() {
		try {
			while (!playersToSave.isEmpty()) {
				final long start = System.currentTimeMillis();
				Log.debug("Awaiting save...");
				lastTask.get();
				Log.debug("Finished awaiting - " + (System.currentTimeMillis() - start) + " ms");
			}
		} catch (final InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		} catch (final ExecutionException e) {
			e.printStackTrace();
		}
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

	public LogFile getLog() {
		return log;
	}

	public Map<String, Object> getUserData(final UUID uuid) {
		return getUserStorage().getConfigurationSection(uuid.toString()).getValues(false);
	}

	public ConfigurationSection getUserStorage() {
		return userSection;
	}

	public final int getConfigVersion() {
		return plugin.getConfig().getInt("Config Version", 0);
	}

}
