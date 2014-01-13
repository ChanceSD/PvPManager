package me.NoChance.PvPManager;

import me.NoChance.PvPManager.Commands.*;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Listeners.*;
import me.NoChance.PvPManager.Managers.CombatManager;
import me.NoChance.PvPManager.Managers.ConfigManager;
import me.NoChance.PvPManager.Managers.PunishmentsManager;
import me.NoChance.PvPManager.Managers.WorldTimerManager;
import me.NoChance.PvPManager.Others.CustomGraph;
import me.NoChance.PvPManager.Others.Updater;
import me.NoChance.PvPManager.Others.Utils;
import me.NoChance.PvPManager.Others.Updater.UpdateResult;
import org.bukkit.plugin.java.JavaPlugin;

public final class PvPManager extends JavaPlugin {

	private ConfigManager configM;
	private CombatManager combatManager;
	private WorldTimerManager worldTimerManager;
	private PunishmentsManager punishmentsManager;

	@Override
	public void onEnable() {
		loadFiles();
		if (Variables.pvpTimerEnabled) {
			this.worldTimerManager = new WorldTimerManager(this);
		}
		this.combatManager = new CombatManager(this);
		this.punishmentsManager = new PunishmentsManager(this);
		startListeners();
		this.configM.loadUsers();
		getCommand("pvp").setExecutor(new PvP(combatManager));
		getCommand("pvpmanager").setExecutor(new PM(this));
		new CustomGraph(this);
		if (Variables.updateCheck) {
			checkForUpdates();
		}
	}

	@Override
	public void onDisable() {
		combatManager.getInCombat().clear();
		this.configM.saveUsers();
		this.configM.save();
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
			Utils.register(new SignListener(this), this);
		}
	}

	private void checkForUpdates() {
		if (!Variables.autoUpdate) {
			Updater updater = new Updater(this, 63773, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
			if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
				Variables.newVersion = updater.getLatestName();
				getLogger().info("Update Available: " + Variables.newVersion);
				Variables.update = true;
				getLogger().info("Link: http://dev.bukkit.org/bukkit-plugins/pvpmanager/");
			}
		} else
			downloadUpdate();
	}

	public boolean downloadUpdate() {
		Updater updater = new Updater(this, 63773, this.getFile(), Updater.UpdateType.NO_VERSION_CHECK, false);
		return updater.getResult() == UpdateResult.SUCCESS;
	}

	public CombatManager getCm() {
		return combatManager;
	}

	public WorldTimerManager getWtm() {
		return worldTimerManager;
	}

	public PunishmentsManager getPm() {
		return punishmentsManager;
	}

	public ConfigManager getConfigM() {
		return configM;
	}

}