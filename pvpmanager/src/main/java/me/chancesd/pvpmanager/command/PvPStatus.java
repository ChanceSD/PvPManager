package me.chancesd.pvpmanager.command;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.sdutils.command.ArgumentType;
import me.chancesd.sdutils.command.BaseCommand;
import me.chancesd.sdutils.command.CommandArgument;

public class PvPStatus extends BaseCommand {

	private final PlayerManager playerManager;

	public PvPStatus(final PluginCommand pluginCommand, final PlayerManager playerManager) {
		super(pluginCommand);
		this.playerManager = playerManager;
		this.description("Check PvP combat status")
				.usage("/pvpstatus [player]").permission("pvpmanager.pvpstatus.self")
				.argument("target", ArgumentType.PLAYER).requirePermission(Permissions.COMMAND_PVP_TOGGLE_OTHERS.getPermission()).endArgument();
	}

	@Override
	public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
		if (args.isEmpty()) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(Lang.ERROR_NOT_PLAYER.msg());
				return;
			}

			final Player player = (Player) sender;
			final CombatPlayer pvpPlayer = playerManager.get(player);
			if (!pvpPlayer.hasPvPEnabled()) {
				pvpPlayer.message(Lang.SELF_STATUS_DISABLED);
			} else {
				pvpPlayer.message(Lang.SELF_STATUS_ENABLED);
			}
			return;
		}
		final CommandArgument targetArg = getArgument(args, "target");
		final Player targetPlayer = targetArg.getAsPlayerOrNull();
		final CombatPlayer target = playerManager.get(targetPlayer);
		if (!target.hasPvPEnabled()) {
			sender.sendMessage(Lang.OTHERS_STATUS_DISABLED.msg(targetPlayer.getName()));
		} else {
			sender.sendMessage(Lang.OTHER_STATUS_ENABLED.msg(targetPlayer.getName()));
		}
	}
}
