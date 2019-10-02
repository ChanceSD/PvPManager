package me.NoChance.PvPManager.Dependencies;

import org.bukkit.entity.Player;

public interface IWorldGuard extends PvPlugin {

	public boolean hasAllowPvPFlag(Player player);

}
