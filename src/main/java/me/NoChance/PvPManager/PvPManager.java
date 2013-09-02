package me.NoChance.PvPManager;

import java.util.ArrayList;
import java.util.HashSet;
import me.NoChance.PvPManager.Config.ConfigManager;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Listeners.CommandListener;
import me.NoChance.PvPManager.Listeners.DamageListener;
import me.NoChance.PvPManager.Listeners.PlayerListener;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PvPManager extends JavaPlugin {

	public HashSet<String> playersStatusOff = new HashSet<String>();
	public HashSet<String> inCombat = new HashSet<String>();
	public ConfigManager configM;
	public Variables variables;
	public ArrayList<PvPTimer> schedulers = new ArrayList<PvPTimer>();

	@Override
	public void onEnable() {
		loadFiles();
		if (Variables.stopCommands && Variables.inCombatEnabled
				|| Variables.pvpTimerEnabled) {
			new CommandListener(this);
		}
		if (Variables.pvpTimerEnabled) {
			enablePvPScheduler();
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
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (label.equalsIgnoreCase("pm")) {
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
			}
			if (label.equalsIgnoreCase("pvp")) {
				if (args.length == 1) {
					if (args[0].equalsIgnoreCase("off")
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
					if (args[0].equalsIgnoreCase("on")
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
					if (args[0].equalsIgnoreCase("status")
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

					return false;
				}
				if (args.length == 2) {
					if (args[0].equalsIgnoreCase("status")
							&& player.hasPermission("pvpmanager.pvpstatus.others")) {
						if (!hasPvpEnabled(args[1])) {
							player.sendMessage(Messages.Others_Status_Disabled.replace("%p", args[1]));
							return true;
						} else if (getServer().getPlayerExact(args[1]) != null
								&& hasPvpEnabled(args[1])) {
							player.sendMessage(Messages.Other_Status_Enabled.replace("%p", args[1]));
							return true;
						} else {
							player.sendMessage(ChatColor.DARK_RED + args[1] + " Does Not Exist or is Offline");
							return true;
						}
					}
				} else {
					return false;
				}
			}
		}
		return false;
	}

	public void loadFiles() {
		if (getConfig().getInt("Config Version") == 0 || getConfig().getInt("Config Version") < 2) {
			getConfig().options().copyDefaults(true);
			getConfig().set("Config Version", 2);
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
		getServer().getPluginManager().disablePlugin(this);
		getServer().getPluginManager().enablePlugin(this);
		for (PvPTimer a : schedulers) {
			a.reload();
		}
		player.sendMessage("PvPManager Reloaded!");
	}

	public boolean hasPvpEnabled(String name) {
		for (String n : playersStatusOff) {
			if (n.equalsIgnoreCase(name))
				return false;
		}
		return true;
	}

	public void enablePvPScheduler() {
		for (World w : getServer().getWorlds()) {
			if (!Variables.worldsExcluded.contains(w.getName())) {
				if (getConfig().getConfigurationSection("PvP Timer." + w.getName()) == null) {
					getConfig().getConfigurationSection("PvP Timer").createSection(w.getName());
					getConfig().set("PvP Timer." + w.getName() + ".Start PvP", 13000);
					getConfig().set("PvP Timer." + w.getName() + ".End PvP", 0);
				}
				if (!schedulers.isEmpty()) {
					for (PvPTimer a : schedulers) {
						if (!a.w.equals(w))
							schedulers.add(new PvPTimer(this, w));
					}
				} else
					schedulers.add(new PvPTimer(this, w));
			}
		}
	}

}