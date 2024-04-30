package me.chancesd.pvpmanager.integration.type;

import org.bukkit.entity.Player;

public interface GodDependency extends Dependency {

	public boolean hasGodMode(Player player);

	public void enableGodMode(Player player);

	public void disableGodMode(Player player);

}
