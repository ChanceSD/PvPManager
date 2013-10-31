package me.NoChance.PvPManager.Commands;

import me.NoChance.PvPManager.Utils;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Managers.CombatManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PvP implements CommandExecutor {

	private CombatManager cm;

	public PvP(CombatManager combatManager) {
		this.cm = combatManager;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length == 0) {
				if ((player.hasPermission("pvpmanager.pvpstatus.change") && !Variables.toggleSignsEnabled)
						|| ((player.hasPermission("pvpmanager.pvpstatus.change") && Variables.toggleSignsEnabled && !Variables.disableToggleCommand))) {
					if (cm.hasPvpEnabled(player.getName())) {
						cm.disablePvp(player);
						player.sendMessage(Messages.PvP_Disabled);
						return true;
					} else {
						cm.enablePvp(player);
						player.sendMessage(Messages.PvP_Enabled);
						return true;
					}
				} else if (Variables.toggleSignsEnabled && Variables.disableToggleCommand) {
					player.sendMessage(ChatColor.DARK_RED + "This command is disabled! You have to use a Sign!");
					return false;
				}
			}
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("status") && player.hasPermission("pvpmanager.pvpstatus.self")) {
					if (!cm.hasPvpEnabled(player.getName())) {
						player.sendMessage(Messages.Self_Status_Disabled);
						return true;
					} else {
						player.sendMessage(Messages.Self_Status_Enabled);
						return true;
					}
				}
				if ((player.hasPermission("pvpmanager.pvpstatus.change") && !Variables.toggleSignsEnabled)
						|| ((player.hasPermission("pvpmanager.pvpstatus.change") && Variables.toggleSignsEnabled && !Variables.disableToggleCommand))) {
					if (args[0].equalsIgnoreCase("off")) {
						if (cm.hasPvpEnabled(player.getName())) {
							cm.disablePvp(player);
							player.sendMessage(Messages.PvP_Disabled);
							return true;
						} else {
							player.sendMessage(Messages.Already_Disabled);
							return true;
						}
					}
					if (args[0].equalsIgnoreCase("on")) {
						if (!cm.hasPvpEnabled(player.getName())) {
							cm.enablePvp(player);
							player.sendMessage(Messages.PvP_Enabled);
							return true;
						} else {
							player.sendMessage(Messages.Already_Enabled);
							return true;
						}
					}
					if (args[0].equalsIgnoreCase("toggle")) {
						if (cm.hasPvpEnabled(player.getName())) {
							cm.disablePvp(player);
							player.sendMessage(Messages.PvP_Disabled);
							return true;
						} else {
							cm.enablePvp(player);
							player.sendMessage(Messages.PvP_Enabled);
							return true;
						}
					}
				} else if (Variables.toggleSignsEnabled && Variables.disableToggleCommand) {
					player.sendMessage(ChatColor.DARK_RED + "This command is disabled! You have to use a Sign!");
					return false;
				}
			}
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("status") && player.hasPermission("pvpmanager.pvpstatus.others")) {
					if (!cm.hasPvpEnabled(args[1])) {
						player.sendMessage(Messages.Others_Status_Disabled.replace("%p", args[1]));
						return true;
					} else if (Utils.isOnline(args[1]) && cm.hasPvpEnabled(args[1])) {
						player.sendMessage(Messages.Other_Status_Enabled.replace("%p", args[1]));
						return true;
					} else {
						player.sendMessage(ChatColor.DARK_RED + args[1] + " Does Not Exist or is Offline");
						return true;
					}
				}
			}
		} else {
			if (args.length == 1)
				sender.sendMessage("You are not a player!");
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("status")) {
					if (!cm.hasPvpEnabled(args[1])) {
						sender.sendMessage(Messages.Others_Status_Disabled.replace("%p", args[1]));
						return true;
					} else if (Utils.isOnline(args[1]) && cm.hasPvpEnabled(args[1])) {
						sender.sendMessage(Messages.Other_Status_Enabled.replace("%p", args[1]));
						return true;
					} else {
						sender.sendMessage(ChatColor.DARK_RED + args[1] + " Does Not Exist or is Offline");
						return true;
					}
				}
			}
		}
		sender.sendMessage(ChatColor.DARK_RED + "You don't have permission!");
		return false;
	}
}
