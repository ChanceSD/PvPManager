package me.chancesd.pvpmanager.integration.type;

import org.bukkit.entity.Player;

/**
 * Represents a plugin that groups players in clans/factions/teams/etc. <br>
 * Allows checking if two players can attack each other or not.
 */
public interface GroupDependency extends Dependency {

	public boolean canAttack(Player attacker, Player defender);

}
