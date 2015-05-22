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

	private final PlayerHandler ph;

	public PvP(final PlayerHandler playerHandler) {
		this.ph = playerHandler;
	}

	public final boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			final PvPlayer pvpPlayer = ph.get(player);
			if (args.length == 0) {
				if ((player.hasPermission("pvpmanager.pvpstatus.change") && !Variables.isToggleSignsEnabled())
						|| ((player.hasPermission("pvpmanager.pvpstatus.change") && Variables.isToggleSignsEnabled() && !Variables.isDisableToggleCommand()))) {
					pvpPlayer.togglePvP();
					return true;
				} else if (Variables.isToggleSignsEnabled() && Variables.isDisableToggleCommand()) {
					player.sendMessage(Messages.getErrorPvpcommandDisabled());
					return false;
				}
			}
		}
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("disable") && sender instanceof Player) {
				final PvPlayer pvpPlayer = ph.get((Player) sender);
				if (pvpPlayer.isNewbie()) {
					pvpPlayer.setNewbie(false);
					return true;
				} else {
					sender.sendMessage(ChatColor.DARK_RED + "You are not protected!");
					return false;
				}
			} else if ((sender.hasPermission("pvpmanager.pvpstatus.change") && !Variables.isToggleSignsEnabled())
					|| ((sender.hasPermission("pvpmanager.pvpstatus.change") && Variables.isToggleSignsEnabled() && !Variables.isDisableToggleCommand()))) {
				if ((args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("on")) && sender instanceof Player) {
					final PvPlayer pvpPlayer = ph.get((Player) sender);
					if (CombatUtils.hasTimePassed(pvpPlayer.getToggleTime(), Variables.getToggleCooldown())) {
						final boolean enable = args[0].equalsIgnoreCase("on") ? true : false;
						if (!enable && pvpPlayer.hasPvPEnabled() || enable && !pvpPlayer.hasPvPEnabled()) {
							pvpPlayer.setPvP(enable);
							return true;
						} else {
							sender.sendMessage(enable ? Messages.getAlreadyEnabled() : Messages.getAlreadyDisabled());
							return true;
						}
					}
					return false;
				} else if (sender.hasPermission("pvpmanager.admin")) {
					if (!CombatUtils.isOnline(args[0])) {
						sender.sendMessage("§4Player not online!");
						return false;
					}
					final PvPlayer specifiedPlayer = ph.get(Bukkit.getPlayer(args[0]));
					final boolean enable = specifiedPlayer.hasPvPEnabled() ? false : true;
					specifiedPlayer.setPvP(enable);
					sender.sendMessage("§6[§8PvPManager§6] §2PvP " + (enable ? "enabled" : "disabled") + " for " + args[0]);
					return true;
				}
			} else if (Variables.isToggleSignsEnabled() && Variables.isDisableToggleCommand()) {
				sender.sendMessage(Messages.getErrorPvpcommandDisabled());
				return false;
			}
		}
		sender.sendMessage(Messages.getErrorCommand());
		return false;
	}

}
