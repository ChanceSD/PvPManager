package me.chancesd.pvpmanager.integration.hook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.DependencyException;
import me.chancesd.pvpmanager.integration.Hook;
import net.milkbowl.vault.economy.Economy;

public class VaultHook extends BaseDependency {

	private final Economy economy;

	public VaultHook(final Hook hook) {
		super(hook);
		this.economy = setupEconomy();
	}

	private Economy setupEconomy() {
		final RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
		if (economyProvider != null)
			return economyProvider.getProvider();
		else
			throw new DependencyException("Error loading Vault! No Economy plugin found", Hook.VAULT);
	}

	@Override
	public String onEnableMessage() {
		return "Vault Found! Using it for currency related features";
	}

	public Economy getEconomy() {
		return economy;
	}

}
