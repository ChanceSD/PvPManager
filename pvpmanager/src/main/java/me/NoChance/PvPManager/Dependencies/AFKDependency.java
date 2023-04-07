package me.NoChance.PvPManager.Dependencies;

import org.bukkit.entity.Player;

import me.NoChance.PvPManager.Dependencies.Interfaces.Dependency;

public interface AFKDependency extends Dependency {

	public boolean isAFK(Player player);

	public default boolean shouldProtectAFK() {
		return getConfigBoolean("Protect AFK", true);
	}

}
