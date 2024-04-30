package me.chancesd.pvpmanager.library.metrics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.Dependency;
import me.chancesd.pvpmanager.setting.Settings;
import me.chancesd.sdutils.metrics.Metrics;

public class CustomMetrics {

	public CustomMetrics(final PvPManager plugin) {
		if (Settings.isReloading())
			return;
		initMetrics(plugin);
	}

	private void initMetrics(final PvPManager plugin) {

		final Metrics metrics = new Metrics(plugin, 5653, Settings.isOptOutMetrics());

		metrics.addCustomChart(
				new Metrics.SimplePie("time_in_combat", () -> Settings.isInCombatEnabled() ? Settings.getTimeInCombat() + " seconds" : "Disabled"));

		metrics.addCustomChart(new Metrics.DrilldownPie("features", () -> {
			final Map<String, Map<String, Integer>> map = new HashMap<>();

				map.put("Newbie Protection", getMapEntry(Settings.isNewbieProtectionEnabled() ? "Enabled" : "Disabled"));
				map.put("Kill Abuse", getMapEntry(Settings.isKillAbuseEnabled() ? "Enabled" : "Disabled"));
				map.put("Update Check",
						getMapEntry(!Settings.isUpdateCheck() ? "Disabled" : !Settings.isAutoUpdate() ? "Update Check" : "Auto Update"));
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
		));

		if (Settings.isInCombatEnabled()) {
			metrics.addCustomChart(new Metrics.SimplePie("player_drops_on_logout", () -> {
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
			}));
		}

		metrics.addCustomChart(new Metrics.AdvancedPie("hooks", () -> {
			final Map<String, Integer> valueMap = new HashMap<>();
			for (final Entry<Hook, Dependency> entry : plugin.getDependencyManager().getDependencies().entrySet()) {
				valueMap.put(entry.getValue().getName(), 1);
			}
			final List<String> extra = Arrays.asList("GSit", "NametagEdit", "RedProtect", "GriefDefender", "Citizens",
			"CMI", "TAB", "ProtectionStones");
			for (final String extraPlugin : extra) {
				if (Bukkit.getPluginManager().isPluginEnabled(extraPlugin)) {
					valueMap.put(extraPlugin, 1);
				}
			}
			return valueMap;
		}));

		metrics.addCustomChart(new Metrics.SimplePie("locale", Settings::getLocale));

		metrics.addCustomChart(
				new Metrics.SingleLineChart("players_in_combat", () -> PvPManager.getInstance().getPlayerHandler().getPlayersInCombat().size()));

		metrics.addCustomChart(new Metrics.DrilldownPie("blocked_actions", () -> {
			final Map<String, Map<String, Integer>> map = new HashMap<>();

			map.put("EnderPearls", getMapEntry(Settings.isBlockEnderPearl()));
			map.put("ChorusFruits", getMapEntry(Settings.isBlockChorusFruit()));
			map.put("Teleport", getMapEntry(Settings.isBlockTeleport()));
			map.put("Place Blocks", getMapEntry(Settings.isBlockPlaceBlocks()));
			map.put("Interact", getMapEntry(Settings.blockInteract()));
			map.put("Elytra", getMapEntry(Settings.isBlockGlide()));
			map.put("Eat", getMapEntry(Settings.isBlockEat()));
			map.put("Totem of Undying", getMapEntry(Settings.isBlockTotemUndying()));
			map.put("Open Inventory", getMapEntry(Settings.isBlockInventoryOpen()));
			final HashMap<String, Integer> result = new HashMap<>();
			if (Settings.isStopCommands()) {
				if (Settings.isCommandsWhitelist())
					result.put("Whitelist", 1);
				else
					result.put("Blacklist", 1);
			} else
				result.put("False", 1);
			map.put("Commands", result);
			return map;
		}));
	}

	private Map<String, Integer> getMapEntry(final boolean value) {
		return value ? getMapEntry("True") : getMapEntry("False");
	}

	private Map<String, Integer> getMapEntry(final String key) {
		final HashMap<String, Integer> result = new HashMap<>();
		result.put(key, 1);
		return result;
	}

}
