package me.NoChance.PvPManager;

import org.bukkit.entity.Player;

public interface PvPlugin {

	boolean canAttack(Player attacker, Player defender);

}
