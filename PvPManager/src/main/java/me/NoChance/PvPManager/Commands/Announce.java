package me.NoChance.PvPManager.Commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Announce implements CommandExecutor {

	@Override
	public final boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', StringUtils.join(args, " ")));
		return true;
	}

}
