package me.NoChance.PvPManager.Managers;

import java.util.HashSet;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlugin;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Dependencies.Factions;
import me.NoChance.PvPManager.Dependencies.FactionsUUID;
import me.NoChance.PvPManager.Listeners.WGListener;
import me.NoChance.PvPManager.Utils.Log;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class DependencyManager {

	private final PvPManager plugin;
	private Economy economy;
	private final HashSet<PvPlugin> dependencies = new HashSet<PvPlugin>();

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
			plugin.registerListener(new WGListener(plugin.getPlayerHandler()));
			Log.info("WorldGuard Found! Enabling WorldGuard Support");
		}
	}

	private void checkForFactions() {
		final Plugin factions = Bukkit.getPluginManager().getPlugin("Factions");
		try {
			if (factions != null) {
				final String fVersion = factions.getDescription().getVersion();
				if (fVersion.contains("U")) {
					dependencies.add(new FactionsUUID());
					Log.info("FactionsUUID Found! Hooked successfully");
				} else if (Integer.valueOf(fVersion.replace(".", "")) >= 270) {
					dependencies.add(new Factions());
					Log.info("Factions Found! Hooked successfully");
				} else
					Log.info("Update your Factions plugin to the latest version if you want PvPManager to hook into it successfully");
			}
		} catch (final NumberFormatException e) {
			Log.warning("Couldn't read Factions version, maybe it's a fork?");
		}
	}

	public final boolean canAttack(final Player attacker, final Player defender) {
		for (final PvPlugin pvPlugin : dependencies) {
			if (!pvPlugin.canAttack(attacker, defender))
				return false;
		}
		return true;
	}

	public final Economy getEconomy() {
		return economy;
	}

}
