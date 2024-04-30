package me.chancesd.pvpmanager.integration;

import org.bukkit.entity.Player;

import me.chancesd.pvpmanager.integration.type.Dependency;

/**
 * Represents a plugin that groups players in clans/factions/teams/etc. <br>
 * Allows checking if two players can attack each other or not.
 */
public interface GroupDependency extends Dependency {

	public boolean canAttack(Player attacker, Player defender);

}
