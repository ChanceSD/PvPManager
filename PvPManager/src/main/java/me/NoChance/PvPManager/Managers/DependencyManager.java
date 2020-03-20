package me.NoChance.PvPManager.Managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.Dependency;
import me.NoChance.PvPManager.Dependencies.DependencyException;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.PvPDependency;
import me.NoChance.PvPManager.Dependencies.RegionDependency;
import me.NoChance.PvPManager.Dependencies.WorldGuardHook;
import me.NoChance.PvPManager.Dependencies.Hooks.FactionsHook;
import me.NoChance.PvPManager.Dependencies.Hooks.FactionsUUIDHook;
import me.NoChance.PvPManager.Dependencies.Hooks.SavageFactionsHook;
import me.NoChance.PvPManager.Dependencies.Hooks.SimpleClansHook;
import me.NoChance.PvPManager.Dependencies.Hooks.VaultHook;
import me.NoChance.PvPManager.Dependencies.Hooks.WorldGuardLegacyHook;
import me.NoChance.PvPManager.Dependencies.Hooks.WorldGuardModernHook;
import me.NoChance.PvPManager.Listeners.PlayerMoveListener;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.NoChance.PvPManager.Utils.Log;
import net.milkbowl.vault.economy.Economy;

public class DependencyManager {

	private final HashMap<Hook, Dependency> dependencies = new HashMap<>();
	private final ArrayList<PvPDependency> attackChecks = new ArrayList<>();
	private final ArrayList<RegionDependency> regionChecks = new ArrayList<>();

	public DependencyManager() {
		setupHooks();
	}

	private void setupHooks() {
		for (final Hook hook : Hook.values()) {
			try {
				if (!hook.isEnabled()) {
					if (hook.getDisabledWarning() != null) {
						Log.warning(hook.getDisabledWarning());
					}
					hook.getDisabledAction().run();
					continue;
				}
				attemptHookingInto(hook);
			} catch (final NoClassDefFoundError | NoSuchMethodError | ClassCastException e) {
				Log.warning("Your " + hook + " version is currently unsupported: " + hook.getDescription().getFullName());
				Log.warning(hook + " support disabled");
			} catch (final DependencyException e) {
				Log.warning(e.getMessage());
				if (isDependencyEnabled(hook)) {
					hook.getDisabledAction().run();
					unregisterDependency(getDependency(hook));
				}
			}
		}
	}

	private void attemptHookingInto(final Hook hook) {
		switch (hook) {
		case FACTIONS:
			final String fVersion = hook.getVersion();
			if (fVersion.contains("RC")) {
				registerDependency(new SavageFactionsHook(hook));
			} else if (fVersion.contains("U")) {
				registerDependency(new FactionsUUIDHook(hook));
			} else if (CombatUtils.isVersionAtLeast(fVersion, "2.7")) {
				registerDependency(new FactionsHook(hook));
			} else {
				Log.info("Update Factions to the latest version if you want PvPManager to hook into it successfully");
			}
			break;
		case SIMPLECLANS:
			registerDependency(new SimpleClansHook(hook));
			break;
		case VAULT:
			registerDependency(new VaultHook(hook));
			break;
		case WORLDGUARD:
			if (CombatUtils.isVersionAtLeast(CombatUtils.stripTags(hook.getVersion()), "7.0")) {
				registerDependency(new WorldGuardModernHook(hook));
			} else {
				registerDependency(new WorldGuardLegacyHook(hook));
			}
			break;
		default:
			registerDependency(new BaseDependency(hook));
			break;
		}
	}

	public final boolean canAttack(final Player attacker, final Player defender) {
		for (final PvPDependency pvPlugin : attackChecks)
			if (!pvPlugin.canAttack(attacker, defender))
				return false;
		return true;
	}

	public final boolean canAttackAt(final Player p, final Location l) {
		for (final RegionDependency regionPlugin : regionChecks) {
			if (!regionPlugin.canAttackAt(p, l))
				return false;
		}
		return true;
	}

	public void startListeners(final PlayerHandler ph) {
		if (Settings.borderHoppingPushback() && !regionChecks.isEmpty()) {
			if (CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.8")) {
				Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(ph), ph.getPlugin());
			} else {
				Log.warning("Pushback on border hopping not available for 1.7.10 or below! Feature disabled!");
				Settings.setBorderHoppingPushback(false);
			}
		}
		if (isDependencyEnabled(Hook.WORLDGUARD)) {
			((WorldGuardHook) getDependency(Hook.WORLDGUARD)).startListener(ph);
		}
	}

	public boolean isDependencyEnabled(final Hook d) {
		return dependencies.containsKey(d);
	}

	public Dependency getDependency(final Hook h) {
		return dependencies.get(h);
	}

	public void registerDependency(final Dependency dep) {
		dependencies.put(dep.getHook(), dep);
		if (dep instanceof PvPDependency) {
			attackChecks.add((PvPDependency) dep);
		}
		if (dep instanceof RegionDependency) {
			regionChecks.add((RegionDependency) dep);
		}
	}

	public void unregisterDependency(final Dependency dep) {
		dependencies.remove(dep.getHook());
		attackChecks.remove(dep);
		attackChecks.remove(dep);
	}

	public JavaPlugin getDependencyMainClass(final Hook h) {
		if (isDependencyEnabled(h))
			return dependencies.get(h).getPlugin();
		return null;
	}

	public final Economy getEconomy() {
		if (isDependencyEnabled(Hook.VAULT))
			return ((VaultHook) dependencies.get(Hook.VAULT)).getEconomy();
		return null;
	}

	@SuppressWarnings("unchecked")
	public Map<Hook, Dependency> getDependencies() {
		return (Map<Hook, Dependency>) dependencies.clone();
	}

}
