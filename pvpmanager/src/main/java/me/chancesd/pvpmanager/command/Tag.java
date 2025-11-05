package me.chancesd.pvpmanager.command;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.sdutils.utils.ChatUtils;
import me.chancesd.sdutils.command.ArgumentType;
import me.chancesd.sdutils.command.BaseCommand;
import me.chancesd.sdutils.command.CommandArgument;

public class Tag extends BaseCommand {

	private static final String ARG_TARGET = "target";
	private static final String ARG_TIME = "time";

	private final PlayerManager ph;

	public Tag(final PluginCommand pluginCommand, final PlayerManager ph) {
		super(pluginCommand);
		this.ph = ph;
		final String defaultCombatTime = String.valueOf(Conf.TIME_IN_COMBAT.asInt());
		description("Tag a player in combat")
				.usage("/tag [player] [time]").permission(Permissions.COMMAND_TAG.getPermission())
				.argument(ARG_TARGET, ArgumentType.PLAYER_OR_ALL).requirePermission(Permissions.COMMAND_TAG_OTHERS.getPermission()).endArgument()
				.argument(ARG_TIME, ArgumentType.INTEGER).defaultValue(defaultCombatTime).dependsOn(ARG_TARGET)
				.tabComplete(defaultCombatTime, "30", "60", "120").endArgument();
	}

	@Override
	public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
		// Handle player checking their own tag status (no args provided)
		if (args.isEmpty()) {
			if (!(sender instanceof final Player player)) {
				ChatUtils.send(sender, "&#FF5555This command can only be used by players when checking your own tag status.");
				return;
			}
			showPlayerTagStatus(ph.get(player));
			return;
		}
		final CommandArgument targetArg = getArgument(args, ARG_TARGET);
		final CommandArgument timeArg = getArgument(args, ARG_TIME);
		final long timeInMs = timeArg.getAsInt() * 1000L;
		
		if (timeInMs <= 0) {
			ChatUtils.send(sender, Lang.PREFIX + " &#FF5555Combat time must be positive (greater than 0)");
			return;
		}

		if (targetArg.isWildcard()) {
			tagAll(sender, timeInMs);
		} else {
			final Player targetPlayer = targetArg.getAsPlayerOrWildcard();
			tagPlayer(sender, targetPlayer.getName(), timeInMs);
		}
	}

	private void showPlayerTagStatus(final CombatPlayer pvPlayer) {
		if (!pvPlayer.isInCombat()) {
			pvPlayer.message(Lang.TAG_NOT_IN_COMBAT.msg());
			return;
		}
		pvPlayer.message(Lang.TAG_TIME_LEFT.msgTime(pvPlayer.getTagTimeLeft()));
	}

	private void tagAll(final CommandSender sender, final long time) {
		for (final CombatPlayer player : ph.getPlayers().values()) {
			player.tag(true, player, time);
		}
		ChatUtils.send(sender, Lang.PREFIX + " &aAll players have been tagged");
	}

	private void tagPlayer(final CommandSender sender, final String name, final long time) {
		final CombatPlayer target = ph.get(Bukkit.getPlayer(name));
		if (target.isInCombat()) {
			ChatUtils.send(sender, Lang.PREFIX + " &#FF5555That player is already in combat");
			return;
		}
		target.tag(true, target, time);
		ChatUtils.send(sender, Lang.PREFIX + " &f" + target.getName() + " &ahas been tagged for &f" + time / 1000 + " &aseconds");
	}
}
