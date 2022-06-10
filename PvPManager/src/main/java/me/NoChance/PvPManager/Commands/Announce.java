package me.NoChance.PvPManager.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.NoChance.PvPManager.Utils.ChatUtils;

public class Announce implements CommandExecutor {

	@Override
	public final boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		Bukkit.broadcastMessage(ChatUtils.colorize(String.join(" ", args)));
		return true;
	}

}
