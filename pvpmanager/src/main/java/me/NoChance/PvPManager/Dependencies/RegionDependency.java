package me.NoChance.PvPManager.Dependencies;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface RegionDependency extends Dependency {

	public boolean canAttackAt(Player p, Location l);

	@Override
	default String onEnableMessage() {
		return getName() + " Found! Enabling Support For " + getName() + " Regions";
	}

}
