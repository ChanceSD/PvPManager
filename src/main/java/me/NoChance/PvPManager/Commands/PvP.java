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
			if (args[0].equalsIgnoreCase("disable") && sender instanceof Player) {
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
					if (!CombatUtils.isOnline(args[0])) {
						sender.sendMessage("§4Player not online!");
						return false;
					}
					PvPlayer specifiedPlayer = ph.get(Bukkit.getPlayer(args[0]));
					boolean enable = specifiedPlayer.hasPvPEnabled() ? false : true;
					specifiedPlayer.setPvP(enable);
					sender.sendMessage("§6[§8PvPManager§6] §2PvP " + (enable ? "enabled" : "disabled") + " for " + args[0]);
					return true;
				}
			} else if (Variables.toggleSignsEnabled && Variables.disableToggleCommand) {
				sender.sendMessage(Messages.Error_PvPCommand_Disabled);
				return false;
			}
		}
		sender.sendMessage(Messages.Error_Command);
		return false;
	}



}
