package me.NoChance.PvPManager.Commands;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPTimer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PM implements CommandExecutor {

	private PvPManager plugin;

	public PM(PvPManager plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length == 0) {
				plugin.variables.helpMenu(player);
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
			}
			if (args.length == 2 && player.hasPermission("pvpmanager.*")) {
				if (args[0].equalsIgnoreCase("pvpstart")) {
					PvPTimer a = plugin.schedulers.get(player.getWorld().getName().toLowerCase());
					a.setStartPvP(Integer.parseInt(args[1]));
					player.sendMessage(ChatColor.DARK_GREEN + "PvP Start Time Changed to " + args[1] + " on World "
							+ player.getWorld().getName());
					a.reload();
					return true;
				}
				if (args[0].equalsIgnoreCase("pvpend")) {
					PvPTimer a = plugin.schedulers.get(player.getWorld().getName().toLowerCase());
					a.setEndPvP(Integer.parseInt(args[1]));
					player.sendMessage(ChatColor.DARK_GREEN + "PvP End Time Changed to " + args[1] + " on World "
							+ player.getWorld().getName());
					a.reload();
					return true;
				}
			}
			if (args.length == 3 && player.hasPermission("pvpmanager.*")) {
				if (args[0].equalsIgnoreCase("pvpstart")) {
					if (plugin.schedulers.containsKey(args[2].toLowerCase())) {
						PvPTimer a = plugin.schedulers.get(args[2].toLowerCase());
						a.setStartPvP(Integer.parseInt(args[1]));
						player.sendMessage(ChatColor.DARK_GREEN + "PvP Start Time Changed to " + args[1] + " on World "
								+ a.w.getName());
						a.reload();
						return true;
					}
				}
				if (args[0].equalsIgnoreCase("pvpend")) {
					if (plugin.schedulers.containsKey(args[2].toLowerCase())) {
						PvPTimer a = plugin.schedulers.get(args[2].toLowerCase());
						a.setEndPvP(Integer.parseInt(args[1]));
						player.sendMessage(ChatColor.DARK_GREEN + "PvP End Time Changed to " + args[1] + " on World "
								+ a.w.getName());
						a.reload();
						return true;
					}
				} else if (args[0].equalsIgnoreCase("pvpstart") || args[0].equalsIgnoreCase("pvpend"))
					if (!plugin.schedulers.containsKey(args[2].toLowerCase()))
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
					if (plugin.schedulers.containsKey(args[2].toLowerCase())) {
						PvPTimer a = plugin.schedulers.get(args[2].toLowerCase());
						a.setStartPvP(Integer.parseInt(args[1]));
						sender.sendMessage(ChatColor.DARK_GREEN + "PvP Start Time Changed to " + args[1] + " on World "
								+ a.w.getName());
						a.reload();
						return true;
					}

				}
				if (args[0].equalsIgnoreCase("pvpend")) {
					if (plugin.schedulers.containsKey(args[2].toLowerCase())) {
						PvPTimer a = plugin.schedulers.get(args[2].toLowerCase());
						a.setEndPvP(Integer.parseInt(args[1]));
						sender.sendMessage(ChatColor.DARK_GREEN + "PvP End Time Changed to " + args[1] + " on World "
								+ a.w.getName());
						a.reload();
						return true;
					}
				}
			} else if (args[0].equalsIgnoreCase("pvpstart") || args[0].equalsIgnoreCase("pvpend"))
				if (!plugin.schedulers.containsKey(args[2].toLowerCase()))
					sender.sendMessage(ChatColor.DARK_RED + "World not found!");
			return false;
		}
		sender.sendMessage(ChatColor.DARK_RED + "You don't have permission!");
		return false;
	}

	public void reload(CommandSender player) {
		plugin.getServer().getPluginManager().disablePlugin(plugin);
		plugin.getServer().getPluginManager().enablePlugin(plugin);
		for (PvPTimer a : plugin.schedulers.values()) {
			a.reload();
		}
		player.sendMessage("PvPManager Reloaded!");
	}

}
