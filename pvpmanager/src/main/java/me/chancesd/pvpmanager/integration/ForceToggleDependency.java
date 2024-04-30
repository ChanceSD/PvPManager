package me.chancesd.pvpmanager.integration;

import org.bukkit.entity.Player;

import me.chancesd.pvpmanager.integration.type.Dependency;
import me.chancesd.pvpmanager.player.ProtectionType;

public interface ForceToggleDependency extends Dependency {

	public boolean shouldDisable(Player player);

	public boolean shouldDisable(Player attacker, Player defender, ProtectionType reason);

	public default boolean shouldDisableProtection() {
		return getConfigBoolean("No Protection In War", true);
	}

}
