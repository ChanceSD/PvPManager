package me.NoChance.PvPManager.Dependencies;

import org.bukkit.entity.Player;

public interface PvPlugin extends Dependency {

	boolean canAttack(Player attacker, Player defender);

}
