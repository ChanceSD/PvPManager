package me.chancesd.pvpmanager.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import java.util.Set;
import java.util.function.Function;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.setting.conf.ConfSection;
import me.chancesd.pvpmanager.utils.ChatUtils;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.MCVersion;

public enum Conf {
	// General section
	LOCALE(ConfSection.GENERAL, "Locale", "EN", String.class),
	USE_TEAMS(ConfSection.GENERAL, "Use Scoreboard Teams", true, Boolean.class),
	WORLD_EXCLUSIONS(ConfSection.GENERAL, "World Exclusions", Conf::getSet, List.class, Set.class),

	// Combat tag section
	COMBAT_TAG_ENABLED(ConfSection.COMBAT_TAG, ConfSection.ENABLED, true, Boolean.class),
	TIME_IN_COMBAT(ConfSection.COMBAT_TAG, "Time", 20, Integer.class),
	GLOWING_IN_COMBAT(ConfSection.COMBAT_TAG, "Glowing", true, Boolean.class),
	UNTAG_ON_KILL(ConfSection.COMBAT_TAG, "Untag On Kill", false, Boolean.class),
	SELF_TAG(ConfSection.COMBAT_TAG, "Self Tag", false, Boolean.class),
	PEARL_RENEW_TAG(ConfSection.COMBAT_TAG, "EnderPearl Renews Tag", true, Boolean.class),

	NAMETAG_COMBAT_ENABLED(ConfSection.NAMETAG, ConfSection.ENABLED, true, Boolean.class),
	NAMETAG_PREFIX(ConfSection.NAMETAG, "Prefix", "&4&lCombat &c", String.class),
	NAMETAG_SUFFIX(ConfSection.NAMETAG, "Suffix", "", String.class),

	ACTION_BAR_ENABLED(ConfSection.ACTION_BAR, ConfSection.ENABLED, true, Boolean.class),
	ACTION_BAR_MESSAGE(ConfSection.ACTION_BAR, "Message", ChatUtils::colorize, String.class, String.class),
	ACTION_BAR_SYMBOL(ConfSection.ACTION_BAR, "Symbol", "▊", String.class),
	ACTION_BAR_BARS(ConfSection.ACTION_BAR, "Total Bars", 20, Integer.class),

	BOSS_BAR_ENABLED(ConfSection.BOSS_BAR, ConfSection.ENABLED, bool -> MCVersion.isAtLeast(MCVersion.V1_9) && bool, Boolean.class, Boolean.class),
	BOSS_BAR_MESSAGE(ConfSection.BOSS_BAR, "Message", ChatUtils::colorize, String.class, String.class),
	BOSS_BAR_COLOR(ConfSection.BOSS_BAR, "BarColor", s -> Conf.loadEnum("org.bukkit.boss.BarColor", (String) s, MCVersion.V1_9)),
	BOSS_BAR_STYLE(ConfSection.BOSS_BAR, "BarStyle", s -> Conf.loadEnum("org.bukkit.boss.BarStyle", (String) s, MCVersion.V1_9)),

	// Actions blocked in combat
	BLOCK_ENDERPEARL(ConfSection.ACTIONS_BLOCKED, "EnderPearls", false, Boolean.class),
	BLOCK_CHORUSFRUIT(ConfSection.ACTIONS_BLOCKED, "ChorusFruits", false, Boolean.class),
	BLOCK_TELEPORT(ConfSection.ACTIONS_BLOCKED, "Teleport", true, Boolean.class),
	BLOCK_GLIDE_IN_COMBAT(ConfSection.ACTIONS_BLOCKED, "Elytra", false, Boolean.class),
	BLOCK_EAT(ConfSection.ACTIONS_BLOCKED, "Eat", false, Boolean.class),
	BLOCK_TOTEM_UNDYING(ConfSection.ACTIONS_BLOCKED, "Totem of Undying", false, Boolean.class),
	BLOCK_PLACE_BLOCKS(ConfSection.ACTIONS_BLOCKED, "Place Blocks", false, Boolean.class),
	BLOCK_BREAK_BLOCKS(ConfSection.ACTIONS_BLOCKED, "Break Blocks", false, Boolean.class),
	BLOCK_INVENTORY_OPEN(ConfSection.ACTIONS_BLOCKED, "Open Inventory", false, Boolean.class),
	BLOCK_INTERACT_IN_COMBAT(ConfSection.ACTIONS_BLOCKED, "Interact.Enabled", false, Boolean.class),
	BLOCK_INTERACT_ITEM_LIST(ConfSection.ACTIONS_BLOCKED, "Interact.List", new ArrayList<>(), List.class),
	BLOCK_COMMANDS(ConfSection.ACTIONS_BLOCKED, "Commands.Enabled", true, Boolean.class),
	BLOCK_COMMANDS_WHITELIST(ConfSection.ACTIONS_BLOCKED, "Commands.Whitelist", true, Boolean.class),
	COMMANDS_ALLOWED(ConfSection.ACTIONS_BLOCKED, "Commands.Command List", Conf::getList, List.class, List.class),

