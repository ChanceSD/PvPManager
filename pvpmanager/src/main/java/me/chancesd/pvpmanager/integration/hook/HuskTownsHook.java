package me.NoChance.PvPManager.Dependencies.Hooks;

import net.william278.husktowns.api.BukkitHuskTownsAPI;
import net.william278.husktowns.libraries.cloplib.operation.Operation;
import net.william278.husktowns.libraries.cloplib.operation.OperationTypeRegistry;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.libraries.cloplib.operation.OperationType;

import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.RegionDependency;
import me.NoChance.PvPManager.Player.CancelResult;
import me.NoChance.PvPManager.Dependencies.ForceToggleDependency;

import java.util.Optional;

public class HuskTownsHook extends BaseDependency implements ForceToggleDependency, RegionDependency {

	private final BukkitHuskTownsAPI huskTownsAPI;

	public HuskTownsHook(final Hook hook) {
		super(hook);
		huskTownsAPI = BukkitHuskTownsAPI.getInstance();
	}

	@Override
	public boolean shouldDisable(final Player player) {
		OnlineUser user = huskTownsAPI.getOnlineUser(player);
		Optional<Member> member = huskTownsAPI.getUserTown(user);

		if (!member.isPresent()) {
			return false;
		}

		Town town = member.get().town();
		return town.getCurrentWar().isPresent();
	}

	@Override
	public boolean shouldDisable(final Player attacker, final Player defender, final CancelResult reason) {
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
