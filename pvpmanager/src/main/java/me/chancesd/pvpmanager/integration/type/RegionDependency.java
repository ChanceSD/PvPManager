package me.chancesd.pvpmanager.integration.type;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface RegionDependency extends Dependency {

	public boolean canAttackAt(Player player, Location location);

	@Override
	default String onEnableMessage() {
		return getName() + " Found! Enabling Support For " + getName() + " Regions";
	}

}
