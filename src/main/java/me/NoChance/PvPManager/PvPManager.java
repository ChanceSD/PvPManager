package me.NoChance.PvPManager;

import java.io.File;

import me.NoChance.PvPManager.Commands.*;
import me.NoChance.PvPManager.Config.LogFile;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Lib.CustomMetrics;
import me.NoChance.PvPManager.Lib.Updater;
import me.NoChance.PvPManager.Lib.Updater.UpdateResult;
import me.NoChance.PvPManager.Listeners.*;
import me.NoChance.PvPManager.Managers.ConfigManager;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Utils.CombatUtils;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class PvPManager extends JavaPlugin {

	private ConfigManager configM;
	private PlayerHandler playerHandler;
	private LogFile log;

	@Override
	public void onEnable() {
		loadFiles();
		playerHandler = new PlayerHandler(this);
		startListeners();
		new CombatUtils(this);
		getCommand("pvp").setExecutor(new PvP(playerHandler));
		getCommand("pvpmanager").setExecutor(new PM(this));
		getCommand("pvpoverride").setExecutor(new PvPOverride(playerHandler));
		startMetrics();
		if (Variables.updateCheck) {
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
		for (PvPlayer p : playerHandler.getPlayers().values()) {
			if (p.isInCombat())
				p.unTag();
			playerHandler.savePvPState(p.getUUID(), p.hasPvPEnabled());
		}
		playerHandler.removeTeams();
	}

	private void loadFiles() {
		this.configM = new ConfigManager(this);
		new Messages(this);
		if (Variables.logToFile)
			log = new LogFile(new File(getDataFolder(), "pvplog.txt"));
	}

	private void startListeners() {
		registerListener(new PlayerListener(this));
		if (Variables.toggleSignsEnabled) {
			registerListener(new SignListener(playerHandler));
		}
	}

	private void startMetrics() {
		new CustomMetrics(this);
	}

	private void checkForUpdates() {
		getLogger().info("Checking for updates...");
		Updater updater = new Updater(this, 63773, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, true);
		if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
			Messages.newVersion = updater.getLatestName();
			getLogger().info("Update Available: " + Messages.newVersion);
			if (Variables.autoUpdate) {
				downloadUpdate();
				return;
			}
			Variables.update = true;
			getLogger().info("Link: http://dev.bukkit.org/bukkit-plugins/pvpmanager/");
		} else
			getLogger().info("No update found");
	}

	public boolean downloadUpdate() {
		Updater updater = new Updater(this, 63773, this.getFile(), Updater.UpdateType.NO_VERSION_CHECK, false);
		return updater.getResult() == UpdateResult.SUCCESS;
	}

	public void registerListener(Listener listener) {
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

}