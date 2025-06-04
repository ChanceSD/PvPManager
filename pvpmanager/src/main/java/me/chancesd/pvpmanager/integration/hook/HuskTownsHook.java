package me.chancesd.pvpmanager.integration.hook;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.ForceToggleDependency;
import me.chancesd.pvpmanager.integration.type.RegionDependency;
import me.chancesd.pvpmanager.player.ProtectionType;
import net.william278.husktowns.api.BukkitHuskTownsAPI;
import net.william278.husktowns.libraries.cloplib.operation.Operation;
import net.william278.husktowns.libraries.cloplib.operation.OperationType;
import net.william278.husktowns.libraries.cloplib.operation.OperationTypeRegistry;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;

public class HuskTownsHook extends BaseDependency implements ForceToggleDependency, RegionDependency {

	private final BukkitHuskTownsAPI huskTownsAPI;

	public HuskTownsHook(final Hook hook) {
		super(hook);
		huskTownsAPI = BukkitHuskTownsAPI.getInstance();
	}

	@Override
	public boolean shouldDisable(final Player player) {
		final OnlineUser user = huskTownsAPI.getOnlineUser(player);
		final Optional<Member> member = huskTownsAPI.getUserTown(user);

		if (!member.isPresent()) {
			return false;
		}

		final Town town = member.get().town();
		return town.getCurrentWar().isPresent();
	}

	@Override
	public boolean shouldDisable(final Player attacker, final Player defender, final ProtectionType reason) {
		return shouldDisable(attacker) && shouldDisable(defender);
	}

	@Override
	public boolean canAttackAt(final Player player, final Location location) {
		  final OperationTypeRegistry reg = huskTownsAPI.getOperationTypeRegistry();
		  final boolean cancelled = reg.getHandler().cancelOperation(Operation.of(
				OperationType.PLAYER_DAMAGE_PLAYER,
				huskTownsAPI.getPosition(location)
		  ));

		  return !cancelled;
	}

}
