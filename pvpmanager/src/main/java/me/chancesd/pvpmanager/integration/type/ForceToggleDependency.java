package me.chancesd.pvpmanager.integration.type;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.chancesd.pvpmanager.player.ProtectionType;

public interface ForceToggleDependency extends Dependency {

	public boolean shouldDisable(@NotNull Player player);

	public boolean shouldDisable(@NotNull Player attacker, @NotNull Player defender, ProtectionType reason);

	public default boolean shouldDisableProtection() {
		return getConfigBoolean("No Protection In War", true);
	}

}
