package me.NoChance.PvPManager.Commands;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Utils.CombatUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PvPStatus implements CommandExecutor {

	private PlayerHandler ph;

	public PvPStatus(PlayerHandler ph) {
		this.ph = ph;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		PvPlayer target;
		if (args.length == 0 && sender instanceof Player) {
			Player player = (Player) sender;
			if (!ph.get(player).hasPvPEnabled()) {
				player.sendMessage(Messages.Self_Status_Disabled);
				return true;
			} else {
				player.sendMessage(Messages.Self_Status_Enabled);
				return true;
			}
		} else if (args.length == 1) {
			if (CombatUtils.isOnline(args[0])) {
				target = ph.get(Bukkit.getPlayer(args[0]));
				if (!target.hasPvPEnabled()) {
					sender.sendMessage(Messages.Others_Status_Disabled.replace("%p", args[0]));
					return true;
				} else {
					sender.sendMessage(Messages.Other_Status_Enabled.replace("%p", args[0]));
					return true;
				}
			} else {
				sender.sendMessage(ChatColor.DARK_RED + args[0] + " Does Not Exist or is Offline");
				return true;
			}
		}
		return false;
	}

}
