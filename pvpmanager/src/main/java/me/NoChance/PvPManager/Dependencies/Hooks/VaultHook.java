package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.DependencyException;
import me.NoChance.PvPManager.Dependencies.Hook;
import net.milkbowl.vault.economy.Economy;

public class VaultHook extends BaseDependency {

	private final Economy economy;

	public VaultHook(final Hook hook) {
		super(hook);
		this.economy = setupEconomy();
	}

	private Economy setupEconomy() {
		final RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
		Economy eco = null;
		if (economyProvider != null) {
			eco = economyProvider.getProvider();
		} else {
			throw new DependencyException("Error loading Vault! No Economy plugin found", Hook.VAULT);
		}
		return eco;
	}

	@Override
	public String onEnableMessage() {
		return "Vault Found! Using it for currency related features";
	}

	public Economy getEconomy() {
		return economy;
	}

}
