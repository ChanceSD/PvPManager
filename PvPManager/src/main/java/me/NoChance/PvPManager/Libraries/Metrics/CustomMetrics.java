package me.NoChance.PvPManager.Libraries.Metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Dependencies.Dependency;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Settings.Settings;

public class CustomMetrics {

	public CustomMetrics(final PvPManager plugin) {
		initMetrics(plugin);
	}

	private void initMetrics(final PvPManager plugin) {

		final Metrics metrics = new Metrics(plugin);

		metrics.addCustomChart(new Metrics.SimplePie("time_in_combat", new Callable<String>() {
			@Override
			public String call() {
				return Settings.isInCombatEnabled() ? Settings.getTimeInCombat() + " seconds" : "Disabled";
			}
		}));

		metrics.addCustomChart(new Metrics.SimplePie("newbie_protection_usage", new Callable<String>() {
			@Override
			public String call() {
				return Settings.isNewbieProtectionEnabled() ? "Enabled" : "Disabled";
			}
		}));

		metrics.addCustomChart(new Metrics.SimplePie("update_check_usage", new Callable<String>() {
			@Override
			public String call() {
				if (!Settings.isUpdateCheck())
					return "Disabled";
				return !Settings.isAutoUpdate() ? "Update Check" : "Auto Update";
			}
		}));

		metrics.addCustomChart(new Metrics.SimplePie("kill_abuse_usage", new Callable<String>() {
			@Override
			public String call() {
				return Settings.isKillAbuseEnabled() ? "Enabled" : "Disabled";
			}
		}));

		metrics.addCustomChart(new Metrics.SimplePie("blood_usage", new Callable<String>() {
			@Override
			public String call() {
				return Settings.isPvpBlood() ? "Enabled" : "Disabled";
			}
		}));

		if (Settings.isInCombatEnabled()) {
			metrics.addCustomChart(new Metrics.SimplePie("player_drops_on_logout", new Callable<String>() {
				@Override
				public String call() {
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

		metrics.addCustomChart(new Metrics.AdvancedPie("hooks", new Callable<Map<String, Integer>>() {
			@Override
			public Map<String, Integer> call() {
				final Map<String, Integer> valueMap = new HashMap<>();
				for (final Entry<Hook, Dependency> entry : plugin.getDependencyManager().getDependencies().entrySet()) {
					valueMap.put(entry.getValue().getClass().getSimpleName(), 1);
				}
				return valueMap;
			}
		}));
	}
}
