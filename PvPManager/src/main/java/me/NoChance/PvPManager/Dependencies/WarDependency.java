package me.NoChance.PvPManager.Dependencies;

import org.bukkit.entity.Player;

public interface WarDependency extends Dependency {

	public boolean isInWar(Player player);

	public boolean isInWar(Player attacker, Player defender);

	public boolean shouldDisablePvPInWar();

}
