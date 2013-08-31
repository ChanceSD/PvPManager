package me.NoChance.PvPManager;

import java.util.HashSet;

import me.NoChance.PvPManager.Listeners.CommandListener;
import me.NoChance.PvPManager.Listeners.DamageListener;
import me.NoChance.PvPManager.Listeners.PlayerListener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PvPManager extends JavaPlugin {

	public HashSet<String> playersStatusOff = new HashSet<String>();
	public HashSet<String> inCombat = new HashSet<String>();
	private ConfigManager configM;
	public Variables variables;

	@Override
	public void onEnable() {
		loadFiles();
		if (Variables.stopCommands && Variables.inCombatEnabled) {
			new CommandListener(this);
		}
		new DamageListener(this);
		new PlayerListener(this);
		new CustomGraph(this);
	}

	@Override
	public void onDisable() {
		this.configM.saveUsers();
		this.configM.save();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (label.equalsIgnoreCase("pm") && sender instanceof Player) { 
			Player player = (Player) sender;
			if (args.length == 0) {
				variables.helpMenu(player);
				return true;
			}
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("reload") 
						&& player.hasPermission("pvpmanager.reload")) { 
					reload(player);
					return true;
				} else if (args[0].equalsIgnoreCase("reload")) { 
					player.sendMessage(ChatColor.DARK_RED
							+ "You don't have permission!"); 
					return false;
				}
			}
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("pvp")) { 
					if (args[1].equalsIgnoreCase("off") 
							&& player
									.hasPermission("pvpmanager.pvpstatus.change")) { 
						if (hasPvpEnabled(player.getName())) {
							playersStatusOff.add(player.getName());
							player.sendMessage(Messages.PvP_Disabled); 
							return true;
						} else {
							player.sendMessage(Messages.Already_Disabled); 
							return true;
						}
					}
					if (args[1].equalsIgnoreCase("on") 
							&& player
									.hasPermission("pvpmanager.pvpstatus.change")) { 
						if (!hasPvpEnabled(player.getName())) {
							playersStatusOff.remove(player.getName());
							player.sendMessage(Messages.PvP_Enabled); 
							return true;
						} else {
							player.sendMessage(Messages.Already_Enabled); 
							return true;
						}
					}
					if (args[1].equalsIgnoreCase("status") 
							&& player
									.hasPermission("pvpmanager.pvpstatus.self")) { 
						if (!hasPvpEnabled(player.getName())) {
							player.sendMessage(Messages.Self_Status_Disabled); 
							return true;
						} else {
							player.sendMessage(Messages.Self_Status_Enabled); 
							return true;
						}
					}
				}
				return false;
			}
			if (args.length == 3) {
				if (args[0].equalsIgnoreCase("pvp") 
						&& args[1].equalsIgnoreCase("status") 
						&& player.hasPermission("pvpmanager.pvpstatus.others")) { 
					if (!hasPvpEnabled(args[2])) {
						player.sendMessage(Messages.Others_Status_Disabled.replace("%p", args[2]));
						return true;
					} else if (getServer().getPlayerExact(args[2]) != null
							&& hasPvpEnabled(args[2])) {
						player.sendMessage(Messages.Other_Status_Enabled.replace("%p", args[2])); 
						return true;
					} else {
						player.sendMessage(ChatColor.DARK_RED + args[2]
								+ " Does Not Exist or is Offline"); 
						return true;
					}
				}
			} else {
				return false;
			}
		}
		return false;
	}

	public void loadFiles(){
		if(getConfig().getInt("Config Version") == 0){
			getConfig().options().copyDefaults(true);
			this.saveConfig();
		}
		this.saveDefaultConfig();
		this.reloadConfig();
		this.configM = new ConfigManager(this);
		this.configM.load();
		this.configM.loadUsers();
		variables = new Variables(this);
		Messages messages = new Messages(this);
		messages.load();
	}
	
	private void reload(Player player) {
		Bukkit.getServer().getPluginManager().disablePlugin(this);
		Bukkit.getServer().getPluginManager().enablePlugin(this);
		player.sendMessage("PvPManager Reloaded!"); 
	}

	public boolean hasPvpEnabled(String name) {
		for (String n : playersStatusOff) {
			if (n.equalsIgnoreCase(name))
				return false;
		}
		return true;
	}

}