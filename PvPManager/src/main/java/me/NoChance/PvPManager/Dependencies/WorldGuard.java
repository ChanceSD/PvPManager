package me.NoChance.PvPManager.Dependencies;

import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;

import me.NoChance.PvPManager.PvPlugin;

public class WorldGuard implements PvPlugin {

	private final WorldGuardPlugin inst;

	public WorldGuard() {
		inst = WorldGuardPlugin.inst();
	}

	@Override
	public boolean canAttack(final Player attacker, final Player defender) {
		final State state = inst.getRegionContainer().createQuery().queryState(defender.getLocation(), defender, DefaultFlag.PVP);
		return state == null || !state.equals(State.DENY);
	}

	@Override
	public Object getMainClass() {
		return inst;
	}

}
