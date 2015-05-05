package me.NoChance.PvPManager.Commands;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Utils.CombatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PvP implements CommandExecutor {

	private PlayerHandler ph;

	public PvP(PlayerHandler playerHandler) {
		this.ph = playerHandler;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			PvPlayer pvpPlayer = ph.get(player);
			if (args.length == 0) {
				if ((player.hasPermission("pvpmanager.pvpstatus.change") && !Variables.toggleSignsEnabled)
						|| ((player.hasPermission("pvpmanager.pvpstatus.change") && Variables.toggleSignsEnabled && !Variables.disableToggleCommand))) {
					pvpPlayer.togglePvP();
					return true;
				} else if (Variables.toggleSignsEnabled && Variables.disableToggleCommand) {
					player.sendMessage(Messages.Error_PvPCommand_Disabled);
					return false;
				}
			}
		}
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("status") && sender.hasPermission("pvpmanager.pvpstatus.self")) {
				PvPlayer target;
				if (args.length == 1 && sender instanceof Player) {
					Player player = (Player) sender;
					if (!ph.get(player).hasPvPEnabled()) {
						player.sendMessage(Messages.Self_Status_Disabled);
						return true;
					} else {
						player.sendMessage(Messages.Self_Status_Enabled);
						return true;
					}
				} else {
					if (isOnline(args[1])) {
						target = ph.get(Bukkit.getPlayer(args[1]));
						if (!target.hasPvPEnabled()) {
							sender.sendMessage(Messages.Others_Status_Disabled.replace("%p", args[1]));
							return true;
						} else {
							sender.sendMessage(Messages.Other_Status_Enabled.replace("%p", args[1]));
							return true;
						}
					} else {
						sender.sendMessage(ChatColor.DARK_RED + args[1] + " Does Not Exist or is Offline");
						return true;
					}

				}
			} else if (args[0].equalsIgnoreCase("list") && sender.hasPermission("pvpmanager.list")) {
				sender.sendMessage(ChatColor.GOLD + "**** Players With PvP Enabled ****");
				sender.sendMessage(ChatColor.DARK_GRAY + pvpList());
				return true;
			} else if (args[0].equalsIgnoreCase("info") && sender.hasPermission("pvpmanager.info")) {
				if (args.length == 1 && sender instanceof Player) {
					sendInfo(sender, ph.get((Player) sender));
					return true;
				} else {
					if (isOnline(args[1])) {
						sendInfo(sender, ph.get(Bukkit.getPlayer(args[1])));
						return true;
					} else {
						sender.sendMessage(ChatColor.DARK_RED + args[1] + " Does Not Exist or is Offline");
						return true;
					}
				}
			} else if (args[0].equalsIgnoreCase("disable") && sender instanceof Player) {
				PvPlayer pvpPlayer = ph.get((Player) sender);
				if (pvpPlayer.isNewbie()) {
					pvpPlayer.setNewbie(false);
					return true;
				} else {
					sender.sendMessage(ChatColor.DARK_RED + "You are not protected!");
					return false;
				}
			} else if ((sender.hasPermission("pvpmanager.pvpstatus.change") && !Variables.toggleSignsEnabled)
					|| ((sender.hasPermission("pvpmanager.pvpstatus.change") && Variables.toggleSignsEnabled && !Variables.disableToggleCommand))) {
				if ((args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("on")) && sender instanceof Player) {
					PvPlayer pvpPlayer = ph.get((Player) sender);
					if (CombatUtils.hasTimePassed(pvpPlayer.getToggleTime(), Variables.toggleCooldown)) {
						boolean enable = args[0].equalsIgnoreCase("on") ? true : false;
						if (!enable && pvpPlayer.hasPvPEnabled() || enable && !pvpPlayer.hasPvPEnabled()) {
							pvpPlayer.setPvP(enable);
							return true;
						} else {
							sender.sendMessage(enable ? Messages.Already_Enabled : Messages.Already_Disabled);
							return true;
						}
					}
					return false;
				} else if (sender.hasPermission("pvpmanager.admin")) {
					if (!isOnline(args[0])) {
						sender.sendMessage("§4Player not online!");
						return false;
					}
					PvPlayer specifiedPlayer = ph.get(Bukkit.getPlayer(args[0]));
					boolean enable = specifiedPlayer.hasPvPEnabled() ? false : true;
					if (specifiedPlayer.hasPvPEnabled()) {
						specifiedPlayer.setPvP(enable);
						sender.sendMessage("§6[§8PvPManager§6] §2PvP " + (enable ? "enabled" : "disabled") + " for " + args[0]);
						return true;
					}
				}
			} else if (Variables.toggleSignsEnabled && Variables.disableToggleCommand) {
				sender.sendMessage(Messages.Error_PvPCommand_Disabled);
				return false;
			}
		}
		sender.sendMessage(Messages.Error_Command);
		return false;
	}

	private void sendInfo(CommandSender sender, PvPlayer target) {
		sender.sendMessage(ChatColor.YELLOW + "§lPvPManager Info");
		sender.sendMessage(ChatColor.GREEN + "- Name: §f" + target.getName());
		sender.sendMessage(ChatColor.GREEN + "- PvP Status: §f" + target.hasPvPEnabled());
		sender.sendMessage(ChatColor.GREEN + "- Tagged: §f" + target.isInCombat());
		sender.sendMessage(ChatColor.GREEN + "- Newbie: §f" + target.isNewbie());
		sender.sendMessage(ChatColor.GREEN + "- World: §f" + target.getWorldName());
		sender.sendMessage(ChatColor.GREEN + "- Override: §f" + target.hasOverride());
	}

	private String pvpList() {
		StringBuilder list = new StringBuilder();
		for (PvPlayer p : ph.getPlayers().values()) {
			if (p.hasPvPEnabled()) {
				list.append(p.getName() + ", ");
			}
		}
		if (list.toString().isEmpty())
			return "No Players with PvP Enabled";
		list.delete(list.length() - 2, list.length());
		return list.toString();
	}

	private boolean isOnline(String name) {
		return Bukkit.getPlayer(name) != null;
	}
}
