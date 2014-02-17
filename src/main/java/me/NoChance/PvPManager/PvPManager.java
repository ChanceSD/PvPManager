package me.NoChance.PvPManager;

import me.NoChance.PvPManager.Commands.*;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Listeners.*;
import me.NoChance.PvPManager.Managers.ConfigManager;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Managers.WorldTimerManager;
import me.NoChance.PvPManager.Others.CustomGraph;
import me.NoChance.PvPManager.Others.Updater;
import me.NoChance.PvPManager.Others.Updater.UpdateResult;
import me.NoChance.PvPManager.Utils.Utils;
import org.bukkit.plugin.java.JavaPlugin;

public final class PvPManager extends JavaPlugin {

	private ConfigManager configM;
	private WorldTimerManager worldTimerManager;
	private PlayerHandler playerHandler;

	@Override
	public void onEnable() {
		loadFiles();
		playerHandler = new PlayerHandler(this);
		if (Variables.pvpTimerEnabled) {
			this.worldTimerManager = new WorldTimerManager(this);
		}
		startListeners();
		getCommand("pvp").setExecutor(new PvP(playerHandler));
		getCommand("pvpmanager").setExecutor(new PM(this));
		startMetrics();
		if (Variables.updateCheck) {
			this.getServer().getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
				public void run() {
					checkForUpdates();
				}
			}, 60);
		}
	}

	@Override
	public void onDisable() {
		for (PvPlayer p : playerHandler.getPlayers().values()) {
			if (p.isInCombat())
				p.setTagged(false);
			playerHandler.savePvPState(p.getName(), p.hasPvPEnabled());
		}
	}

	private void loadFiles() {
		new Messages(this);
		this.configM = new ConfigManager(this);
		this.configM.load();
	}

	private void startListeners() {
		if ((Variables.stopCommands && Variables.inCombatEnabled) || Variables.pvpTimerEnabled) {
			Utils.register(new CommandListener(this), this);
		}
		Utils.register(new DamageListener(this), this);
		Utils.register(new PlayerListener(this), this);
		if (Variables.toggleSignsEnabled) {
			Utils.register(new SignListener(playerHandler), this);
		}
	}

	private void startMetrics() {
		new CustomGraph(this);
	}

	private void checkForUpdates() {
		getLogger().info("Checking for updates...");
		Updater updater = new Updater(this, 63773, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, true);
		if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
			Variables.newVersion = updater.getLatestName();
			getLogger().info("Update Available: " + Variables.newVersion);
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

	public WorldTimerManager getWtm() {
		return worldTimerManager;
	}

	public ConfigManager getConfigM() {
		return configM;
	}

	public PlayerHandler getPlayerHandler() {
		return playerHandler;
	}

}