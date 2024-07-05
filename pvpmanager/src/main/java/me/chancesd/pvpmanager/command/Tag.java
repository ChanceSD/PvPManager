package me.chancesd.pvpmanager.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.utils.CombatUtils;

public class Tag implements CommandExecutor {

	private final PlayerManager ph;

	public Tag(final PlayerManager ph) {
		this.ph = ph;
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 0 && sender instanceof final Player player) {
			final CombatPlayer pvPlayer = ph.get(player);
			if (!pvPlayer.isInCombat()) {
				pvPlayer.message(Lang.TAG_NOT_IN_COMBAT.msg());
			} else {
				final long timeLeft = pvPlayer.getTaggedTime() + Conf.TIME_IN_COMBAT.asInt() * 1000 - System.currentTimeMillis();
				pvPlayer.message(Lang.TAG_TIME_LEFT.msgTime(timeLeft));
			}
			return true;
		} else if (args.length == 1 && Permissions.ADMIN.hasPerm(sender)) {
			final String name = args[0];
			if (!CombatUtils.isOnlineWithFeedback(sender, name))
				return true;

			tagPlayer(sender, name, Conf.TIME_IN_COMBAT.asInt() * 1000L);
			return true;
		} else if (args.length == 2 && Permissions.ADMIN.hasPerm(sender)) {
			final String name = args[0];
			if (!CombatUtils.isOnlineWithFeedback(sender, name))
				return true;
			try {
				final int time = Integer.parseInt(args[1]);
				tagPlayer(sender, name, time * 1000L);
			} catch (final NumberFormatException e) {
				sender.sendMessage(Lang.PREFIXMSG + " §cError, time must be a number!");
			}
			return true;
		}
		return false;
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

}
