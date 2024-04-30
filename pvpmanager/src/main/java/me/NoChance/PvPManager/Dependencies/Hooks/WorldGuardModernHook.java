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

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.ForceToggleDependency;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.Interfaces.WorldGuardDependency;
import me.NoChance.PvPManager.Player.ProtectionType;
import me.chancesd.pvpmanager.listener.WGListener;
import me.chancesd.pvpmanager.manager.PlayerHandler;
import me.chancesd.pvpmanager.setting.Messages;
import me.chancesd.pvpmanager.setting.Settings;

public class WorldGuardModernHook extends BaseDependency implements WorldGuardDependency, ForceToggleDependency {

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

	@Override
	public boolean shouldDisable(final Player player) {
		return false;
	}

	@Override
	public boolean shouldDisable(final Player damager, final Player defender, final ProtectionType reason) {
		if (hasAllowPvPFlag(defender) || containsRegionsAt(defender.getLocation(), Settings.getWorldguardOverridesList())) {
			final PvPlayer attacker = PvPlayer.get(damager);
			final PvPlayer attacked = PvPlayer.get(defender);
			if (reason == ProtectionType.PVPDISABLED) {
				disablePvP(attacker);
				disablePvP(attacked);
			} else {
				disableNewbieProtection(attacker);
				disableNewbieProtection(attacked);
			}
			return true;
		}
		return false;
	}

	private void disableNewbieProtection(final PvPlayer player) {
		if (player.isNewbie()) {
			player.setNewbie(false);
			player.message(Messages.getNewbieForceRemovedWG());
		}
	}

	private void disablePvP(final PvPlayer player) {
		if (!player.hasPvPEnabled()) {
			player.setPvP(true);
			player.message(Messages.getPvpForceEnabledWG());
		}
	}

	@Override
	public boolean shouldDisableProtection() {
		return Settings.isWorldguardOverrides();
	}

}
