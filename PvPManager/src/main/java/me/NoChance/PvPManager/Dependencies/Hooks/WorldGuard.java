package me.NoChance.PvPManager.Dependencies.Hooks;

import me.NoChance.PvPManager.Dependencies.PvPlugin;
import me.NoChance.PvPManager.PvPManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

public class WorldGuard implements PvPlugin {

	private final WorldGuardWrapper wrapper;

	public WorldGuard() {
		wrapper = WorldGuardWrapper.getInstance();
		wrapper.registerEvents(PvPManager.getInstance());
	}

	// This method has no use, use canBeAttacked() instead
	@Override
	public boolean canAttack(final Player attacker, final Player defender) {
		return true;
	}

	@Override
	public boolean canBeAttacked(final Player player, final org.bukkit.Location l) {
		return getPvPState(player, l);
	}

	public boolean hasAllowPvPFlag(final Player defender) {
		return getPvPState(defender, defender.getLocation());
	}

	private boolean getPvPState(final Player p, final org.bukkit.Location l) {
		return wrapper.queryStateFlag(p, l, "pvp").orElse(true);
	}

	@Override
	public JavaPlugin getMainClass() {
		return wrapper.getWorldGuardPlugin();
	}
}
