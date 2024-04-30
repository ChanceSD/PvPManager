package me.chancesd.pvpmanager.integration.hook;

import java.lang.reflect.Method;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.DependencyException;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.chancesd.pvpmanager.listener.WGListenerLegacy;

public class WorldGuardLegacyHook extends BaseDependency implements WorldGuardDependency {

	private RegionQuery regionQuery;

	public WorldGuardLegacyHook(final Hook hook) {
		super(hook);
		final WorldGuardPlugin wgPlugin = WorldGuardPlugin.inst();
		try {
			final Method method = WorldGuardPlugin.class.getMethod("getRegionContainer");
			regionQuery = ((RegionContainer) method.invoke(wgPlugin)).createQuery();
		} catch (final ReflectiveOperationException e) {
			regionQuery = null;
			throw new DependencyException("Error loading WorldGuard! Update WorldGuard to a newer version", e, hook);
		}
	}

	// This method has no use in free version, use canAttackAt() instead
	// Exclusions for vulnerable anti border hopping
	@Override
	public boolean canAttack(final Player attacker, final Player defender) {
		return true;
	}

	@Override
	public boolean canAttackAt(final Player p, final Location l) {
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
		return regionQuery.getApplicableRegions(l).getRegions();
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
		return regionQuery.queryState(l, (Player) null, DefaultFlag.PVP);
	}

	@Override
	public void startListener(final PlayerManager ph) {
		Bukkit.getPluginManager().registerEvents(new WGListenerLegacy(ph), ph.getPlugin());
	}

	@Override
	public String getName() {
		return "WorldGuardLegacy";
	}

}