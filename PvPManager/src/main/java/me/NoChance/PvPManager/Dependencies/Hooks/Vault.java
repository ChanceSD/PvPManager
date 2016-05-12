package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import me.NoChance.PvPManager.Dependencies.Dependency;
import net.milkbowl.vault.economy.Economy;

public class Vault implements Dependency {

	private final Economy economy;
	private final net.milkbowl.vault.Vault vault;

	public Vault(final Economy economy) {
		this.economy = economy;
		this.vault = (net.milkbowl.vault.Vault) Bukkit.getPluginManager().getPlugin("Vault");
	}

	@Override
	public JavaPlugin getMainClass() {
		return vault;
	}

	public Economy getEconomy() {
		return economy;
	}

}
