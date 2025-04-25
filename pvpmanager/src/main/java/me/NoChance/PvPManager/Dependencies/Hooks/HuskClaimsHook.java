package me.NoChance.PvPManager.Dependencies.Hooks;

import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.ForceToggleDependency;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.RegionDependency;
import me.NoChance.PvPManager.Player.CancelResult;
import net.william278.huskclaims.api.BukkitHuskClaimsAPI;
import net.william278.huskclaims.libraries.cloplib.operation.Operation;
import net.william278.huskclaims.libraries.cloplib.operation.OperationType;
import net.william278.huskclaims.libraries.cloplib.operation.OperationTypeRegistry;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class HuskClaimsHook extends BaseDependency implements ForceToggleDependency, RegionDependency {

	private final BukkitHuskClaimsAPI huskClaimsAPI;

	public HuskClaimsHook(final Hook hook) {
		super(hook);
		huskClaimsAPI = BukkitHuskClaimsAPI.getInstance();
	}

	@Override
	public boolean shouldDisable(final Player player) {
		return false;
	}

	@Override
	public boolean shouldDisable(final Player attacker, final Player defender, final CancelResult reason) {
		return shouldDisable(attacker) && shouldDisable(defender);
	}

	@Override
	public boolean canAttackAt(final Player player, final Location location) {
		  final OperationTypeRegistry reg = huskClaimsAPI.getOperationTypeRegistry();
		  final boolean cancelled = reg.getHandler().cancelOperation(Operation.of(
				OperationType.PLAYER_DAMAGE_PLAYER,
				huskClaimsAPI.getPosition(location)
		  ));

		  return !cancelled;
	}

}
