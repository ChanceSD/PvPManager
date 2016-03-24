package me.NoChance.PvPManager.Dependencies;

import me.NoChance.PvPManager.Dependency;
import net.milkbowl.vault.economy.Economy;

public class Vault implements Dependency {

	private final Economy economy;

	public Vault(final Economy economy) {
		this.economy = economy;
	}

	@Override
	public Object getMainClass() {
		return economy;
	}

}
