package me.NoChance.PvPManager;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import me.NoChance.PvPManager.Commands.Announce;
import me.NoChance.PvPManager.Commands.Newbie;
import me.NoChance.PvPManager.Commands.PM;
import me.NoChance.PvPManager.Commands.PvP;
import me.NoChance.PvPManager.Commands.PvPInfo;
import me.NoChance.PvPManager.Commands.PvPList;
import me.NoChance.PvPManager.Commands.PvPOverride;
import me.NoChance.PvPManager.Commands.PvPStatus;
import me.NoChance.PvPManager.Commands.Tag;
import me.NoChance.PvPManager.Libraries.Metrics.CustomMetrics;
import me.NoChance.PvPManager.Libraries.Updater.BukkitUpdater;
import me.NoChance.PvPManager.Libraries.Updater.SpigotUpdater;
import me.NoChance.PvPManager.Libraries.Updater.Updater;
import me.NoChance.PvPManager.Libraries.Updater.Updater.UpdateResult;
import me.NoChance.PvPManager.Libraries.Updater.Updater.UpdateType;
import me.NoChance.PvPManager.Listeners.EntityListener;
import me.NoChance.PvPManager.Listeners.EntityListener1_9;
import me.NoChance.PvPManager.Listeners.PlayerListener;
import me.NoChance.PvPManager.Listeners.PlayerListener1_11;
import me.NoChance.PvPManager.Managers.ConfigManager;
import me.NoChance.PvPManager.Managers.DependencyManager;
import me.NoChance.PvPManager.Managers.DisplayManager;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.NoChance.PvPManager.Utils.Log;
import me.chancesd.pvpmanager.managers.StorageManager;
import me.chancesd.pvpmanager.utils.ScheduleUtils;

public class PvPManager extends JavaPlugin {

	private ConfigManager configM;
	private PlayerHandler playerHandler;
	private Updater updater;
	private StorageManager storageManager;
	private DependencyManager dependencyManager;
	private DisplayManager displayManager;
	private EntityListener entityListener;
	private static PvPManager instance;

	@Override
	public void onEnable() {
		final long start = System.currentTimeMillis();
		instance = this;
		Log.setup(getLogger());
		ScheduleUtils.setupExecutor();
		if (ScheduleUtils.checkFolia()) {
			Log.info("Running on Folia. Support for Folia is still experimental");
		}
		loadFiles();
		storageManager = new StorageManager(this);
		dependencyManager = new DependencyManager();
		displayManager = new DisplayManager(this);
		playerHandler = new PlayerHandler(this);
		startListeners();
		registerCommands();
		startMetrics();
		checkJavaVersion();
		Log.info("PvPManager Enabled (" + (System.currentTimeMillis() - start) + " ms)");
	}

	@Override
	public void onDisable() {
		playerHandler.handlePluginDisable();
		storageManager.shutdown();
		ScheduleUtils.cancelAllTasks(this);
		instance = null;
	}

	private void loadFiles() {
		this.configM = new ConfigManager(this);
		Messages.setup(this);
	}

	private void startListeners() {
		if (CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.9")) {
			registerListener(new EntityListener1_9(playerHandler));
		}
		entityListener = new EntityListener(playerHandler);
		registerListener(entityListener);
		if (CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.11.2")) {
			registerListener(new PlayerListener1_11(playerHandler));
		}
		registerListener(new PlayerListener(playerHandler));
		dependencyManager.startListeners(this);
	}

	private void registerCommands() {
		registerCommand(getCommand("pvp"), new PvP(playerHandler));
		registerCommand(getCommand("newbie"), new Newbie(playerHandler));
		registerCommand(getCommand("pvpmanager"), new PM(this));
		registerCommand(getCommand("pvpoverride"), new PvPOverride(playerHandler));
		registerCommand(getCommand("pvpinfo"), new PvPInfo(playerHandler));
		registerCommand(getCommand("pvplist"), new PvPList(playerHandler));
		registerCommand(getCommand("pvpstatus"), new PvPStatus(playerHandler));
		registerCommand(getCommand("pvptag"), new Tag(playerHandler));
		registerCommand(getCommand("announce"), new Announce());
	}

	private void startMetrics() {
		new CustomMetrics(this);
	}

	public void checkForUpdates() {
		Log.info("Checking for updates...");
		updater = new BukkitUpdater(this, 63773, UpdateType.VERSION_CHECK).check();
		if (updater.getResult() != UpdateResult.UPDATE_AVAILABLE) {
			updater = new SpigotUpdater(this, 845, UpdateType.VERSION_CHECK).check();
		}
		if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
			Messages.setNewVersion(updater.getLatestName());
			final String updateMsg = Messages.PREFIXMSG + " §aUpdate available: §c" + Messages.getNewVersion() + " §aCurrent version: §c"
			        + Messages.getCurrentversion();
			Messages.queueAdminMsg(updateMsg);
			Bukkit.broadcast(updateMsg, "pvpmanager.admin");
			if (Settings.isAutoUpdate()) {
				if (updater.downloadFile()) {
					Messages.queueAdminMsg(Messages.PREFIXMSG + " §aUpdate downloaded, it will be applied automatically on the next server restart");
					Bukkit.broadcast(Messages.PREFIXMSG + " §aUpdate downloaded to your update folder, it will be applied automatically on the next server restart",
					        "pvpmanager.admin");
					return;
				}
				Log.info("Could not download latest update. Please update manually from one of the links below.");
			}
			Settings.setUpdate(true);
			final String linkMsg = Messages.PREFIXMSG + " §aFollow the link to download: §8" + updater.getUpdateLink();
			Messages.queueAdminMsg(linkMsg);
			Bukkit.broadcast(linkMsg, "pvpmanager.admin");
		} else {
			Log.info("No update found");
		}
	}

	private void checkJavaVersion() {
		int javaVersion;
		String version = System.getProperty("java.version");
		if (version.startsWith("1.")) {
			version = version.substring(2, 3);
		} else {
			final int dot = version.indexOf(".");
			if (dot != -1) {
				version = version.substring(0, dot);
			} else {
				final int separator = version.indexOf("-");
				if (separator != -1) {
					version = version.substring(0, separator);
				}
			}
		}
		try {
			javaVersion = Integer.parseInt(version);
		} catch (final NumberFormatException e) {
			return;
		}
		if (javaVersion < 16) {
			Log.severe("You appear to be using Java 15 or lower. For now the plugin still works but please update to Java 16+");
			Log.severe("In the future PvPManager will stop supporting Java versions this old");
		}
	}

	private void registerListener(final Listener listener) {
		this.getServer().getPluginManager().registerEvents(listener, this);
	}

	private void registerCommand(final PluginCommand command, final CommandExecutor executor) {
		if (command == null)
			return;
		command.setExecutor(executor);
	}

	public EntityListener getEntityListener() {
		return entityListener;
	}

	public ConfigManager getConfigM() {
		return configM;
	}

	public PlayerHandler getPlayerHandler() {
		return playerHandler;
	}

	public StorageManager getStorageManager() {
		return storageManager;
	}

	public DependencyManager getDependencyManager() {
		return dependencyManager;
	}

	public Updater getUpdater() {
		return updater;
	}

	public DisplayManager getDisplayManager() {
		return displayManager;
	}

	/**
	 * @return instance of PvPManager
	 */
	public static PvPManager getInstance() {
		return instance;
	}

}
