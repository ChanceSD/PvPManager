package me.NoChance.PvPManager.Dependencies;

import org.bukkit.entity.Player;

public interface PvPDependency extends Dependency {

	public boolean canAttack(Player attacker, Player defender);

}
