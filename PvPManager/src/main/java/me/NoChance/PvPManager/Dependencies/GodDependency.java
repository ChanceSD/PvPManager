package me.NoChance.PvPManager.Dependencies;

import org.bukkit.entity.Player;

public interface GodDependency extends Dependency {

	public boolean hasGodMode(Player player);

	public void removeGodMode(Player player);

}
