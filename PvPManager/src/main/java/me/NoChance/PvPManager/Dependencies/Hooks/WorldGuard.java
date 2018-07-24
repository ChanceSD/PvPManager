package me.NoChance.PvPManager.Dependencies.Hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.NoChance.PvPManager.Dependencies.PvPlugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldGuard implements PvPlugin {

	private final com.sk89q.worldguard.WorldGuard instance;
	private final RegionQuery regionQuery;

	public WorldGuard() {
		instance = com.sk89q.worldguard.WorldGuard.getInstance();
		regionQuery = instance.getPlatform().getRegionContainer().createQuery();
	}

	// This method has no use, use canBeAttacked() instead
	@Override
	public boolean canAttack(final Player attacker, final Player defender) {
		return true;
	}

	@Override
	public boolean canBeAttacked(final Player player, final org.bukkit.Location l) {
		return getPvPState(player, l) != StateFlag.State.DENY;
	}

	public boolean hasAllowPvPFlag(final Player defender) {
		return getPvPState(defender, defender.getLocation()) == StateFlag.State.ALLOW;
	}

	private StateFlag.State getPvPState(final Player p, final org.bukkit.Location l) {
		return regionQuery.queryState(BukkitAdapter.adapt(l), WorldGuardPlugin.inst().wrapPlayer(p), Flags.PVP);
	}

	@Override
	public JavaPlugin getMainClass() {
		return WorldGuardPlugin.inst();
	}

}
