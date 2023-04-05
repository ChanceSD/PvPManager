package me.NoChance.PvPManager.Dependencies.Hooks;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.WorldGuardHook;
import me.NoChance.PvPManager.Listeners.WGListener;
import me.NoChance.PvPManager.Managers.PlayerHandler;

public class WorldGuardModernHook extends BaseDependency implements WorldGuardHook {

	private final RegionQuery regionQuery;

	public WorldGuardModernHook(final Hook hook) {
		super(hook);
		regionQuery = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
	}

	// This method has no use in free version, use canAttackAt() instead
	// Exclusions for vulnerable anti border hopping
	@Override
	public boolean canAttack(final Player attacker, final Player defender) {
		return true;
	}

	@Override
	public boolean canAttackAt(final Player p, final Location l) {
		// State has to be != DENY because you can pvp on ALLOW and on no state
		return getWGPvPState(l) != State.DENY;
	}

	@Override
	public boolean hasAllowPvPFlag(final Player defender) {
		return getWGPvPState(defender.getLocation()) == State.ALLOW;
	}

	@Override
	public boolean hasDenyPvPFlag(final Player player) {
		return getWGPvPState(player.getLocation()) == State.DENY;
	}

	@Override
	public Set<ProtectedRegion> getRegionsAt(final Location l) {
		return regionQuery.getApplicableRegions(BukkitAdapter.adapt(l)).getRegions();
	}

	@Override
	public boolean containsRegionsAt(final Location l, final Set<String> regionIDs) {
		for (final ProtectedRegion r : getRegionsAt(l)) {
			if (regionIDs.contains(r.getId()))
				return true;
		}
		return false;
	}

	@Override
	public State getWGPvPState(final Location l) {
		return regionQuery.queryState(BukkitAdapter.adapt(l), null, Flags.PVP);
	}

	@Override
	public void startListener(final PlayerHandler ph) {
		Bukkit.getPluginManager().registerEvents(new WGListener(ph), ph.getPlugin());
	}

}
