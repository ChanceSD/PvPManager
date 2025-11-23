package me.chancesd.pvpmanager.command;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.sdutils.command.BaseCommand;
import me.chancesd.sdutils.command.CommandArgument;

public class PvPOverride extends BaseCommand {

	private final PlayerManager ph;

	public PvPOverride(final PluginCommand pluginCommand, final PlayerManager ph) {
		super(pluginCommand);
		this.ph = ph;
		this.description("Override all PvP protections")
				.usage("/pvpoverride").permission(Permissions.COMMAND_OVERRIDE.getPermission()).playerOnly();
	}

	@Override
	public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
		final Player player = (Player) sender;
		final CombatPlayer pvPlayer = ph.get(player);
		final boolean override = pvPlayer.toggleOverride();
		pvPlayer.message(Lang.PREFIX.msg() + ChatColor.DARK_GREEN + " PvP Override set to: " + ChatColor.GOLD + override);
	}
}
