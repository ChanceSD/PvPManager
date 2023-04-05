package me.NoChance.PvPManager.Dependencies.API;

import org.bukkit.entity.Player;

public interface GodDependency extends Dependency {

	public boolean hasGodMode(Player player);

	public void enableGodMode(Player player);

	public void disableGodMode(Player player);

}
