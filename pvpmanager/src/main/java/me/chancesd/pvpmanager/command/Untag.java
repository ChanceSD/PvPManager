package me.chancesd.pvpmanager.command;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.UntagReason;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.sdutils.command.ArgumentType;
import me.chancesd.sdutils.command.BaseCommand;
import me.chancesd.sdutils.command.CommandArgument;

public class Untag extends BaseCommand {

	private final PlayerManager playerManager;

	public Untag(final PluginCommand pluginCommand, final PlayerManager playerManager) {
		super(pluginCommand);
		this.playerManager = playerManager;
		this.permission(Permissions.COMMAND_UNTAG.getPermission())
				.description("Untags a player")
				.usage("/untag <player|*>").argument("target", ArgumentType.PLAYER_OR_ALL).required().endArgument();
	}

	@Override
	public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
		final CommandArgument targetArg = getArgument(args, "target");

		if (targetArg.isAllPlayers()) {
			untagAll(sender);
			return;
		}
		final Player targetPlayer = targetArg.getAsPlayerOrNull();
		final CombatPlayer combatPlayer = playerManager.get(targetPlayer);
		if (!combatPlayer.isInCombat()) {
			sender.sendMessage(Lang.PREFIX + " §cThat player is not in combat");
			return;
		}
		combatPlayer.untag(UntagReason.COMMAND);
		sender.sendMessage(Lang.PREFIX + " " + combatPlayer.getName() + " §2has been untagged");
	}

	private void untagAll(final CommandSender sender) {
		int untaggedCount = 0;
		for (final CombatPlayer player : playerManager.getPlayers().values()) {
			if (player.isInCombat()) {
				player.untag(UntagReason.COMMAND);
				untaggedCount++;
			}
		}

		if (untaggedCount == 0) {
			sender.sendMessage(Lang.PREFIX + " §eNo players were in combat");
		} else {
			sender.sendMessage(Lang.PREFIX + " §aAll players have been untagged");
		}
	}
}
