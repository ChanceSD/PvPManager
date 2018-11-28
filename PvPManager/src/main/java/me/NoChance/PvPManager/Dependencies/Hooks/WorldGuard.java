package me.NoChance.PvPManager.Dependencies.Hooks;

import me.NoChance.PvPManager.Dependencies.PvPlugin;
import me.NoChance.PvPManager.PvPManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;

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
	public boolean canBeAttacked(final Player player, final Location location) {
		return getPvPState(player, location);
	}

	public boolean hasAllowPvPFlag(final Player defender) {
		return getPvPState(defender, defender.getLocation());
	}

	@SuppressWarnings("unchecked")
	private boolean getPvPState(final Player player, final Location location) {
		IWrappedFlag<WrappedState> flag = (IWrappedFlag<WrappedState>) wrapper.getFlag("pvp").orElse(null);
		return wrapper.queryFlag(player, location, flag).map(state -> state == WrappedState.ALLOW).orElse(true);
	}

	@Override
	public JavaPlugin getMainClass() {
		return wrapper.getWorldGuardPlugin();
	}
}
