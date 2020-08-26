package me.NoChance.PvPManager.Dependencies;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import me.NoChance.PvPManager.Settings.Settings;

public enum Hook {
	WORLDGUARD("WorldGuard"), ESSENTIALS("Essentials"), FACTIONS("Factions"), VAULT("Vault", "Vault not found! Features requiring Vault won't work!", () -> {
		Settings.setFineAmount(0);
		Settings.setMoneyPenalty(0);
		Settings.setMoneyReward(0);
	}), SIMPLECLANS("SimpleClans"), COMMANDBOOK("CommandBook"), PLACEHOLDERAPI("PlaceholderAPI"), LIBSDISGUISES("LibsDisguises");

	private final String pluginName;
	private String disabledWarning;
	private Runnable disabledAction;

	private Hook(final String name) {
		this(name, null, () -> {
		});
	}

	private Hook(final String name, final String disabledWarning, final Runnable disabledAction) {
		this.pluginName = name;
		this.disabledWarning = disabledWarning;
		this.disabledAction = disabledAction;
	}

	public boolean isEnabled() {
		return getPlugin() != null;
	}

	public String getVersion() {
		return getDescription().getVersion();
	}

	public PluginDescriptionFile getDescription() {
		return getPlugin().getDescription();
	}

	public Plugin getPlugin() {
		return Bukkit.getPluginManager().getPlugin(pluginName);
	}

	public String getDisabledWarning() {
		return disabledWarning;
	}

	public Runnable getDisabledAction() {
		return disabledAction;
	}

	@Override
	public String toString() {
		return pluginName;
	}
}
