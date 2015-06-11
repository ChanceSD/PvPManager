package me.NoChance.PvPManager.Dependencies;

import me.NoChance.PvPManager.PvPlugin;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;

public class WorldGuard implements PvPlugin {

	private final RegionQuery rq;

	public WorldGuard(final Plugin plugin) {
		this.rq = ((WorldGuardPlugin) plugin).getRegionContainer().createQuery();
	}

	@Override
	public final boolean canAttack(final Player attacker, final Player defender) {
		return rq.testState(attacker.getLocation(), attacker, DefaultFlag.PVP) && rq.testState(defender.getLocation(), defender, DefaultFlag.PVP);
	}

}
