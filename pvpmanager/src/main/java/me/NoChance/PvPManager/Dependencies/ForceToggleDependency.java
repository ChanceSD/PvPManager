package me.NoChance.PvPManager.Dependencies;

import org.bukkit.entity.Player;

import me.NoChance.PvPManager.Dependencies.Interfaces.Dependency;
import me.NoChance.PvPManager.Player.ProtectionType;

public interface ForceToggleDependency extends Dependency {

	public boolean shouldDisable(Player player);

	public boolean shouldDisable(Player attacker, Player defender, ProtectionType reason);

	public default boolean shouldDisableProtection() {
		return getConfigBoolean("No Protection In War", true);
	}

}
