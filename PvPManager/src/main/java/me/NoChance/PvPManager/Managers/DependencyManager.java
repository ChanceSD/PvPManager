package me.NoChance.PvPManager.Managers;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.NoChance.PvPManager.Dependency;
import me.NoChance.PvPManager.PvPlugin;
import me.NoChance.PvPManager.Config.Settings;
import me.NoChance.PvPManager.Dependencies.Factions;
import me.NoChance.PvPManager.Dependencies.FactionsUUID;
import me.NoChance.PvPManager.Dependencies.Vault;
import me.NoChance.PvPManager.Dependencies.WorldGuard;
import me.NoChance.PvPManager.Utils.Log;
import net.milkbowl.vault.economy.Economy;

public class DependencyManager {

	private final HashMap<String, Dependency> dependencies = new HashMap<>();
	private final HashSet<PvPlugin> attackChecks = new HashSet<>();

	public DependencyManager() {
		checkForVault();
		checkForWorldguard();
		checkForFactions();
	}

	private void checkForVault() {
		if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			final RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			Economy economy = null;
			if (economyProvider != null) {
				economy = economyProvider.getProvider();
			}
			if (economy != null) {
				Log.info("Vault Found! Using it for currency related features");
				dependencies.put("Vault", new Vault(economy));
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

	private void checkForWorldguard() {
		if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
			dependencies.put("WorldGuard", new WorldGuard());
			Log.info("WorldGuard Found! Enabling WorldGuard Support");
		}
	}

	private void checkForFactions() {
		final Plugin factionsPlugin = Bukkit.getPluginManager().getPlugin("Factions");
		try {
			if (factionsPlugin != null) {
				final String fVersion = factionsPlugin.getDescription().getVersion();
				if (fVersion.contains("U")) {
					final FactionsUUID factionsU = new FactionsUUID();
					dependencies.put("Factions", factionsU);
					attackChecks.add(factionsU);
					Log.info("FactionsUUID Found! Hooked successfully");
				} else if (Integer.parseInt(fVersion.replace(".", "")) >= 270) {
					final Factions factions = new Factions();
					dependencies.put("Factions", factions);
					attackChecks.add(factions);
					Log.info("Factions Found! Hooked successfully");
				} else {
					Log.info("Update your Factions plugin to the latest version if you want PvPManager to hook into it successfully");
				}
			}
		} catch (final NumberFormatException e) {
			Log.warning("Couldn't read Factions version, maybe it's yet another fork?");
		}
	}

	public final boolean canAttack(final Player attacker, final Player defender) {
		for (final PvPlugin pvPlugin : attackChecks)
			if (!pvPlugin.canAttack(attacker, defender))
				return false;
		return true;
	}

	public boolean worldguardCanAttack(final Player p) {
		final PvPlugin wg = (PvPlugin) dependencies.get("WorldGuard");
		return wg.canAttack(null, p);
	}

	public final boolean isDependencyEnabled(final String s) {
		return dependencies.containsKey(s);
	}

	public Object getDependency(final String s) {
		if (isDependencyEnabled(s))
			return dependencies.get(s).getMainClass();
		return null;
	}

	public final Economy getEconomy() {
		if (isDependencyEnabled("Vault"))
			return (Economy) dependencies.get("Vault").getMainClass();
		return null;
	}

}
