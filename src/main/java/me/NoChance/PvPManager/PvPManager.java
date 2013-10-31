package me.NoChance.PvPManager;

import me.NoChance.PvPManager.Updater.UpdateResult;
import me.NoChance.PvPManager.Commands.*;
import me.NoChance.PvPManager.Config.*;
import me.NoChance.PvPManager.Listeners.*;
import me.NoChance.PvPManager.Managers.CombatManager;
import me.NoChance.PvPManager.Managers.ConfigManager;
import me.NoChance.PvPManager.Managers.WorldTimerManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PvPManager extends JavaPlugin {

	private ConfigManager configM;
	public boolean update;
	public String newVersion;
	private CombatManager combatManager;
	private WorldTimerManager worldTimerManager;

	@Override
	public void onEnable() {
		loadFiles();
		if (Variables.pvpTimerEnabled) {
			this.worldTimerManager = new WorldTimerManager(this);
		}
		startListeners();
		this.combatManager = new CombatManager(this);
		getCommand("pvp").setExecutor(new PvP(combatManager));
		getCommand("pm").setExecutor(new PM(this));
		new CustomGraph(this);
		if (Variables.updateCheck) {
			getLogger().info("Checking for updates...");
			Updater updater = new Updater(this, 63773, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, true);
			if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
				update = true;
				newVersion = updater.getLatestName();
				getLogger().info("Update Available: " + newVersion);
				getLogger().info("Link: http://dev.bukkit.org/bukkit-plugins/pvpmanager/");
			} else
				getLogger().info("No update found");
		}
	}

	@Override
	public void onDisable() {
		this.configM.saveUsers();
		this.configM.save();
	}

	public void loadFiles() {
		new Messages(this);
		if (getConfig().getInt("Config Version") == 0 || getConfig().getInt("Config Version") < 7) {
			getConfig().options().copyDefaults(true);
			getConfig().set("Config Version", 7);
			this.saveConfig();
		}
		this.saveDefaultConfig();
		this.reloadConfig();
		this.configM = new ConfigManager(this);
		this.configM.load();
		this.configM.loadUsers();
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
	
	public CombatManager getCm(){
		return combatManager;
	}

	public WorldTimerManager getWtm() {
		return worldTimerManager;
	}

}