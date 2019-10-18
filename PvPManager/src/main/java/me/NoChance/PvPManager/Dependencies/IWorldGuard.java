package me.NoChance.PvPManager.Dependencies;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public interface IWorldGuard extends PvPlugin {

	public boolean hasAllowPvPFlag(Player player);

	public Set<ProtectedRegion> getRegionsAt(final Location l);

	public boolean containsRegionsAt(final Location l, final Set<String> regionIDs);

}
