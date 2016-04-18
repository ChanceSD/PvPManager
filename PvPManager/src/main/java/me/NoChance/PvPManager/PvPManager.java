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
import me.NoChance.PvPManager.Config.LogFile;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Lib.CustomMetrics;
import me.NoChance.PvPManager.Listeners.EntityListener;
import me.NoChance.PvPManager.Listeners.PlayerListener;
import me.NoChance.PvPManager.Listeners.WGListener;
import me.NoChance.PvPManager.Managers.ConfigManager;
import me.NoChance.PvPManager.Managers.DependencyManager;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Updater.BukkitUpdater;
import me.NoChance.PvPManager.Updater.SpigotUpdater;
import me.NoChance.PvPManager.Updater.Updater;
import me.NoChance.PvPManager.Updater.Updater.UpdateResult;
import me.NoChance.PvPManager.Updater.Updater.UpdateType;
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
		dependencyManager = new DependencyManager(this);
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
	}

	private void loadFiles() {
		this.configM = new ConfigManager(this);
		Messages.setup(this);
		if (Variables.isLogToFile()) {
			log = new LogFile(new File(getDataFolder(), "pvplog.txt"));
		}
	}

	private void startListeners() {
		registerListener(new EntityListener(playerHandler));
		registerListener(new PlayerListener(playerHandler));
		if (dependencyManager.useWG()) {
			registerListener(new WGListener(playerHandler));
		}
	}

	private void startMetrics() {
		new CustomMetrics(this);
	}

	public void checkForUpdates() {
		Log.info("Checking for updates...");
		if (Variables.getUpdateLocation().equalsIgnoreCase("Bukkit")) {
			updater = new BukkitUpdater(this, 63773, UpdateType.VERSION_CHECK);
		} else {
			updater = new SpigotUpdater(this, UpdateType.VERSION_CHECK);
		}
		if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
			Messages.setNewVersion(updater.getLatestName());
			Log.info("Update Available: " + Messages.getNewVersion());
			if (Variables.isAutoUpdate()) {
				downloadUpdate();
				Log.info("Version Downloaded To Your Update Folder");
				return;
			}
			Variables.setUpdate(true);
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

	public static PvPManager getInstance() {
		return instance;
	}

}