	// Combat log punishments
	FINE_AMOUNT(ConfSection.COMBAT_LOG, "Money Penalty", 0.0, Double.class),
	KILL_ON_LOGOUT(ConfSection.COMBAT_LOG, "Kill on Logout.Enabled", true, Boolean.class),
	DROP_INVENTORY(ConfSection.COMBAT_LOG, "Kill on Logout.Player Drops.Inventory", true, Boolean.class),
	DROP_EXP(ConfSection.COMBAT_LOG, "Kill on Logout.Player Drops.Experience", true, Boolean.class),
	DROP_ARMOR(ConfSection.COMBAT_LOG, "Kill on Logout.Player Drops.Armor", true, Boolean.class),

	PUNISH_ON_KICK(ConfSection.COMBAT_LOG, "Punish On Kick.Enabled", true, Boolean.class),
	MATCH_KICK_REASON(ConfSection.COMBAT_LOG, "Punish On Kick.Match Kick Reason", false, Boolean.class),
	PUNISH_KICK_REASONS(ConfSection.COMBAT_LOG, "Punish On Kick.Kick Reasons", Conf::getList, List.class, List.class),
	COMMANDS_ON_COMBATLOG(ConfSection.COMBAT_LOG, "Commands On Combat Log", Conf::getCommandList, List.class, List.class),

	// PvP Toggle
	DEFAULT_PVP(ConfSection.PVP_TOGGLE, "Default PvP", true, Boolean.class),
	TOGGLE_COOLDOWN(ConfSection.PVP_TOGGLE, "Cooldown", 15, Integer.class),
	TOGGLE_NAMETAG_ENABLED(ConfSection.PVP_TOGGLE, "NameTags.Enabled", false, Boolean.class),
	TOGGLE_PREFIX_ON(ConfSection.PVP_TOGGLE, "NameTags.Prefix On", "&1", String.class),
	TOGGLE_PREFIX_OFF(ConfSection.PVP_TOGGLE, "NameTags.Prefix Off", "&2", String.class),
	PVP_DISABLED_FEE(ConfSection.PVP_TOGGLE, "PvP Disabled Money Fee", 0, Integer.class),
	COMMANDS_PVP_ON(ConfSection.PVP_TOGGLE, "Commands PvP On", Conf::getCommandList, List.class, List.class),
	COMMANDS_PVP_OFF(ConfSection.PVP_TOGGLE, "Commands PvP Off", Conf::getCommandList, List.class, List.class),
	WORLDGUARD_OVERRIDES(ConfSection.PVP_TOGGLE, "WorldGuard Overrides.Enabled", true, Boolean.class),
	WORLDGUARD_OVERRIDES_LIST(ConfSection.PVP_TOGGLE, "WorldGuard Overrides.Region List", Conf::getSet, List.class, Set.class),

	// Anti border hopping
	VULNERABLE_ENABLED(ConfSection.BORDER_HOPPING, "Vulnerable.Enabled", true, Boolean.class),
	VULNERABLE_RENEW_TAG(ConfSection.BORDER_HOPPING, "Vulnerable.Renew Combat Tag", true, Boolean.class),
	PUSHBACK_ENABLED(ConfSection.BORDER_HOPPING, "Push Back.Enabled", true, Boolean.class),
	PUSHBACK_FORCE(ConfSection.BORDER_HOPPING, "Push Back.Force", 1.2, Double.class),
	PUSHBACK_REMOVE_ELYTRA(ConfSection.BORDER_HOPPING, "Push Back.Remove Elytra", false, Boolean.class),

