package me.NoChance.PvPManager;

import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Listeners.CommandListener;
import me.NoChance.PvPManager.Listeners.DamageListener;
import me.NoChance.PvPManager.Listeners.PlayerListener;
import me.NoChance.PvPManager.Listeners.SignListener;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class GlobalManager {
	
	private PvPManager plugin;
	private CombatManager combatManager;

	public GlobalManager(PvPManager plugin){
		startListeners();
	}

	private void startListeners() {
		if ((Variables.stopCommands && Variables.inCombatEnabled) || Variables.pvpTimerEnabled) {
			new CommandListener(this);
		}
		if (Variables.pvpTimerEnabled) {
			enablePvPScheduler();
		}
		new DamageListener(this);
		new PlayerListener(this);
		if (Variables.toggleSignsEnabled) {
			new SignListener(this);
		}
	}
	
	private void enablePvPScheduler() {
		for (World w : plugin.getServer().getWorlds()) {
			if (!Variables.worldsExcluded.contains(w.getName())) {
				if (plugin.getConfig().getConfigurationSection("PvP Timer." + w.getName()) == null) {
					ConfigurationSection world = plugin.getConfig().getConfigurationSection("PvP Timer").createSection(w.getName());
					world.set("Start PvP", 13000);
					world.set("End PvP", 0);
					if (Variables.announcePvpOnWorldChange) {
						world.set("On World Change.On", "&4PvP is currently enabled in " + w.getName());
						world.set("On World Change.Off", "&2PvP is currently disabled in " + w.getName());
					}
					plugin.saveConfig();
				}
				if (!plugin.getConfig().isSet("PvP Timer." + w.getName() + ".On World Change")){
					plugin.getConfig().set("PvP Timer." + w.getName() + ".On World Change.On", "&4PvP is currently enabled in " + w.getName());
					plugin.getConfig().set("PvP Timer." + w.getName() + ".On World Change.Off", "&2PvP is currently disabled in " + w.getName());
					plugin.saveConfig();				
				}

				if (!plugin.schedulers.containsKey(w.getName().toLowerCase()))
					plugin.schedulers.put(w.getName().toLowerCase(), new PvPTimer(plugin, w));
			}
		}
	}

	public PvPManager getPlugin() {
		return plugin;
	}

	public CombatManager getCM() {
		return combatManager;
	}
	
	public Player getPlayer(String name){
		return plugin.getServer().getPlayerExact(name);
	}
	
	public boolean isOnline(Player player){
		if(plugin.getServer().getPlayerExact(player.getName()) != null)
			return true;
		else
			return false;
	}
	
	public boolean isOnline(String player){
		if(plugin.getServer().getPlayerExact(player) != null)
			return true;
		else
			return false;
	}
	
}
