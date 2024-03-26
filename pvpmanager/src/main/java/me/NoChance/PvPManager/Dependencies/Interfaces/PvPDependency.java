package me.NoChance.PvPManager.Dependencies.Interfaces;

import org.bukkit.entity.Player;

public interface PvPDependency extends Dependency {

	public boolean canAttack(Player attacker, Player defender);

}
