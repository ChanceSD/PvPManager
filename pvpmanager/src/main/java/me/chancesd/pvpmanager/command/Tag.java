package me.chancesd.pvpmanager.command;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.utils.ChatUtils;
import me.chancesd.pvpmanager.utils.CombatUtils;

public class Tag implements TabExecutor {

	private final PlayerManager ph;

	public Tag(final PlayerManager ph) {
		this.ph = ph;
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
	    // Handle player checking their own tag
	    if (args.length == 0) {
	        if (!(sender instanceof final Player player)) {
	            return false;
	        }
	        showPlayerTagStatus(ph.get(player));
	        return true;
	    }

	    // Admin commands require permission
	    if (!Permissions.ADMIN.hasPerm(sender)) {
	        return false;
	    }

	    // Parse time if provided, otherwise use default
	    long timeInMs = Conf.TIME_IN_COMBAT.asInt() * 1000L;
	    if (args.length == 2) {
	        try {
	            timeInMs = Integer.parseInt(args[1]) * 1000L;
	        } catch (final NumberFormatException e) {
	            sender.sendMessage(Lang.PREFIXMSG + " §cError, time must be a number!");
	            return true;
	        }
	    }

	    // Handle tagging target(s)
	    final String target = args[0];
	    if (target.equals("*")) {
	        tagAll(sender, timeInMs);
	    } else if (CombatUtils.isOnlineWithFeedback(sender, target)) {
	        tagPlayer(sender, target, timeInMs);
	    }
	    return true;
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
		sender.sendMessage(Lang.PREFIXMSG + " §aAll players have been tagged");
	}

	private void tagPlayer(final CommandSender sender, final String name, final long time) {
		final CombatPlayer target = ph.get(Bukkit.getPlayer(name));
		if (target.isInCombat()) {
			sender.sendMessage(Lang.PREFIXMSG + " §cThat player is already in combat");
			return;
		}
		target.tag(true, target, time);
		sender.sendMessage(Lang.PREFIXMSG + " " + target.getName() + " §2has been tagged for §e" + time / 1000 + " §2seconds");
	}

	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 1 && Permissions.ADMIN.hasPerm(sender)) {
			final List<String> list = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
			list.add("*");
			return ChatUtils.getMatchingEntries(args[0], list);
		}

		return Collections.emptyList();
	}

}
