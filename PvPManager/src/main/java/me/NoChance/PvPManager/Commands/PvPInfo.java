package me.NoChance.PvPManager.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Utils.CombatUtils;

public class PvPInfo implements CommandExecutor {

	private final PlayerHandler ph;

	public PvPInfo(final PlayerHandler ph) {
		this.ph = ph;
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (args.length == 0 && sender instanceof Player) {
			sendInfo(sender, ph.get((Player) sender));
			return true;
		} else if (args.length == 1 && sender.hasPermission("pvpmanager.info.others")) {
			if (CombatUtils.isOnline(args[0])) {
				sendInfo(sender, ph.get(Bukkit.getPlayer(args[0])));
				return true;
			}
			sender.sendMessage(Messages.getErrorPlayerNotFound().replace("%p", args[0]));
			return true;
		}
		return false;
	}

	private void sendInfo(final CommandSender sender, final PvPlayer target) {
		sender.sendMessage(ChatColor.YELLOW + "§lPvPManager Info");
		sender.sendMessage(ChatColor.GREEN + "- Name: §f" + target.getName());
		sender.sendMessage(ChatColor.GREEN + "- UUID: §f" + target.getUUID());
		sender.sendMessage(ChatColor.GREEN + "- PvP Status: §f" + target.hasPvPEnabled());
		sender.sendMessage(ChatColor.GREEN + "- In Combat: §f" + target.isInCombat());
		sender.sendMessage(ChatColor.GREEN + "- Newbie: §f" + target.isNewbie());
		sender.sendMessage(ChatColor.GREEN + "- World: §f" + target.getPlayer().getWorld().getName());
		sender.sendMessage(ChatColor.GREEN + "- Override PvP: §f" + target.hasOverride());
	}

}
