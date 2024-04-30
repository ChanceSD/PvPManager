package me.chancesd.pvpmanager.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Messages;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.utils.ChatUtils;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.pvpmanager.world.CombatWorld;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

public class PvP implements TabExecutor {

	private final PlayerManager ph;

	public PvP(final PlayerManager playerHandler) {
		this.ph = playerHandler;
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (!sender.hasPermission("pvpmanager.pvpstatus.change")) {
			sender.sendMessage(Messages.getErrorPermission());
			return true;
		}

		if (sender instanceof Player && args.length == 0) {
			final Player player = (Player) sender;
			final CombatPlayer pvpPlayer = ph.get(player);
			togglePvP(pvpPlayer, !pvpPlayer.hasPvPEnabled());
			return true;
		}

		if (args.length == 1) {
			if (sender instanceof Player && (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("on"))) {
				final CombatPlayer pvpPlayer = ph.get((Player) sender);
				final boolean state = args[0].equalsIgnoreCase("on");
				togglePvP(pvpPlayer, state);
				return true;
			} else if (Permissions.ADMIN.hasPerm(sender)) {
				final String target = args[0];
				if (target.equalsIgnoreCase("*"))
					togglePvPAll(sender, false, true);
				else
					togglePvPAdmin(sender, target, false, true);
				return true;
			}
			return false;
		}

		if (args.length == 2 && Permissions.ADMIN.hasPerm(sender) && (args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("on"))) {
			final String target = args[0];
			if (target.equalsIgnoreCase("*"))
				togglePvPAll(sender, args[1].equalsIgnoreCase("on"), false);
			else
				togglePvPAdmin(sender, target, args[1].equalsIgnoreCase("on"), false);
			return true;
		}

		sender.sendMessage(Messages.getErrorCommand());
		return true;
	}

	private void togglePvP(final CombatPlayer player, final boolean state) {
		if (!player.hasToggleCooldownPassed())
			return;

		if (player.hasPvPEnabled() == state) {
			player.message(state ? Messages.getAlreadyEnabled() : Messages.getAlreadyDisabled());
			return;
		}

		final CombatWorld combatWorld = player.getCombatWorld();
		if (state && combatWorld.isPvPForced() == CombatWorld.WorldOptionState.OFF) {
			player.message(Messages.getErrorPvPToggleNoPvP());
			return;
		} else if (!state && combatWorld.isPvPForced() == CombatWorld.WorldOptionState.ON) {
			player.message(Messages.getErrorPvPToggleForcePvP());
			return;
		}

		player.setPvP(state);
	}

	private void togglePvPAdmin(final CommandSender sender, final String playerName, final boolean state, final boolean toggle) {
		if (!CombatUtils.isOnlineWithFeedback(sender, playerName))
			return;
		final CombatPlayer specifiedPlayer = ph.get(Bukkit.getPlayer(playerName));
		specifiedPlayer.setPvP(toggle ? !specifiedPlayer.hasPvPEnabled() : state);
		final String stateMessage = specifiedPlayer.hasPvPEnabled() ? Messages.getEnabled() : Messages.getDisabled();
		sender.sendMessage(Messages.getPvPToggleAdminChanged().replace("%p", playerName).replace("%state", stateMessage)); // TODO add replace variables
	}

	private void togglePvPAll(final CommandSender sender, final boolean state, final boolean toggle) {
		for (final Player player : Bukkit.getOnlinePlayers()) {
			togglePvPAdmin(sender, player.getName(), state, toggle);
		}
	}

	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 1) {
			if (!Permissions.ADMIN.hasPerm(sender))
				return ChatUtils.getMatchingEntries(args[0], Lists.newArrayList("ON", "OFF"));
			final List<String> list = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
			list.addAll(Arrays.asList("*", "ON", "OFF"));
			return ChatUtils.getMatchingEntries(args[0], list);
		}
		if (args.length == 2 && Permissions.ADMIN.hasPerm(sender))
			return ChatUtils.getMatchingEntries(args[1], Lists.newArrayList("ON", "OFF"));

		return Collections.emptyList();
	}
}
