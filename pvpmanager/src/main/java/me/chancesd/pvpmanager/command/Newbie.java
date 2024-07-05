package me.chancesd.pvpmanager.command;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.utils.ChatUtils;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.sdutils.utils.TimeUtil;

public class Newbie implements TabExecutor {
	private final PlayerManager ph;

	public Newbie(final PlayerManager ph) {
		this.ph = ph;
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 0 && sender instanceof final Player player) {
			final CombatPlayer pvPlayer = ph.get(player);
			if (!pvPlayer.isNewbie()) {
				pvPlayer.message(Lang.ERROR_NOT_NEWBIE.msg());
			} else {
				final long timeLeft = pvPlayer.getNewbieTimeLeft();
				pvPlayer.message(Lang.NEWBIE_TIME_CHECK.msgTime(timeLeft));
			}
			return true;
		} else if (args.length >= 1) {
			final String subcommand = args[0];
			if (Conf.NEWBIE_ALLOW_DISABLE.asBool() && subcommand.equalsIgnoreCase("disable") && sender instanceof final Player player) {
				final CombatPlayer combatPlayer = ph.get(player);
				if (combatPlayer.isNewbie()) {
					combatPlayer.setNewbie(false);
					return true;
				}
				combatPlayer.message(Lang.ERROR_NOT_NEWBIE.msg());
				return true;
			} else if (Permissions.ADMIN.hasPerm(sender) && args.length == 2) {
				if (subcommand.equalsIgnoreCase("checktime")) {
					checkNewbieTime(sender, args[1]);
					return true;
				} else if (subcommand.equalsIgnoreCase("add")) {
					addNewbie(sender, args[1]);
					return true;
				} else if (subcommand.equalsIgnoreCase("remove")) {
					removeNewbie(sender, args[1]);
					return true;
				}
			}
		} else {
			sender.sendMessage("This command is only available for players.");
		}
		return false;
	}

	private void checkNewbieTime(final CommandSender sender, final String targetPlayerName) {
		if (!CombatUtils.isOnlineWithFeedback(sender, targetPlayerName))
			return;

		final CombatPlayer specifiedPlayer = ph.get(Bukkit.getPlayer(targetPlayerName));
		final long timeLeft = specifiedPlayer.getNewbieTimeLeft();
		sender.sendMessage(Lang.NEWBIE_TIME_CHECK_OTHER.msg(specifiedPlayer.getName(), TimeUtil.getDiffDuration(Lang.NEWBIE_TIME_CHECK_OTHER, timeLeft)));
	}

	private void addNewbie(final CommandSender sender, final String targetPlayerName) {
		if (!CombatUtils.isOnlineWithFeedback(sender, targetPlayerName))
			return;

		final CombatPlayer specifiedPlayer = ph.get(Bukkit.getPlayer(targetPlayerName));
		specifiedPlayer.setNewbie(true);
		sender.sendMessage(
				ChatUtils.colorize(String.format(Lang.PREFIXMSG + " Added newbie protection to &e%s", specifiedPlayer.getName())));
	}

	private void removeNewbie(final CommandSender sender, final String targetPlayerName) {
		if (!CombatUtils.isOnlineWithFeedback(sender, targetPlayerName))
			return;

		final CombatPlayer specifiedPlayer = ph.get(Bukkit.getPlayer(targetPlayerName));
		specifiedPlayer.setNewbie(false);
		sender.sendMessage(
				ChatUtils.colorize(String.format(Lang.PREFIXMSG + " Removed newbie protection from &e%s", specifiedPlayer.getName())));
	}

	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 1) {
			if (!Permissions.ADMIN.hasPerm(sender))
				return ChatUtils.getMatchingEntries(args[0], Lists.newArrayList("disable"));
			return ChatUtils.getMatchingEntries(args[0], Lists.newArrayList("add", "checktime", "disable", "remove"));
		}
		if (args.length == 2 && Permissions.ADMIN.hasPerm(sender))
			return ChatUtils.getMatchingEntries(args[1], Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));

		return Collections.emptyList();
	}
}
