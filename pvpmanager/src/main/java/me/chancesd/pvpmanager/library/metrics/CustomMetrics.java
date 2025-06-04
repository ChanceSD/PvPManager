package me.chancesd.pvpmanager.library.metrics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.integration.type.Dependency;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.sdutils.metrics.Metrics;

public class CustomMetrics {

	public CustomMetrics(final PvPManager plugin) {
		if (plugin.isReloading())
			return;
		initMetrics(plugin);
	}

	private void initMetrics(final PvPManager plugin) {

		final Metrics metrics = new Metrics(plugin, 5653, Conf.METRICS_OPT_OUT.asBool());

		metrics.addCustomChart(
				new Metrics.SimplePie("time_in_combat",
						() -> Conf.COMBAT_TAG_ENABLED.asBool() ? Conf.TIME_IN_COMBAT.asInt() + " seconds" : "Disabled"));

		metrics.addCustomChart(new Metrics.DrilldownPie("features", () -> {
			final Map<String, Map<String, Integer>> map = new HashMap<>();

			map.put("Newbie Protection", getMapEntryFeature(Conf.NEWBIE_ENABLED.asBool()));
			map.put("Kill Abuse", getMapEntryFeature(Conf.KILL_ABUSE_ENABLED.asBool()));
			map.put("Update Check", getMapEntry(!Conf.CHECK_UPDATES.asBool() ? "Disabled" : !Conf.AUTO_UPDATE.asBool() ? "Update Check" : "Auto Update"));
			map.put("PvP Blood", getMapEntryFeature(Conf.PVP_BLOOD.asBool()));
			map.put("Drop Mode", getMapEntry(Conf.PLAYER_DROP_MODE.asEnum(Conf.DropMode.class).toString()));
			map.put("Combat Nametags", getMapEntryFeature(Conf.NAMETAG_COMBAT_ENABLED.asBool() || Conf.TOGGLE_NAMETAG_ENABLED.asBool()));
			map.put("Database Type", getMapEntry(plugin.getStorageManager().getStorage().getDatabaseType().toString()));
			if (Conf.BOSS_BAR_ENABLED.asBool() && Conf.ACTION_BAR_ENABLED.asBool())
				map.put("Display Type", getMapEntry("Both"));
			else if (!Conf.BOSS_BAR_ENABLED.asBool() && !Conf.ACTION_BAR_ENABLED.asBool())
				map.put("Display Type", getMapEntry("None"));
			else
				map.put("Display Type", getMapEntry(Conf.BOSS_BAR_ENABLED.asBool() ? "Only Bossbar" : "Only Actionbar"));
			return map;
		}));

		if (Conf.COMBAT_TAG_ENABLED.asBool()) {
			metrics.addCustomChart(new Metrics.SimplePie("player_drops_on_logout", () -> {
				if (!Conf.KILL_ON_LOGOUT.asBool())
					return "Kill On Logout Disabled";
				else if (!Conf.DROP_INVENTORY.asBool() && !Conf.DROP_EXP.asBool() && !Conf.DROP_ARMOR.asBool())
					return "Keep Everything";
				else if (Conf.DROP_INVENTORY.asBool() && Conf.DROP_EXP.asBool() && Conf.DROP_ARMOR.asBool())
					return "Drop Everything";
				else if (!Conf.DROP_INVENTORY.asBool() && !Conf.DROP_ARMOR.asBool() && Conf.DROP_EXP.asBool())
					return "Only Drop Exp";
				else if (!Conf.DROP_INVENTORY.asBool() && !Conf.DROP_EXP.asBool() && Conf.DROP_ARMOR.asBool())
					return "Only Drop Armor";
				else if (Conf.DROP_INVENTORY.asBool() && !Conf.DROP_EXP.asBool() && !Conf.DROP_ARMOR.asBool())
					return "Only Drop Inventory";
				else if (!Conf.DROP_INVENTORY.asBool() && Conf.DROP_EXP.asBool() && Conf.DROP_ARMOR.asBool())
					return "Only Keep Inventory";
				else if (Conf.DROP_INVENTORY.asBool() && Conf.DROP_EXP.asBool() && !Conf.DROP_ARMOR.asBool())
					return "Only Keep Armor";
				else if (Conf.DROP_INVENTORY.asBool() && !Conf.DROP_EXP.asBool() && Conf.DROP_ARMOR.asBool())
					return "Only Keep Exp";
				return "";
			}));
		}

		metrics.addCustomChart(new Metrics.AdvancedPie("hooks", () -> {
			final Map<String, Integer> valueMap = new HashMap<>();
			for (final Dependency entry : plugin.getDependencyManager().getDependencies()) {
				valueMap.put(entry.getName(), 1);
			}
			final List<String> extra = Arrays.asList("GSit", "NametagEdit", "RedProtect", "GriefDefender", "Citizens",
					"CMI", "ProtectionStones");
			for (final String extraPlugin : extra) {
				if (Bukkit.getPluginManager().isPluginEnabled(extraPlugin)) {
					valueMap.put(extraPlugin, 1);
				}
			}
			return valueMap;
		}));

		metrics.addCustomChart(new Metrics.SimplePie("locale", Conf.LOCALE::asString));

		metrics.addCustomChart(
				new Metrics.SingleLineChart("players_in_combat", () -> PvPManager.getInstance().getPlayerManager().getPlayersInCombat().size()));

		metrics.addCustomChart(new Metrics.DrilldownPie("blocked_actions", () -> {
			final Map<String, Map<String, Integer>> map = new HashMap<>();

			map.put("EnderPearls", getMapEntry(Conf.BLOCK_ENDERPEARL.asBool()));
			map.put("ChorusFruits", getMapEntry(Conf.BLOCK_CHORUSFRUIT.asBool()));
			map.put("Teleport", getMapEntry(Conf.BLOCK_TELEPORT.asBool()));
			map.put("Place Blocks", getMapEntry(Conf.BLOCK_PLACE_BLOCKS.asBool()));
			map.put("Interact", getMapEntry(Conf.BLOCK_INTERACT_IN_COMBAT.asBool()));
			map.put("Elytra", getMapEntry(Conf.BLOCK_GLIDE_IN_COMBAT.asBool()));
			map.put("Eat", getMapEntry(Conf.BLOCK_EAT.asBool()));
			map.put("Totem of Undying", getMapEntry(Conf.BLOCK_TOTEM_UNDYING.asBool()));
			map.put("Open Inventory", getMapEntry(Conf.BLOCK_INVENTORY_OPEN.asBool()));
			final HashMap<String, Integer> result = new HashMap<>();
			if (Conf.BLOCK_COMMANDS.asBool()) {
				if (Conf.BLOCK_COMMANDS_WHITELIST.asBool())
					result.put("Whitelist", 1);
				else
					result.put("Blacklist", 1);
			} else
				result.put("False", 1);
			map.put("Commands", result);
			return map;
		}));
	}

	private Map<String, Integer> getMapEntryFeature(final boolean value) {
		return value ? getMapEntry("Enabled") : getMapEntry("Disabled");
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
