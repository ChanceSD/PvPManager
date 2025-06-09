package me.chancesd.pvpmanager.command;

import java.util.List;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.world.CombatWorld;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.sdutils.command.ArgumentType;
import me.chancesd.sdutils.command.BaseCommand;
import me.chancesd.sdutils.command.CommandArgument;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class PvP extends BaseCommand {

	private final PlayerManager ph;

	public PvP(final PluginCommand pluginCommand, final PlayerManager playerHandler) {
		super(pluginCommand);
		this.ph = playerHandler;
		permission(Permissions.COMMAND_PVP_TOGGLE.getPermission())
				.argument("player", ArgumentType.PLAYER_OR_ALL).requirePermission(Permissions.COMMAND_PVP_ADMIN.getPermission()).endArgument()
				.argument("status", ArgumentType.BOOLEAN).requirePermission(Permissions.COMMAND_PVP_ADMIN.getPermission()).tabComplete("ON", "OFF")
				.endArgument()
				.description("Toggle PvP for yourself or others");
	}

	@Override
	public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
		// Pattern: /pvp (no args) - toggle own PvP (players only)
		if (args.isEmpty()) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("§cThis command can only be used by players.");
				return;
			}
			final Player player = (Player) sender;
			final CombatPlayer pvpPlayer = ph.get(player);
			togglePvP(pvpPlayer, !pvpPlayer.hasPvPEnabled());
			return;
		}

		// Admin patterns: /pvp <player|*> [on|off]
		final CommandArgument playerArg = getArgument(args, "player");
		if (args.size() == 1) {
			if (playerArg.isWildcard()) {
				togglePvPAll(sender, false, true);
			} else {
				togglePvPAdmin(sender, playerArg.getValue(), false, true);
			}
			return;
		}
		// Pattern: /pvp <player|*> on|off - set target's PvP status
		final boolean state = getArgument(args, "status").getAsBoolean();
		if (playerArg.isWildcard()) {
			togglePvPAll(sender, state, false);
		} else {
			togglePvPAdmin(sender, playerArg.getValue(), state, false);
		}
	}

	private void togglePvP(final CombatPlayer player, final boolean state) {
		if (!hasToggleCooldownPassed(player))
			return;

		if (player.hasPvPEnabled() == state) {
			player.message(state ? Lang.PVP_TOGGLE_ALREADY_ENABLED : Lang.PVP_TOGGLE_ALREADY_DISABLED);
			return;
		}

		final CombatWorld combatWorld = player.getCombatWorld();
		if (state && combatWorld.isPvPForced() == CombatWorld.WorldOptionState.OFF) {
			player.message(Lang.ERROR_PVP_TOGGLE_NO_PVP);
			return;
		} else if (!state && combatWorld.isPvPForced() == CombatWorld.WorldOptionState.ON) {
			player.message(Lang.ERROR_PVP_TOGGLE_FORCE_PVP);
			return;
		}

		player.setPvP(state);
	}

	private void togglePvPAdmin(final CommandSender sender, final String playerName, final boolean state, final boolean toggle) {
		if (!CombatUtils.isOnlineWithFeedback(sender, playerName))
			return;
		final CombatPlayer specifiedPlayer = ph.get(Bukkit.getPlayer(playerName));
		specifiedPlayer.setPvP(toggle ? !specifiedPlayer.hasPvPEnabled() : state);
		final String stateMessage = specifiedPlayer.hasPvPEnabled() ? Lang.ENABLED.msg() : Lang.DISABLED.msg();
		sender.sendMessage(Lang.PVP_TOGGLE_ADMIN_CHANGED.msg(playerName, stateMessage));
	}

	private void togglePvPAll(final CommandSender sender, final boolean state, final boolean toggle) {
		for (final Player player : Bukkit.getOnlinePlayers()) {
			togglePvPAdmin(sender, player.getName(), state, toggle);
		}
	}

	public final boolean hasToggleCooldownPassed(final CombatPlayer player) {
		if (!CombatUtils.hasTimePassed(player.getToggleTime(), Conf.TOGGLE_COOLDOWN.asInt())
				&& !player.hasPerm(Permissions.EXEMPT_PVPTOGGLE_COOLDOWN)) {
			final long timeLeft = CombatUtils.getTimeLeftMs(player.getToggleTime(), Conf.TOGGLE_COOLDOWN.asInt() * 1000L);
			player.message(Lang.ERROR_PVP_COOLDOWN.msgTime(timeLeft));
			return false;
		}

		return true;
	}
}
