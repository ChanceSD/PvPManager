package me.NoChance.PvPManager.Dependencies;

import org.bukkit.entity.Player;

import me.NoChance.PvPManager.Dependencies.API.Dependency;
import me.NoChance.PvPManager.Player.ProtectionResult;

public interface ForceToggleDependency extends Dependency {

	public boolean shouldDisable(Player player);

	public boolean shouldDisable(Player attacker, Player defender, ProtectionResult reason);

	public default boolean shouldDisableProtection() {
		return getConfigBoolean("No Protection In War", true);
	}

}