	// Newbie protection
	NEWBIE_ENABLED(ConfSection.NEWBIE_PROTECTION, ConfSection.ENABLED, true, Boolean.class),
	NEWBIE_TIME(ConfSection.NEWBIE_PROTECTION, "Time", 600, Integer.class),
	NEWBIE_ALLOW_DISABLE(ConfSection.NEWBIE_PROTECTION, "Allow Player Disable", true, Boolean.class),
	NEWBIE_BLOCK_PICK(ConfSection.NEWBIE_PROTECTION, "Block Pick Items", false, Boolean.class),
	NEWBIE_BLOCK_PLACE(ConfSection.NEWBIE_PROTECTION, "Block Place Blocks", false, Boolean.class),
	NEWBIE_BLOCK_BREAK(ConfSection.NEWBIE_PROTECTION, "Block Break Blocks", false, Boolean.class),
	NEWBIE_GODMODE(ConfSection.NEWBIE_PROTECTION, "Protect From Everything", false, Boolean.class),
	NEWBIE_BLACKLIST(ConfSection.NEWBIE_PROTECTION, "Command Blacklist", Conf::getList, List.class, List.class),

	// Item cooldowns
	ITEM_COOLDOWNS(ConfSection.ITEM_COOLDOWNS, "", Conf::loadItemCooldowns, ConfigurationSection.class, Map.class),

	// Player kills
	MONEY_REWARD(ConfSection.PLAYER_KILLS, "Money Reward", 0.0, Double.class),
	MONEY_PENALTY(ConfSection.PLAYER_KILLS, "Money Penalty", 0.0, Double.class),
	MONEY_STEAL(ConfSection.PLAYER_KILLS, "Money Steal", false, Boolean.class),
	EXP_STEAL(ConfSection.PLAYER_KILLS, "Exp Steal", 0.0, Double.class),
	COMMANDS_ON_KILL(ConfSection.PLAYER_KILLS, "Commands On Kill", Conf::getCommandList, List.class, List.class),
	KILLS_WG_EXCLUSIONS(ConfSection.PLAYER_KILLS, "WorldGuard Exclusions", Conf::getSet, List.class, Set.class),

	KILL_ABUSE_ENABLED(ConfSection.ANTI_KILL_ABUSE, ConfSection.ENABLED, true, Boolean.class),
	KILL_ABUSE_MAX(ConfSection.ANTI_KILL_ABUSE, "Max Kills", 5, Integer.class),
	KILL_ABUSE_TIME(ConfSection.ANTI_KILL_ABUSE, "Time Limit", 20, Integer.class),
	KILL_ABUSE_WARN(ConfSection.ANTI_KILL_ABUSE, "Warn Before", true, Boolean.class),
	KILL_ABUSE_COMMANDS(ConfSection.ANTI_KILL_ABUSE, "Commands on Abuse", Conf::getCommandList, List.class, List.class),
	RESPAWN_PROTECTION(ConfSection.ANTI_KILL_ABUSE, "Respawn Protection", 3, Integer.class),

	// Other settings
	PVP_BLOOD(ConfSection.OTHER_SETTINGS, "PvP Blood", true, Boolean.class),
	IGNORE_NO_DMG_HITS(ConfSection.OTHER_SETTINGS, "Ignore No Damage Hits", true, Boolean.class),
	PLAYER_DROP_MODE(ConfSection.OTHER_SETTINGS, "Player Drop Mode", s -> Conf.loadEnum(DropMode.class, s), String.class, DropMode.class),
	HEALTH_BELOW_NAME(ConfSection.OTHER_SETTINGS, "Show health under name.Enabled", true, Boolean.class),
	HEALTH_BELOW_NAME_SYMBOL(ConfSection.OTHER_SETTINGS, "Show health under name.Display Name", ChatUtils::colorize, String.class, String.class),

	// Disable on hit
	DISABLE_FLY(ConfSection.DISABLE_ON_HIT, "Fly", true, Boolean.class),
	DISABLE_GAMEMODE(ConfSection.DISABLE_ON_HIT, "GameMode", true, Boolean.class),
	DISABLE_DISGUISE(ConfSection.DISABLE_ON_HIT, "Disguise", true, Boolean.class),
	DISABLE_GODMODE(ConfSection.DISABLE_ON_HIT, "GodMode", true, Boolean.class),
	DISABLE_ELYTRA(ConfSection.DISABLE_ON_HIT, "Elytra", false, Boolean.class),
	DISABLE_INVISIBILITY(ConfSection.DISABLE_ON_HIT, "Invisibility", false, Boolean.class),

