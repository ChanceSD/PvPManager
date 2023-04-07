package me.NoChance.PvPManager.Dependencies.Interfaces;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.NoChance.PvPManager.Managers.PlayerHandler;

public interface WorldGuardDependency extends GroupDependency, RegionDependency {

	public boolean hasAllowPvPFlag(Player player);

	public boolean hasDenyPvPFlag(Player player);

	public State getWGPvPState(final Location l);

	public Set<ProtectedRegion> getRegionsAt(final Location l);

	public boolean containsRegionsAt(final Location l, final Set<String> regionIDs);

	public void startListener(PlayerHandler ph);

}
