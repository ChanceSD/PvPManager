package me.NoChance.PvPManager.Commands;

import org.bukkit.Bukkit;
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
		sender.sendMessage(Messages.getString("PvPInfo_Title"));
		sender.sendMessage(Messages.getString("PvPInfo_Line1") + target.getName());
		sender.sendMessage(Messages.getString("PvPInfo_Line2") + target.getUUID());
		sender.sendMessage(Messages.getString("PvPInfo_Line3") + target.hasPvPEnabled());
		sender.sendMessage(Messages.getString("PvPInfo_Line4") + target.isInCombat());
		sender.sendMessage(Messages.getString("PvPInfo_Line5") + target.isNewbie());
		sender.sendMessage(Messages.getString("PvPInfo_Line6") + target.getPlayer().getWorld().getName());
		sender.sendMessage(Messages.getString("PvPInfo_Line7") + target.hasOverride());
	}

}
