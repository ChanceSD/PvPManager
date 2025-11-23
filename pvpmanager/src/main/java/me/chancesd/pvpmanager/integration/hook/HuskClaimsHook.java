package me.chancesd.pvpmanager.integration.hook;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.RegionDependency;
import net.william278.huskclaims.api.BukkitHuskClaimsAPI;
import net.william278.huskclaims.libraries.cloplib.operation.Operation;
import net.william278.huskclaims.libraries.cloplib.operation.OperationType;
import net.william278.huskclaims.libraries.cloplib.operation.OperationTypeRegistry;

public class HuskClaimsHook extends BaseDependency implements RegionDependency {

	private final BukkitHuskClaimsAPI huskClaimsAPI;

	public HuskClaimsHook(final Hook hook) {
		super(hook);
		huskClaimsAPI = BukkitHuskClaimsAPI.getInstance();
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
