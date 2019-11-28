package me.NoChance.PvPManager.Dependencies;

import org.bukkit.Location;

public interface RegionDependency extends Dependency {

	public boolean canAttackAt(Location l);

	@Override
	default String onEnableMessage() {
		return getName() + " Found! Enabling Support For " + getName() + " Regions";
	}

}
