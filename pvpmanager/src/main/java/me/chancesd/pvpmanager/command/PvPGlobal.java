package me.chancesd.pvpmanager.command;

import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.google.common.collect.Lists;

import me.chancesd.pvpmanager.setting.Messages;
import me.chancesd.pvpmanager.setting.Settings;
import me.chancesd.pvpmanager.utils.ChatUtils;

public class PvPGlobal implements TabExecutor {

	@Override
	public final boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 1) {
			final boolean status = args[0].equalsIgnoreCase("on");
			Settings.setGlobalStatus(status);
			sender.sendMessage(Messages.PREFIXMSG + ChatColor.DARK_GREEN + " Server PvP was set to " + status);
			return true;
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length != 1)
			return Collections.emptyList();
		return ChatUtils.getMatchingEntries(args[0], Lists.newArrayList("ON", "OFF"));
	}

}
