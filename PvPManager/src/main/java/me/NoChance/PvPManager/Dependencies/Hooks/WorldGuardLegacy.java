package me.NoChance.PvPManager.Dependencies.Hooks;

import java.lang.reflect.Method;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;

import me.NoChance.PvPManager.Dependencies.IWorldGuard;

public class WorldGuardLegacy implements IWorldGuard {

	private final WorldGuardPlugin inst;
	private RegionQuery regionQuery;

	public WorldGuardLegacy() {
		inst = WorldGuardPlugin.inst();
		try {
			final Method method = inst.getClass().getMethod("getRegionContainer");
			regionQuery = ((RegionContainer) method.invoke(inst)).createQuery();
		} catch (final Exception e) {
			e.printStackTrace();
			regionQuery = null;
		}
	}

	// This method has no use in free version, use canBeAttacked() instead
	// Exclusions for vulnerable anti border hopping
	@Override
	public boolean canAttack(final Player attacker, final Player defender) {
		return true;
	}

	@Override
	public boolean canBeAttacked(final Player player, final Location l) {
		return getPvPState(player, l) != State.DENY;
	}

	@Override
	public boolean hasAllowPvPFlag(final Player defender) {
		return getPvPState(defender, defender.getLocation()) == State.ALLOW;
	}

	private State getPvPState(final Player p, final Location l) {
		return regionQuery.queryState(l, p, DefaultFlag.PVP);
	}

	@Override
	public JavaPlugin getMainClass() {
		return inst;
	}
}