	CHECK_UPDATES("Update Check", ConfSection.ENABLED, true, Boolean.class),
	AUTO_UPDATE("Update Check", "Auto Update", true, Boolean.class),
	METRICS_OPT_OUT("Metrics", "Opt-out", false, Boolean.class),
	HARMFUL_POTIONS("Harmful Potions", "", Conf::getSet, List.class, Set.class),

	DEBUG_MODE("Debug Mode", "", Conf::setDebug, Boolean.class, Boolean.class);

	public enum DropMode {
		ALWAYS, DROP, KEEP, TRANSFER, CLEAR
	}

	private final String section;
	private final String configKey;
	private final Object def;
	private Function<Object, ?> function;
	private final Class<?> clazz;
	private final Class<?> clazzResult;

	private static YamlConfiguration config;
	private static PvPManager plugin;

	// This isn't pretty but not sure how else to do it without extra casting overhead/complications
	private boolean boolValue;
	private String stringValue;
	private int intValue;
	private double doubleValue;
	private List<String> stringList;
	private Set<String> stringSet;
	private Enum<?> enumValue;
	private Map<Material, ItemCooldown> map;

	private <T> Conf(final String section, final String configKey, final T def, final Class<T> clazz) {
		this.section = section;
		this.configKey = configKey;
		this.def = def;
		this.clazz = clazz;
		this.clazzResult = null;
	}

	@SuppressWarnings("unchecked")
	private <T, R> Conf(final String section, final String configKey, final Function<T, R> function, final Class<T> clazz, final Class<R> clazzResult) {
		this.section = section;
		this.configKey = configKey;
		this.def = null;
		this.clazz = clazz;
		this.function = (Function<Object, ?>) function;
		this.clazzResult = clazzResult;
	}

	private <R> Conf(final String section, final String configKey, final Function<Object, R> function) {
		this.section = section;
		this.configKey = configKey;
		this.def = null;
		this.clazz = null;
		this.function = function;
		this.clazzResult = null;
	}

	private void loadSetting() {
		final String path;
		if(configKey.isEmpty()) {
			path = section;
		} else {
			path = section + "." + configKey;
		}

		if (function == null) {
			loadRegular(path);
		} else {
			loadFunction(path);
		}
	}

