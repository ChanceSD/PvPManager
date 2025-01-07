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
import me.chancesd.pvpmanager.player.UntagReason;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.utils.ChatUtils;
import me.chancesd.pvpmanager.utils.CombatUtils;

public class Untag implements TabExecutor {

	private final PlayerManager ph;

	public Untag(final PlayerManager ph) {
		this.ph = ph;
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length != 1) {
			return false;
		}

		final String target = args[0];
		if (target.equals("*")) {
			untagAll(sender);
			return true;
		}

		if (!CombatUtils.isOnlineWithFeedback(sender, target)) {
			return true;
		}

		final CombatPlayer targetPlayer = ph.get(Bukkit.getPlayer(target));
		if (!targetPlayer.isInCombat()) {
			sender.sendMessage(Lang.PREFIXMSG + " §cThat player is not in combat");
			return true;
		}

		targetPlayer.untag(UntagReason.COMMAND);
		sender.sendMessage(Lang.PREFIXMSG + " " + targetPlayer.getName() + " §2has been untagged");
		return true;
	}

	private void untagAll(final CommandSender sender) {
		for (final CombatPlayer player : ph.getPlayers().values()) {
			player.untag(UntagReason.COMMAND);
		}
		sender.sendMessage(Lang.PREFIXMSG + " §aAll players have been untagged");
	}

	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 1) {
			final List<String> list = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
			list.add("*");
			return ChatUtils.getMatchingEntries(args[0], list);
		}

		return Collections.emptyList();
	}

}
