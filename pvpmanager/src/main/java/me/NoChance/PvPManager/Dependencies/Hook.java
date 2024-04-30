package me.NoChance.PvPManager.Dependencies;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import me.chancesd.pvpmanager.setting.Settings;

public enum Hook {
	WORLDGUARD("WorldGuard"),
	ESSENTIALS("Essentials"),
	VAULT("Vault", "Vault not found! Features requiring Vault won't work!", () -> {
		Settings.setFineAmount(0);
		Settings.setMoneyPenalty(0);
		Settings.setMoneyReward(0);
	}),
	SIMPLECLANS("SimpleClans"),
	PLACEHOLDERAPI("PlaceholderAPI"),
	LIBSDISGUISES("LibsDisguises"),
	GRIEFPREVENTION("GriefPrevention"),
	TOWNY("Towny"),
	HUSKTOWNS("HuskTowns"),
	HUSKCLAIMS("HuskClaims"),
	KINGDOMSX("Kingdoms");

	@NotNull
	private final String pluginName;
	private final String disabledWarning;
	private final Runnable disabledAction;

	private Hook(@NotNull final String name) {
		this(name, null, () -> {
		});
	}

	private Hook(@NotNull final String name, final String disabledWarning, final Runnable disabledAction) {
		this.pluginName = name;
		this.disabledWarning = disabledWarning;
		this.disabledAction = disabledAction;
	}

	public boolean isEnabled() {
		return Bukkit.getPluginManager().isPluginEnabled(pluginName);
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
