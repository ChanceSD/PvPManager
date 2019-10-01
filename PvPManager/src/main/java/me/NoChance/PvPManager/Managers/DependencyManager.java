package me.NoChance.PvPManager.Managers;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.NoChance.PvPManager.Dependencies.Dependency;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.PvPlugin;
import me.NoChance.PvPManager.Dependencies.Hooks.Factions;
import me.NoChance.PvPManager.Dependencies.Hooks.FactionsUUID;
import me.NoChance.PvPManager.Dependencies.Hooks.SimpleClans;
import me.NoChance.PvPManager.Dependencies.Hooks.Vault;
import me.NoChance.PvPManager.Dependencies.Hooks.WorldGuard;
import me.NoChance.PvPManager.Dependencies.Hooks.WorldGuardLegacy;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.NoChance.PvPManager.Utils.Log;
import net.milkbowl.vault.economy.Economy;

public class DependencyManager {

	private final HashMap<Hook, Dependency> dependencies = new HashMap<>();
	private final HashSet<PvPlugin> attackChecks = new HashSet<>();

	public DependencyManager() {
		checkPlugin(Hook.FACTIONS, "Factions");
		checkPlugin(Hook.SIMPLECLANS, "SimpleClans");
		checkPlugin(Hook.VAULT, "Vault");
		checkPlugin(Hook.WORLDGUARD, "WorldGuard");
	}

	private void checkPlugin(final Hook hook, final String pluginName) {
		final Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
		try {
			switch (hook) {
			case FACTIONS:
				checkForFactions(plugin);
				break;
			case SIMPLECLANS:
				checkForSimpleClans(plugin);
				break;
			case VAULT:
				checkForVault(plugin);
				break;
			case WORLDGUARD:
				checkForWorldguard(plugin);
				break;
			default:
				break;
			}
		} catch (final NoClassDefFoundError | NoSuchMethodError e) {
			Log.warning("Your " + pluginName + " version is currently unsupported: " + plugin.getDescription().getFullName());
			Log.warning(pluginName + " support disabled");
		}
	}

	private void checkForVault(final Plugin plugin) {
		if (plugin != null) {
			final RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			Economy economy = null;
			if (economyProvider != null) {
				economy = economyProvider.getProvider();
			}
			if (economy != null) {
				Log.info("Vault Found! Using it for currency related features");
				dependencies.put(Hook.VAULT, new Vault(economy));
			} else {
				Log.severe("Error! No Economy plugin found");
			}
		} else {
			Log.severe("Vault not found! Features requiring Vault won't work!");
			Settings.setFineAmount(0);
			Settings.setMoneyPenalty(0);
			Settings.setMoneyReward(0);
		}
	}

	private void checkForWorldguard(final Plugin plugin) {
		if (plugin == null)
			return;

		if (CombatUtils.isVersionSuperior(plugin.getDescription().getVersion(), "6.2")) {
			dependencies.put(Hook.WORLDGUARD, new WorldGuard());
		} else {
			dependencies.put(Hook.WORLDGUARD, new WorldGuardLegacy());
		}
		Log.info("WorldGuard Found! Enabling WorldGuard Support");
	}

	private void checkForFactions(final Plugin plugin) {
		if (plugin == null)
			return;

		try {
			final String fVersion = plugin.getDescription().getVersion();
			if (fVersion.contains("U")) {
				final FactionsUUID factionsU = new FactionsUUID();
				dependencies.put(Hook.FACTIONS, factionsU);
				attackChecks.add(factionsU);
				Log.info("FactionsUUID Found! Hooked successfully");
			} else if (Integer.parseInt(fVersion.replace(".", "")) >= 270) {
				final Factions factions = new Factions();
				dependencies.put(Hook.FACTIONS, factions);
				attackChecks.add(factions);
				Log.info("Factions Found! Hooked successfully");
			} else {
				Log.info("Update your Factions plugin to the latest version if you want PvPManager to hook into it successfully");
			}
		} catch (final NumberFormatException e) {
			Log.warning("Couldn't read Factions version, maybe it's yet another fork?");
		}
	}

	private void checkForSimpleClans(final Plugin plugin) {
		if (plugin == null)
			return;
		final SimpleClans simpleClans = new SimpleClans();
		dependencies.put(Hook.SIMPLECLANS, simpleClans);
		attackChecks.add(simpleClans);
		Log.info("SimpleClans Found! Hooked successfully");
	}

	public final boolean canAttack(final Player attacker, final Player defender) {
		for (final PvPlugin pvPlugin : attackChecks)
			if (!pvPlugin.canAttack(attacker, defender))
				return false;
		return true;
	}

	public boolean worldguardCanAttack(final Location l) {
		return ((PvPlugin) dependencies.get(Hook.WORLDGUARD)).canBeAttacked(null, l);
	}

	public final boolean isDependencyEnabled(final Hook d) {
		return dependencies.containsKey(d);
	}

	public Dependency getDependency(final Hook h) {
		return dependencies.get(h);
	}

	public JavaPlugin getDepencyMainClass(final Hook h) {
		if (isDependencyEnabled(h))
			return dependencies.get(h).getMainClass();
		return null;
	}

	public final Economy getEconomy() {
		if (isDependencyEnabled(Hook.VAULT))
			return ((Vault) dependencies.get(Hook.VAULT)).getEconomy();
		return null;
	}

}
