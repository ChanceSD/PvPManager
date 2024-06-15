package me.chancesd.pvpmanager.integration.hook.worldguard;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.ForceToggleDependency;
import me.chancesd.pvpmanager.integration.type.WorldGuardDependency;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.ProtectionType;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Conf;

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
	public void startListener(final PlayerManager ph) {
		Bukkit.getPluginManager().registerEvents(new WGListener(ph), ph.getPlugin());
	}

	@Override
	public boolean shouldDisable(final Player player) {
		return false;
	}

	@Override
	public boolean shouldDisable(final Player damager, final Player defender, final ProtectionType reason) {
		if (hasAllowPvPFlag(defender) || containsRegionsAt(defender.getLocation(), Conf.WORLDGUARD_OVERRIDES_LIST.asSet())) {
			final CombatPlayer attacker = CombatPlayer.get(damager);
			final CombatPlayer attacked = CombatPlayer.get(defender);
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

	private void disableNewbieProtection(final CombatPlayer player) {
		if (player.isNewbie()) {
			player.setNewbie(false);
			player.message(Lang.NEWBIE_FORCE_REMOVED_WG.msg());
		}
	}

	private void disablePvP(final CombatPlayer player) {
		if (!player.hasPvPEnabled()) {
			player.setPvP(true);
			player.message(Lang.PVP_FORCE_ENABLED_WG.msg());
		}
	}

	@Override
	public boolean shouldDisableProtection() {
		return Conf.WORLDGUARD_OVERRIDES.asBool();
	}

}
