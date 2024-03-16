package me.NoChance.PvPManager.Libraries.Metrics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Dependencies.Dependency;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Settings.Settings;
import me.chancesd.sdutils.metrics.Metrics;

public class CustomMetrics {

	public CustomMetrics(final PvPManager plugin) {
		if (Settings.isReloading())
			return;
		initMetrics(plugin);
	}

	private void initMetrics(final PvPManager plugin) {

		final Metrics metrics = new Metrics(plugin, 5653, Settings.isOptOutMetrics());

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
				map.put("Drop Mode", getMapEntry(Settings.getDropMode().toString()));
				map.put("Combat Nametags", getMapEntry(Settings.useNameTag() ? "Enabled" : "Disabled"));
				map.put("Database Type", getMapEntry(plugin.getStorageManager().getStorage().getDatabaseType().toString()));
				if (Settings.isBossBarEnabled() && Settings.isActionBarEnabled())
					map.put("Display Type", getMapEntry("Both"));
				else if (!Settings.isBossBarEnabled() && !Settings.isActionBarEnabled())
					map.put("Display Type", getMapEntry("None"));
				else
					map.put("Display Type", getMapEntry(Settings.isBossBarEnabled() ? "Only Bossbar" : "Only Actionbar"));
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
				final List<String> extra = Arrays.asList("GSit", "NametagEdit", "GriefPrevention", "RedProtect", "GriefDefender", "Citizens",
						"KingdomsX", "CMI", "TAB");
				for (final String plugin : extra) {
					if (Bukkit.getPluginManager().isPluginEnabled(plugin)) {
						valueMap.put(plugin, 1);
					}
				}
				return valueMap;
			}
		}));

		metrics.addCustomChart(new Metrics.SimplePie("auto_soup", new Callable<String>() {
			@Override
			public String call() throws Exception {
				return Settings.isAutoSoupEnabled() ? "Enabled" : "Disabled";
			}
		}));

		metrics.addCustomChart(new Metrics.SimplePie("locale", new Callable<String>() {
			@Override
			public String call() throws Exception {
				return Settings.getLocale();
			}
		}));

		metrics.addCustomChart(new Metrics.SingleLineChart("players_in_combat", new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return PvPManager.getInstance().getPlayerHandler().getPlayersInCombat().size();
			}
		}));
	}

	private Map<String, Integer> getMapEntry(final String key) {
		final HashMap<String, Integer> result = new HashMap<>();
		result.put(key, 1);
		return result;
	}

}
