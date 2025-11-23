package me.chancesd.pvpmanager.command;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.lang.Replacement;
import me.chancesd.sdutils.command.ArgumentType;
import me.chancesd.sdutils.command.BaseCommand;
import me.chancesd.sdutils.command.CommandArgument;
import me.chancesd.sdutils.utils.ChatUtils;

public class Announce extends BaseCommand {

	public Announce(final PluginCommand pluginCommand) {
		super(pluginCommand);
		this.description("Broadcasts a message to all players")
				.permission(Permissions.COMMAND_ANNOUNCE.getPermission())
				.argument("message", ArgumentType.STRING_ARRAY).required().endArgument();
	}

	@Override
	public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
		final String message = getArgument(args, "message").getValue();

		// Broadcast the message to all players
		final String colorizedMessage = ChatUtils.colorize(message).replace(Replacement.PREFIX.getPlaceholder(), Lang.PREFIX.msg());
		Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(colorizedMessage));

		// Also send to console
		Bukkit.getConsoleSender().sendMessage(colorizedMessage);
	}

}
