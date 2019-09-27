package me.NoChance.PvPManager.Libraries.Metrics;

import java.util.concurrent.Callable;

import org.bukkit.plugin.java.JavaPlugin;

import me.NoChance.PvPManager.Settings.Settings;

public class CustomMetrics {

	public CustomMetrics(final JavaPlugin plugin) {
		initMetrics(plugin);
	}

	private void initMetrics(final JavaPlugin plugin) {

		final Metrics metrics = new Metrics(plugin);

		metrics.addCustomChart(new Metrics.SimplePie("time_in_combat", new Callable<String>() {
			@Override
			public String call() throws Exception {
				return Settings.isInCombatEnabled() ? Settings.getTimeInCombat() + " seconds" : "Disabled";
			}
		}));

		metrics.addCustomChart(new Metrics.SimplePie("newbie_protection_usage", new Callable<String>() {
			@Override
			public String call() throws Exception {
				return Settings.isNewbieProtectionEnabled() ? "Enabled" : "Disabled";
			}
		}));

		metrics.addCustomChart(new Metrics.SimplePie("update_check_usage", new Callable<String>() {
			@Override
			public String call() throws Exception {
				if (!Settings.isUpdateCheck())
					return "Disabled";
				return !Settings.isAutoUpdate() ? "Update Check" : "Auto Update";
			}
		}));

		metrics.addCustomChart(new Metrics.SimplePie("kill_abuse_usage", new Callable<String>() {
			@Override
			public String call() throws Exception {
				return Settings.isKillAbuseEnabled() ? "Enabled" : "Disabled";
			}
		}));

		metrics.addCustomChart(new Metrics.SimplePie("blood_usage", new Callable<String>() {
			@Override
			public String call() throws Exception {
				return Settings.isPvpBlood() ? "Enabled" : "Disabled";
			}
		}));

		if (Settings.isInCombatEnabled()) {
			metrics.addCustomChart(new Metrics.SimplePie("player_drops_on_logout", new Callable<String>() {
				@Override
				public String call() throws Exception {
					if (!Settings.isKillOnLogout())
						return "Kill On Logout Disabled";
					else if (!Settings.isDropInventory() && !Settings.isDropExp() && !Settings.isDropArmor())
						return "Keep Everything";
					else if (Settings.isDropInventory() && Settings.isDropExp() && Settings.isDropArmor())
						return "Drop Everything";
					else if (!Settings.isDropInventory() && !Settings.isDropArmor() && Settings.isDropExp())
						return "Only Drop Exp";
					else if (!Settings.isDropInventory() && !Settings.isDropExp() && Settings.isDropArmor())
						return "Only Drop Armor";
					else if (Settings.isDropInventory() && !Settings.isDropExp() && !Settings.isDropArmor())
						return "Only Drop Inventory";
					else if (!Settings.isDropInventory() && Settings.isDropExp() && Settings.isDropArmor())
						return "Only Keep Inventory";
					else if (Settings.isDropInventory() && Settings.isDropExp() && !Settings.isDropArmor())
						return "Only Keep Armor";
					else if (Settings.isDropInventory() && !Settings.isDropExp() && Settings.isDropArmor())
						return "Only Keep Exp";
					return "";
				}
			}));

		}

	}
}
