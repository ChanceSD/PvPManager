package me.NoChance.PvPManager.Commands;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPTimer;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Managers.WorldTimerManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PM implements CommandExecutor {

	private PvPManager plugin;
	private WorldTimerManager wtm;

	public PM(PvPManager plugin) {
		this.plugin = plugin;
		this.wtm = plugin.getWtm();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length == 0 && player.hasPermission("pvpmanager.menu")) {
				Variables.helpMenu(player);
				return true;
			}
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("reload") && player.hasPermission("pvpmanager.reload")) {
					reload(player);
					return true;
				} else if (args[0].equalsIgnoreCase("reload")) {
					player.sendMessage(ChatColor.DARK_RED + "You don't have permission!");
					return false;
				}
				if (args[0].equalsIgnoreCase("update") && player.hasPermission("pvpmanager.admin")) {
					if (Variables.updateCheck) {
						if (Variables.update) {
							if (plugin.downloadUpdate())
								player.sendMessage("§2Update Successful. On next restart you will have §e" + Variables.newVersion);
							else
								player.sendMessage("§4An error ocurred while updating, please report to the developer");
						}
						else 
							player.sendMessage("§2You have the latest version: §ePvPManager v" + Variables.currentVersion);
					} else
						player.sendMessage("§4Update Checking is disabled, enable it in the Config file");
					return true;
				}
			}
			if (args.length == 2 && player.hasPermission("pvpmanager.pvptimer")) {
				if (args[0].equalsIgnoreCase("pvpstart")) {
					PvPTimer a = plugin.getWtm().getPvpTimer(player.getWorld());
					a.setStartPvP(Integer.parseInt(args[1]));
					player.sendMessage(ChatColor.DARK_GREEN + "PvP Start Time Changed to " + args[1] + " on World "
							+ player.getWorld().getName());
					a.reload();
					return true;
				}
				if (args[0].equalsIgnoreCase("pvpend")) {
					PvPTimer a = plugin.getWtm().getPvpTimer(player.getWorld());
					a.setEndPvP(Integer.parseInt(args[1]));
					player.sendMessage(ChatColor.DARK_GREEN + "PvP End Time Changed to " + args[1] + " on World "
							+ player.getWorld().getName());
					a.reload();
					return true;
				}
			}
			if (args.length == 3 && player.hasPermission("pvpmanager.pvptimer")) {
				if (args[0].equalsIgnoreCase("pvpstart")) {
					if (wtm.isPvpTimerWorld(args[2])) {
						PvPTimer a = wtm.getPvpTimer(args[2]);
						a.setStartPvP(Integer.parseInt(args[1]));
						player.sendMessage(ChatColor.DARK_GREEN + "PvP Start Time Changed to " + args[1] + " on World "
								+ a.getWorld().getName());
						a.reload();
						return true;
					}
				}
				if (args[0].equalsIgnoreCase("pvpend")) {
					if (wtm.isPvpTimerWorld(args[2])) {
						PvPTimer a = wtm.getPvpTimer(args[2]);
						a.setEndPvP(Integer.parseInt(args[1]));
						player.sendMessage(ChatColor.DARK_GREEN + "PvP End Time Changed to " + args[1] + " on World "
								+ a.getWorld().getName());
						a.reload();
						return true;
					}
				} else if (args[0].equalsIgnoreCase("pvpstart") || args[0].equalsIgnoreCase("pvpend"))
					if (!wtm.isPvpTimerWorld(args[2]))
						player.sendMessage(ChatColor.DARK_RED + "World not found!");
				return false;
			}
		} else {
			if (args.length == 0) {
				sender.sendMessage("[PvPManager] You are not a player, available commands:");
				sender.sendMessage("pm reload");
				sender.sendMessage("pm pvpstart <time> <world>");
				sender.sendMessage("pm pvpend <time> <world>");
				sender.sendMessage("pvp status <player>");
				return true;
			}
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("reload")) {
					reload(sender);
					return true;
				}
			}
			if (args.length == 3) {
				if (args[0].equalsIgnoreCase("pvpstart")) {
					if (wtm.isPvpTimerWorld(args[2])) {
						PvPTimer a = wtm.getPvpTimer(args[2]);
						a.setStartPvP(Integer.parseInt(args[1]));
						sender.sendMessage(ChatColor.DARK_GREEN + "PvP Start Time Changed to " + args[1] + " on World "
								+ a.getWorld().getName());
						a.reload();
						return true;
					}

				}
				if (args[0].equalsIgnoreCase("pvpend")) {
					if (wtm.isPvpTimerWorld(args[2])) {
						PvPTimer a = wtm.getPvpTimer(args[2]);
						a.setEndPvP(Integer.parseInt(args[1]));
						sender.sendMessage(ChatColor.DARK_GREEN + "PvP End Time Changed to " + args[1] + " on World "
								+ a.getWorld().getName());
						a.reload();
						return true;
					}
				}
			} else if (args[0].equalsIgnoreCase("pvpstart") || args[0].equalsIgnoreCase("pvpend"))
				if (!wtm.isPvpTimerWorld(args[2]))
					sender.sendMessage(ChatColor.DARK_RED + "World not found!");
			return false;
		}
		sender.sendMessage(ChatColor.DARK_RED + "You don't have permission or command doesn't exist!");
		return false;
	}

	public void reload(CommandSender player) {
		Variables.update = false;
		plugin.getServer().getScheduler().cancelTasks(plugin);
		plugin.getServer().getPluginManager().disablePlugin(plugin);
		plugin.getServer().getPluginManager().enablePlugin(plugin);
		wtm.reloadPvpTimers();
		player.sendMessage("PvPManager Reloaded!");
	}

}