	private void loadRegular(final @NotNull String path) {
		if (clazz == Boolean.class) {
			boolValue = config.getBoolean(path, (boolean) def);
		} else if (clazz == String.class) {
			stringValue = config.getString(path, (String) def);
		} else if (clazz == Integer.class) {
			intValue = config.getInt(path, (int) def);
		} else if (clazz == Double.class) {
			doubleValue = config.getDouble(path, (double) def);
		} else if (clazz == List.class) {
			stringList = config.getStringList(path);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadFunction(final @NotNull String path) {
		if (clazzResult == Set.class) {
			stringSet = (Set<String>) function.apply(config.getStringList(path));
		} else if (clazzResult == List.class) {
			stringList = (List<String>) function.apply(config.getStringList(path));
		} else if (clazzResult == null || clazzResult.isEnum()) { // Null check for some class that didn't exist in lower MC versions
			enumValue = (Enum<?>) function.apply(config.getString(path));
		} else if (clazzResult == Map.class) {
			map = (Map<Material, ItemCooldown>) function.apply(config.getConfigurationSection(path));
		} else if (clazzResult == String.class) {
			stringValue = (String) function.apply(config.getString(path));
		} else if (clazzResult == Boolean.class) {
			boolValue = (boolean) function.apply(config.getBoolean(path));
		}
	}

	public boolean asBool() {
		return boolValue;
	}

	@NotNull
	public String asString() {
		return stringValue;
	}

	public int asInt() {
		return intValue;
	}

	public double asDouble() {
		return doubleValue;
	}

	public List<String> asList() {
		return stringList;
	}

	public Set<String> asSet() {
		return stringSet;
	}

	@NotNull
	public <E extends Enum<E>> E asEnum(final Class<E> en) {
		return en.cast(enumValue);
	}

	public Map<Material, ItemCooldown> asMap() {
		return map;
	}

	public void set(final String stringValue) {
		this.stringValue = stringValue;
	}

	public void set(final boolean booleanValue) {
		this.boolValue = booleanValue;
	}

	public void disable() {
		if (clazz == Boolean.class) {
			boolValue = false;
		} else if (clazz == List.class) {
			stringList.clear();
		} else if (clazz == Double.class) {
			doubleValue = 0;
		}
	}

	public static void initialize(final PvPManager pl, final YamlConfiguration yamlConfig) {
		Conf.plugin = pl;
		Conf.config = yamlConfig;
		Stream.of(Conf.values()).forEach(Conf::loadSetting);
		Log.infoColor(ChatColor.GREEN + "Using player nametags: " + ChatColor.AQUA + (NAMETAG_COMBAT_ENABLED.asBool() || TOGGLE_NAMETAG_ENABLED.asBool()));
	}

	private static <R extends Enum<R>> R loadEnum(final Class<R> clazz, final String value) {
		try {
			return Enum.valueOf(clazz, value);
		} catch (final IllegalArgumentException e) {
			Log.warning("Error! " + clazz.getSimpleName() + " '" + value + "' does not exist!");
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private static <R extends Enum<R>> R loadEnum(final String className, final String value, final MCVersion minVersion) {
		if (MCVersion.isAtLeast(minVersion)) {
			try {
				return loadEnum((Class<R>) Class.forName(className), value);
			} catch (final ClassNotFoundException e) {
				Log.severe(e.getMessage(), e);
			}
		}
		return null;
	}

	public static ConfigurationSection getPluginHooks() {
		return plugin.getConfigM().getHooksConfig().getConfigurationSection(ConfSection.PLUGIN_HOOKS);
	}

	private static List<String> getList(final List<String> list) {
		list.removeIf(string -> string.startsWith("example"));
		return list;
	}

	private static Set<String> getSet(final List<String> list) {
		return new HashSet<>(getList(list));
	}

	private static List<String> getCommandList(final List<String> list) {
		for (final Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
			String string = iterator.next().toLowerCase();
			if (string.startsWith("!console")) {
				string = string.substring(9);
			}
			if (string.startsWith("!player")) {
				string = string.substring(8);
			}
			if (string.isEmpty() || string.startsWith("example")) {
				iterator.remove();
			}
		}
		return list;
	}

	private static Map<Material, ItemCooldown> loadItemCooldowns(final ConfigurationSection section) {
		final EnumMap<Material, ItemCooldown> itemCooldowns = new EnumMap<>(Material.class);
		if (section == null)
			return Collections.emptyMap();

		final ConfigurationSection combat = section.getConfigurationSection("Combat");
		final Map<Material, Integer> combatCooldowns = readItemCooldowns(combat);

		final ConfigurationSection global = section.getConfigurationSection("Global");
		final Map<Material, Integer> globalCooldowns = readItemCooldowns(global);

		final Set<Material> materials = new HashSet<>(combatCooldowns.keySet());
		materials.addAll(globalCooldowns.keySet());

		for (final Material material : materials) {
			itemCooldowns.put(material, new ItemCooldown(combatCooldowns.getOrDefault(material, 0), globalCooldowns.getOrDefault(material, 0)));
		}
		return itemCooldowns;
	}

	private static Map<Material, Integer> readItemCooldowns(final ConfigurationSection cooldownSection) {
		if (cooldownSection == null)
			return Collections.emptyMap();
		final Map<Material, Integer> results = new HashMap<>();
		for (final Entry<String, Object> e : cooldownSection.getValues(false).entrySet()) {
			final Material material = Material.getMaterial(e.getKey().toUpperCase());
			if (material == null) {
				Log.warning("The material " + e.getKey()
						+ " in Item Cooldowns doesn't exist. You might have typed it incorrectly or it might not exist in this MC version");
				continue;
			}
			results.put(material, (int) e.getValue());
		}
		return results;
	}

	public static boolean setDebug(final Boolean debug) {
		Log.setDebug(debug);
		Conf.DEBUG_MODE.set(debug);
		return debug;
	}

}
