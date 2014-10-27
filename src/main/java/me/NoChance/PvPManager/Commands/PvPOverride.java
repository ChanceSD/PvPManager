package me.NoChance.PvPManager.Commands;

import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Managers.PlayerHandler;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PvPOverride implements CommandExecutor {

	private PlayerHandler ph;

	public PvPOverride(PlayerHandler ph) {
		this.ph = ph;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			if (sender.hasPermission("pvpmanager.override")) {
				ph.get((Player) sender).toggleOverride();
			} else
				sender.sendMessage(Messages.Error_Permission);
		}
		return true;
	}

}
