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

		metrics.addCustomChart(new Metrics.DrilldownPie("features", new Callable<Map<String, Map<String, Integer>>>() {
			@Override
			public Map<String, Map<String, Integer>> call() throws Exception {
				final Map<String, Map<String, Integer>> map = new HashMap<>();

				map.put("Newbie Protection", getMapEntry(Settings.isNewbieProtectionEnabled() ? "Enabled" : "Disabled"));
				map.put("Kill Abuse", getMapEntry(Settings.isKillAbuseEnabled() ? "Enabled" : "Disabled"));
				map.put("Update Check", getMapEntry(!Settings.isUpdateCheck() ? "Disabled" : !Settings.isAutoUpdate() ? "Update Check" : "Auto Update"));
				map.put("PvP Blood", getMapEntry(Settings.isPvpBlood() ? "Enabled" : "Disabled"));
				return map;
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
					valueMap.put(entry.getValue().getName(), 1);
				}
				return valueMap;
			}
		}));
	}

	private Map<String, Integer> getMapEntry(final String key) {
		final HashMap<String, Integer> result = new HashMap<>();
		result.put(key, 1);
		return result;
	}

}
