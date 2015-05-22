package me.NoChance.PvPManager;

import java.io.File;

import me.NoChance.PvPManager.Commands.PM;
import me.NoChance.PvPManager.Commands.PvP;
import me.NoChance.PvPManager.Commands.PvPInfo;
import me.NoChance.PvPManager.Commands.PvPList;
import me.NoChance.PvPManager.Commands.PvPOverride;
import me.NoChance.PvPManager.Commands.PvPStatus;
import me.NoChance.PvPManager.Config.LogFile;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Lib.CustomMetrics;
import me.NoChance.PvPManager.Lib.Updater;
import me.NoChance.PvPManager.Lib.Updater.UpdateResult;
import me.NoChance.PvPManager.Listeners.PlayerListener;
import me.NoChance.PvPManager.Listeners.SignListener;
import me.NoChance.PvPManager.Managers.ConfigManager;
import me.NoChance.PvPManager.Managers.DependencyManager;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Utils.Log;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class PvPManager extends JavaPlugin {

	private ConfigManager configM;
	private PlayerHandler playerHandler;
	private LogFile log;
	private DependencyManager dependencyManager;

	@Override
	public void onEnable() {
		loadFiles();
		Log.setup(getLogger());
		dependencyManager = new DependencyManager(this);
		playerHandler = new PlayerHandler(this);
		startListeners();
		getCommand("pvp").setExecutor(new PvP(playerHandler));
		getCommand("pvpmanager").setExecutor(new PM(this));
		getCommand("pvpoverride").setExecutor(new PvPOverride(playerHandler));
		getCommand("pvpinfo").setExecutor(new PvPInfo(playerHandler));
		getCommand("pvplist").setExecutor(new PvPList(playerHandler));
		getCommand("pvpstatus").setExecutor(new PvPStatus(playerHandler));
		startMetrics();
		if (Variables.isUpdateCheck()) {
			new BukkitRunnable() {
				public void run() {
					checkForUpdates();
				}
			}.runTaskTimerAsynchronously(this, 0, 360000);
		}
	}

	@Override
	public void onDisable() {
		playerHandler.getTagTask().cancel();
		for (final PvPlayer p : playerHandler.getPlayers().values()) {
			if (p.isInCombat())
				p.unTag();
			playerHandler.savePvPState(p.getUUID(), p.hasPvPEnabled());
		}
		playerHandler.removeTeams();
	}

	private void loadFiles() {
		this.configM = new ConfigManager(this);
		new Messages(this);
		if (Variables.isLogToFile())
			log = new LogFile(new File(getDataFolder(), "pvplog.txt"));
	}

	private void startListeners() {
		registerListener(new PlayerListener(this));
		if (Variables.isToggleSignsEnabled()) {
			registerListener(new SignListener(playerHandler));
		}
	}

	private void startMetrics() {
		new CustomMetrics(this);
	}

	private void checkForUpdates() {
		Log.info("Checking for updates...");
		final Updater updater = new Updater(this, 63773, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, true);
		if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
			Messages.setNewVersion(updater.getLatestName());
			Log.info("Update Available: " + Messages.getNewversion());
			if (Variables.isAutoUpdate()) {
				downloadUpdate();
				return;
			}
			Variables.setUpdate(true);
			Log.info("Link: http://dev.bukkit.org/bukkit-plugins/pvpmanager/");
		} else
			Log.info("No update found");
	}

	public boolean downloadUpdate() {
		final Updater updater = new Updater(this, 63773, this.getFile(), Updater.UpdateType.NO_VERSION_CHECK, false);
		return updater.getResult() == UpdateResult.SUCCESS;
	}

	public void registerListener(final Listener listener) {
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

}
