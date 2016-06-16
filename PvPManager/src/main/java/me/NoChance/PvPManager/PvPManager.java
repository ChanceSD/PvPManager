package me.NoChance.PvPManager;

import java.io.File;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import me.NoChance.PvPManager.Commands.Announce;
import me.NoChance.PvPManager.Commands.PM;
import me.NoChance.PvPManager.Commands.PvP;
import me.NoChance.PvPManager.Commands.PvPInfo;
import me.NoChance.PvPManager.Commands.PvPList;
import me.NoChance.PvPManager.Commands.PvPOverride;
import me.NoChance.PvPManager.Commands.PvPStatus;
import me.NoChance.PvPManager.Commands.Tag;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Libraries.Metrics.CustomMetrics;
import me.NoChance.PvPManager.Libraries.Updater.BukkitUpdater;
import me.NoChance.PvPManager.Libraries.Updater.Updater;
import me.NoChance.PvPManager.Libraries.Updater.Updater.UpdateResult;
import me.NoChance.PvPManager.Libraries.Updater.Updater.UpdateType;
import me.NoChance.PvPManager.Listeners.EntityListener;
import me.NoChance.PvPManager.Listeners.PlayerListener;
import me.NoChance.PvPManager.Listeners.PlayerMoveListener;
import me.NoChance.PvPManager.Listeners.WGListener;
import me.NoChance.PvPManager.Managers.ConfigManager;
import me.NoChance.PvPManager.Managers.DependencyManager;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.LogFile;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.Log;

public final class PvPManager extends JavaPlugin {

	private ConfigManager configM;
	private PlayerHandler playerHandler;
	private Updater updater;
	private LogFile log;
	private DependencyManager dependencyManager;
	private static PvPManager instance;

	@Override
	public void onEnable() {
		instance = this;
		Log.setup(getLogger());
		loadFiles();
		dependencyManager = new DependencyManager();
		playerHandler = new PlayerHandler(this);
		startListeners();
		getCommand("pvp").setExecutor(new PvP(playerHandler));
		getCommand("pvpmanager").setExecutor(new PM(this));
		getCommand("pvpoverride").setExecutor(new PvPOverride(playerHandler));
		getCommand("pvpinfo").setExecutor(new PvPInfo(playerHandler));
		getCommand("pvplist").setExecutor(new PvPList(playerHandler));
		getCommand("pvpstatus").setExecutor(new PvPStatus(playerHandler));
		getCommand("tag").setExecutor(new Tag(playerHandler));
		getCommand("announce").setExecutor(new Announce());
		startMetrics();
	}

	@Override
	public void onDisable() {
		playerHandler.handlePluginDisable();
		instance = null;
	}

	private void loadFiles() {
		this.configM = new ConfigManager(this);
		Messages.setup(this);
		if (Settings.isLogToFile()) {
			log = new LogFile(new File(getDataFolder(), "pvplog.txt"));
		}
	}

	private void startListeners() {
		registerListener(new EntityListener(playerHandler));
		registerListener(new PlayerListener(playerHandler));
		if (dependencyManager.isDependencyEnabled(Hook.WORLDGUARD)) {
			if (Settings.borderHoppingPushback()) {
				registerListener(new PlayerMoveListener(playerHandler));
			}
			registerListener(new WGListener(playerHandler));
		}
	}

	private void startMetrics() {
		new CustomMetrics(this);
	}

	public void checkForUpdates() {
		Log.info("Checking for updates...");
		// disable spigot updater for now
		// if (Settings.getUpdateLocation().equalsIgnoreCase("Bukkit")) {
		updater = new BukkitUpdater(this, 63773, UpdateType.VERSION_CHECK);
		// }
		// else {
		// updater = new SpigotUpdater(this, UpdateType.VERSION_CHECK);
		// }
		if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
			Messages.setNewVersion(updater.getLatestName());
			Log.info("Update Available: " + Messages.getNewVersion());
			if (Settings.isAutoUpdate()) {
				downloadUpdate();
				Log.info("Version Downloaded To Your Update Folder");
				return;
			}
			Settings.setUpdate(true);
			Log.info("Link: http://dev.bukkit.org/bukkit-plugins/pvpmanager/");
		} else {
			Log.info("No update found");
		}
	}

	public boolean downloadUpdate() {
		return updater.downloadFile();
	}

	private void registerListener(final Listener listener) {
		this.getServer().getPluginManager().registerEvents(listener, this);
	}

	public ConfigManager getConfigM() {
		return configM;
	}

	public PlayerHandler getPlayerHandler() {
		return playerHandler;
	}

	public LogFile getLog() {
		return log;
	}

	public DependencyManager getDependencyManager() {
		return dependencyManager;
	}

	/**
	 * @return instance of PvPManager
	 */
	public static PvPManager getInstance() {
		return instance;
	}

}
