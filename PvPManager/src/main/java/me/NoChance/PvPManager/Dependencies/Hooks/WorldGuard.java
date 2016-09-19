package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.Location;
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

	// This method has no use, use canBeAttacked() instead
	@Override
	public boolean canAttack(final Player attacker, final Player defender) {
		return true;
	}

	@Override
	public boolean canBeAttacked(final Player player, final Location l) {
		return getPvPState(player, l) != State.DENY;
	}

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
