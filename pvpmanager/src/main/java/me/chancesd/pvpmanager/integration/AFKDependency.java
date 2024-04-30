package me.chancesd.pvpmanager.integration;

import org.bukkit.entity.Player;

import me.chancesd.pvpmanager.integration.type.Dependency;

public interface AFKDependency extends Dependency {

	public boolean isAFK(Player player);

	public default boolean shouldProtectAFK() {
		return getConfigBoolean("Protect AFK", true);
	}

}
