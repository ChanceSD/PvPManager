package me.NoChance.PvPManager.Managers;

import java.util.HashMap;
import java.util.HashSet;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlugin;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Dependencies.Factions;
import me.NoChance.PvPManager.Dependencies.FactionsUUID;
import me.NoChance.PvPManager.Dependencies.WorldGuard;
import me.NoChance.PvPManager.Utils.Log;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class DependencyManager {

	private final PvPManager plugin;
	private boolean useWG;
	private Economy economy;
	private final HashMap<String, PvPlugin> dependencies = new HashMap<>();
	private final HashSet<PvPlugin> attackChecks = new HashSet<>();

	public DependencyManager(final PvPManager plugin) {
		this.plugin = plugin;
		checkForVault();
		checkForWorldguard();
		checkForFactions();
	}

	private void checkForVault() {
		if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			final RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager()
					.getRegistration(net.milkbowl.vault.economy.Economy.class);
			if (economyProvider != null) {
				economy = economyProvider.getProvider();
			}
			if (getEconomy() != null) {
				Log.info("Vault Found! Using it for currency related features");
			} else
				Log.severe("Error! No Economy plugin found");
		} else {
			Log.severe("Vault not found! Features requiring Vault won't work!");
			Variables.setFineEnabled(false);
			Variables.setMoneyPenalty(0);
			Variables.setMoneyReward(0);
		}
	}

	private void checkForWorldguard() {
		if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
			WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
			if (wg.getDescription().getVersion().startsWith("6")) {
				useWG = true;
				dependencies.put("WorldGuard", new WorldGuard(wg));
				Log.info("WorldGuard Found! Enabling WorldGuard Support");
			} else {
				Log.info("WorldGuard Found But Not Supported, Update Your WorldGuard Version!");
			}
		}
	}

	private void checkForFactions() {
		final Plugin factionsPlugin = Bukkit.getPluginManager().getPlugin("Factions");
		try {
			if (factionsPlugin != null) {
				final String fVersion = factionsPlugin.getDescription().getVersion();
				if (fVersion.contains("U")) {
					FactionsUUID factionsU = new FactionsUUID();
					dependencies.put("Factions", factionsU);
					attackChecks.add(factionsU);
					Log.info("FactionsUUID Found! Hooked successfully");
				} else if (Integer.parseInt(fVersion.replace(".", "")) >= 270) {
					Factions factions = new Factions();
					dependencies.put("Factions", factions);
					attackChecks.add(factions);
					Log.info("Factions Found! Hooked successfully");
				} else
					Log.info("Update your Factions plugin to the latest version if you want PvPManager to hook into it successfully");
			}
		} catch (final NumberFormatException e) {
			Log.warning("Couldn't read Factions version, maybe it's yet another fork?");
		}
	}

	public final boolean hasWGFlag(final Player attacker, final Player defender) {
		return useWG ? dependencies.get("WorldGuard").canAttack(attacker, defender) : false;
	}

	public final boolean canAttack(final Player attacker, final Player defender) {
		for (final PvPlugin pvPlugin : attackChecks) {
			if (!pvPlugin.canAttack(attacker, defender))
				return false;
		}
		return true;
	}

	public final boolean useWG() {
		return useWG;
	}

	public final Economy getEconomy() {
		return economy;
	}

}
