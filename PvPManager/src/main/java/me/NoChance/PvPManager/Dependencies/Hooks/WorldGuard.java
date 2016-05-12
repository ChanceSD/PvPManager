package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;

import me.NoChance.PvPManager.Dependencies.PvPlugin;

public class WorldGuard implements PvPlugin {

	private final WorldGuardPlugin inst;
	private final RegionQuery regionQuery;

	public WorldGuard() {
		inst = WorldGuardPlugin.inst();
		regionQuery = inst.getRegionContainer().createQuery();
	}

	@Override
	public boolean canAttack(final Player attacker, final Player defender) {
		final State state = regionQuery.queryState(defender.getLocation(), defender, DefaultFlag.PVP);
		return state == null || !state.equals(State.DENY);
	}

	public boolean hasAllowPvPFlag(final Player defender) {
		return regionQuery.queryState(defender.getLocation(), defender, DefaultFlag.PVP) == State.ALLOW;
	}

	@Override
	public JavaPlugin getMainClass() {
		return inst;
	}

}
