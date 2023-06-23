package me.NoChance.PvPManager.Dependencies;

import org.bukkit.entity.Player;

import me.NoChance.PvPManager.Player.CancelResult;

public interface ForceToggleDependency extends Dependency {

	public boolean shouldDisable(Player player);

	public boolean shouldDisable(Player attacker, Player defender, CancelResult reason);

	public boolean shouldDisableProtection();

}
