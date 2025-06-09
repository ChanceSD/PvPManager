package me.chancesd.pvpmanager.command;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.sdutils.command.ArgumentType;
import me.chancesd.sdutils.command.BaseCommand;
import me.chancesd.sdutils.command.CommandArgument;

public class PvPGlobal extends BaseCommand {

	private final PlayerManager playerManager;

	public PvPGlobal(final PluginCommand pluginCommand, final PlayerManager playerManager) {
		super(pluginCommand);
		this.playerManager = playerManager;
		description("Toggle PvP for the whole server")
				.usage("/pvpglobal <on|off>")
				.permission(Permissions.COMMAND_PVP_GLOBAL.getPermission())
				.argument("status", ArgumentType.STRING).required().tabComplete("ON", "OFF").endArgument();
	}

	@Override
	public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
		final String statusArg = getArgument(args, "status").getValue().toLowerCase();
		final boolean status = statusArg.equals("on");

		playerManager.setGlobalStatus(status);
		sender.sendMessage(Lang.PREFIX.msg() + ChatColor.DARK_GREEN + " Server PvP was set to " + status);
	}
}
