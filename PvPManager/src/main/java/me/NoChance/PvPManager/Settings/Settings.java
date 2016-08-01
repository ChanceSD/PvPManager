package me.NoChance.PvPManager.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class Settings {

	public enum DropMode {
		ALWAYS, DROP, KEEP, TRANSFER
	}

	private static int configVersion;
	private static boolean autoUpdate;
	private static boolean blockEnderPearl;
	private static boolean blockTeleport;
	private static boolean blockPickNewbies;
	private static boolean blockPlaceBlocks;
	private static List<String> commandsAllowed = Collections.singletonList("tag");
	private static List<String> commandsOnKill = new ArrayList<>();
	private static List<String> commandsOnPvPLog = new ArrayList<>();
	private static List<String> commandsPvPOff = new ArrayList<>();
	private static List<String> commandsPvPOn = new ArrayList<>();
	private static boolean commandsWhitelist;
	private static boolean defaultPvp;
	private static boolean disableDisguise;
	private static boolean disableFly;
	private static boolean disableGamemode;
	private static boolean disableInvisibility;
	private static boolean disableGodMode;
	private static boolean dropArmor;
	private static boolean dropExp;
	private static boolean dropInventory;
	private static DropMode dropMode;
	private static double fineAmount;
	private static boolean ignoreNoDamageHits;
	private static boolean inCombatEnabled;
	private static boolean useNameTag;
	private static List<String> killAbuseCommands = Collections.singletonList("kick <player> Kill Abuse Is Not Allowed!");
	private static boolean killAbuseEnabled;
	private static int killAbuseMaxKills;
	private static int killAbuseTime;
	private static boolean killOnLogout;
	private static String locale;
	private static boolean logToFile;
	private static double moneyPenalty;
	private static double moneyReward;
	private static String nameTagPrefix;
	private static boolean newbieGodMode;
	private static boolean newbieProtectionEnabled;
	private static int newbieProtectionTime;
	private static boolean punishOnKick;
	private static boolean pvpBlood;
	private static int respawnProtection;
	private static boolean stopCommands;
	private static int timeInCombat;
	private static String toggleColorOff;
	private static String toggleColorOn;
	private static int toggleCooldown;
	private static boolean toggleNametagsEnabled;
	private static boolean forcePvPOnWorldChange;
	private static boolean update = false;
	private static boolean updateCheck;
	private static String updateLocation;
	private static boolean optOutMetrics;
	private static double soupHealth;
	private static boolean borderHoppingVulnerable;
	private static boolean borderHoppingPushback;
	private static boolean worldguardOverrides;
	private static List<String> newbieBlacklist;
	private static List<String> worldsExcluded = Arrays.asList("Example", "Example2");
	private static ConfigurationSection GENERAL;
	private static ConfigurationSection BORDERHOPPING;
	private static ConfigurationSection DISABLE;
	private static ConfigurationSection TAGGEDCOMBAT;
	private static ConfigurationSection NEWBIEPROTECTION;
	private static ConfigurationSection KILLABUSE;
	private static ConfigurationSection PLAYERKILLS;
	private static ConfigurationSection PVPTOGGLE;
	private static ConfigurationSection UPDATECHECK;

	private Settings() {
	}

	private static void assignSections(final Config config) {
		GENERAL = config.getConfigurationSection("General");
		BORDERHOPPING = config.getConfigurationSection("Anti Border Hopping");
		DISABLE = config.getConfigurationSection("Disable");
		TAGGEDCOMBAT = config.getConfigurationSection("Tagged In Combat");
		NEWBIEPROTECTION = config.getConfigurationSection("Newbie Protection");
		KILLABUSE = config.getConfigurationSection("Kill Abuse");
		PLAYERKILLS = config.getConfigurationSection("Player Kills");
		PVPTOGGLE = config.getConfigurationSection("PvP Toggle");
		UPDATECHECK = config.getConfigurationSection("Update Check");
	}

	@SuppressWarnings("unchecked")
	public static void initizalizeVariables(final Config c) {
		assignSections(c);

		locale = GENERAL.getString("Locale", "en").toUpperCase();
		defaultPvp = GENERAL.getBoolean("Default PvP", true);
		pvpBlood = GENERAL.getBoolean("PvP Blood", true);
		dropMode = DropMode.valueOf(GENERAL.getString("Player Drop Mode", "ALWAYS").toUpperCase());
		ignoreNoDamageHits = GENERAL.getBoolean("Ignore No Damage Hits", false);
		soupHealth = GENERAL.getDouble("Auto Soup Health", 0);
		worldsExcluded = (List<String>) GENERAL.getList("World Exclusions", worldsExcluded);

		borderHoppingVulnerable = BORDERHOPPING.getBoolean("Vulnerable", true);
		borderHoppingPushback = BORDERHOPPING.getBoolean("Push Back", true);

		disableFly = DISABLE.getBoolean("Fly", true);
		disableGamemode = DISABLE.getBoolean("GameMode", true);
		disableDisguise = DISABLE.getBoolean("Disguise", true);
		disableGodMode = DISABLE.getBoolean("GodMode", true);
		disableInvisibility = DISABLE.getBoolean("Invisibility", false);

		inCombatEnabled = TAGGEDCOMBAT.getBoolean("Enabled", true);
		timeInCombat = TAGGEDCOMBAT.getInt("Time", 10);
		nameTagPrefix = TAGGEDCOMBAT.getString("NameTag Prefix", "&c");
		useNameTag = nameTagPrefix.equalsIgnoreCase("none") || nameTagPrefix.isEmpty() ? false : true;
		blockEnderPearl = TAGGEDCOMBAT.getBoolean("Block.EnderPearls", true);
		blockTeleport = TAGGEDCOMBAT.getBoolean("Block.Teleport", true);
		blockPlaceBlocks = TAGGEDCOMBAT.getBoolean("Block.Place Blocks", false);
		stopCommands = TAGGEDCOMBAT.getBoolean("Block.Commands.Enabled", true);
		commandsWhitelist = TAGGEDCOMBAT.getBoolean("Block.Commands.Whitelist", true);
		commandsAllowed = (List<String>) TAGGEDCOMBAT.getList("Block.Commands.Command List", commandsAllowed);
		punishOnKick = TAGGEDCOMBAT.getBoolean("Punishments.Punish On Kick", true);
		fineAmount = TAGGEDCOMBAT.getDouble("Punishments.Money Penalty", 10.00);
		logToFile = TAGGEDCOMBAT.getBoolean("Punishments.Log To File", true);
		killOnLogout = TAGGEDCOMBAT.getBoolean("Punishments.Kill on Logout.Enabled", true);
		dropInventory = TAGGEDCOMBAT.getBoolean("Punishments.Kill on Logout.Player Drops.Inventory", true);
		dropExp = TAGGEDCOMBAT.getBoolean("Punishments.Kill on Logout.Player Drops.Experience", true);
		dropArmor = TAGGEDCOMBAT.getBoolean("Punishments.Kill on Logout.Player Drops.Armor", true);
		commandsOnPvPLog = (List<String>) TAGGEDCOMBAT.getList("Punishments.Commands On PvPLog", new ArrayList<>());

		newbieProtectionEnabled = NEWBIEPROTECTION.getBoolean("Enabled", true);
		newbieProtectionTime = NEWBIEPROTECTION.getInt("Time(minutes)", 5);
		blockPickNewbies = NEWBIEPROTECTION.getBoolean("Block Pick Items", false);
		newbieGodMode = NEWBIEPROTECTION.getBoolean("Protect From Everything", false);
		newbieBlacklist = (List<String>) NEWBIEPROTECTION.getList("Command Blacklist", new ArrayList<>());

		killAbuseEnabled = KILLABUSE.getBoolean("Enabled", true);
		killAbuseMaxKills = KILLABUSE.getInt("Max Kills", 5);
		killAbuseTime = KILLABUSE.getInt("Time Limit", 60);
		killAbuseCommands = (List<String>) KILLABUSE.getList("Commands on Abuse", killAbuseCommands);
		respawnProtection = KILLABUSE.getInt("Respawn Protection", 5);

		setMoneyReward(PLAYERKILLS.getDouble("Money Reward", 10));
		setMoneyPenalty(PLAYERKILLS.getDouble("Money Penalty", 10));
		commandsOnKill = (List<String>) PLAYERKILLS.getList("Commands On Kill", commandsOnKill);

		toggleCooldown = PVPTOGGLE.getInt("Cooldown", 15);
		setToggleNametagsEnabled(PVPTOGGLE.getBoolean("NameTags.Enabled", false));
		toggleColorOn = PVPTOGGLE.getString("NameTags.Prefix On", "&1");
		toggleColorOff = PVPTOGGLE.getString("NameTags.Prefix Off", "&2");
		commandsPvPOn = (List<String>) PVPTOGGLE.getList("Commands PvP On", new ArrayList<>());
		commandsPvPOff = (List<String>) PVPTOGGLE.getList("Commands PvP Off", new ArrayList<>());
		forcePvPOnWorldChange = PVPTOGGLE.getBoolean("Force On Change World", false);
		worldguardOverrides = PVPTOGGLE.getBoolean("WorldGuard Overrides", true);

		updateCheck = UPDATECHECK.getBoolean("Enabled", true);
		updateLocation = UPDATECHECK.getString("Update Location", "Bukkit");
		autoUpdate = UPDATECHECK.getBoolean("Auto Update", true);

		optOutMetrics = c.getBoolean("Metrics.Opt-out", false);
		configVersion = c.getInt("Config Version");

	}

	public static void updateDefaultConfig(final Config config, final int newVersion) {
		config.set("General.Default PvP", Settings.isDefaultPvp());
		config.set("General.PvP Blood", Settings.isPvpBlood());
		config.set("General.Ignore No Damage Hits", Settings.isIgnoreNoDamageHits());
		config.set("General.Auto Soup Health", Settings.getSoupHealth());

		config.set("Anti Border Hopping.Vulnerable", Settings.borderHoppingVulnerable());
		config.set("Anti Border Hopping.Push Back", Settings.borderHoppingPushback());

		config.set("Disable.Fly", Settings.isDisableFly());
		config.set("Disable.GameMode", Settings.isDisableGamemode());
		config.set("Disable.Disguise", Settings.isDisableDisguise());
		config.set("Disable.Invisibility", Settings.isDisableInvisibility());

		config.set("Tagged In Combat.Enabled", Settings.isInCombatEnabled());
		config.set("Tagged In Combat.Time", Settings.getTimeInCombat());
		config.set("Tagged In Combat.NameTag Prefix", Settings.getNameTagColor());
		config.set("Tagged In Combat.Block.EnderPearls", Settings.isBlockEnderPearl());
		config.set("Tagged In Combat.Block.Teleport", Settings.isBlockEnderPearl());
		config.set("Tagged In Combat.Block.Place Blocks", Settings.isBlockPlaceBlocks());
		config.set("Tagged In Combat.Block.Commands.Enabled", Settings.isStopCommands());
		config.set("Tagged In Combat.Block.Commands.Whitelist", Settings.isCommandsWhitelist());
		config.set("Tagged In Combat.Block.Commands.Command List", Settings.getCommandsAllowed());
		config.set("Tagged In Combat.Punishments.Punish On Kick", Settings.punishOnKick());
		config.set("Tagged In Combat.Punishments.Commands On PvPLog", Settings.getCommandsOnPvPLog());
		config.set("Tagged In Combat.Punishments.Log To File", Settings.isLogToFile());
		config.set("Tagged In Combat.Punishments.Kill on Logout.Enabled", Settings.isKillOnLogout());
		config.set("Tagged In Combat.Punishments.Kill on Logout.Player Drops.Inventory", Settings.isDropInventory());
		config.set("Tagged In Combat.Punishments.Kill on Logout.Player Drops.Experience", Settings.isDropExp());
		config.set("Tagged In Combat.Punishments.Kill on Logout.Player Drops.Armor", Settings.isDropArmor());
		config.set("Tagged In Combat.Punishments.Money Penalty", Settings.getFineAmount());

		config.set("Player Kills.Money Reward", Settings.getMoneyReward());
		config.set("Player Kills.Money Penalty", Settings.getMoneyPenalty());
		config.set("Player Kills.Commands On Kill", Settings.getCommandsOnKill());

		config.set("PvP Toggle.Cooldown", Settings.getToggleCooldown());
		config.set("PvP Toggle.NameTags.Enabled", Settings.isToggleNametagsEnabled());
		config.set("PvP Toggle.NameTags.Prefix On", Settings.getToggleColorOn());
		config.set("PvP Toggle.NameTags.Prefix Off", Settings.getToggleColorOff());
		config.set("PvP Toggle.Commands PvP On", Settings.getCommandsPvPOn());
		config.set("PvP Toggle.Commands PvP Off", Settings.getCommandsPvPOff());
		config.set("PvP Toggle.WorldGuard Overrides", Settings.isWorldguardOverrides());

		config.set("Kill Abuse.Enabled", Settings.isKillAbuseEnabled());
		config.set("Kill Abuse.Max Kills", Settings.getKillAbuseMaxKills());
		config.set("Kill Abuse.Time Limit", Settings.getKillAbuseTime());
		config.set("Kill Abuse.Commands on Abuse", Settings.getKillAbuseCommands());
		config.set("Kill Abuse.Respawn Protection", Settings.getRespawnProtection());

		config.set("Newbie Protection.Enabled", Settings.isNewbieProtectionEnabled());
		config.set("Newbie Protection.Time(minutes)", Settings.getNewbieProtectionTime());
		config.set("Newbie Protection.Block Pick Items", Settings.isBlockPickNewbies());
		config.set("Newbie Protection.Protect From Everything", Settings.isNewbieGodMode());
		config.set("Newbie Protection.Command Blacklist", Settings.getNewbieBlacklist());

		config.set("Config Version", newVersion);
		config.set("Update Check.Enabled", Settings.isUpdateCheck());
		config.set("Update Check.Update Location", Settings.getUpdateLocation());
		config.set("Update Check.Auto Update", Settings.isUpdate());
		config.saveConfig();
		initizalizeVariables(config);
	}

	public static void helpMenu(final Player player) {
		player.sendMessage(ChatColor.GOLD + "-------------- PvPManager Help Page --------------");
		player.sendMessage(ChatColor.GOLD + "/pvp [player] " + ChatColor.WHITE + "| Set PvP Enabled or Disabled");
		player.sendMessage(ChatColor.GOLD + "/pvpinfo [player] " + ChatColor.WHITE + "| Check your or other player info");
		player.sendMessage(ChatColor.GOLD + "/pvplist " + ChatColor.WHITE + "| List all players with PvP enabled");
		player.sendMessage(ChatColor.GOLD + "/pvpo " + ChatColor.WHITE + "| Override all PvP protections");
		player.sendMessage(ChatColor.GOLD + "/pvpstatus [player] " + ChatColor.WHITE + "| Check yours or other player PvP status");
		player.sendMessage(ChatColor.GOLD + "/pvp disable " + ChatColor.WHITE + "| Disable Newbie Protection");
		player.sendMessage(ChatColor.GOLD + "/pm " + ChatColor.WHITE + "| Show This Help Page");
		player.sendMessage(ChatColor.GOLD + "/pm update " + ChatColor.WHITE + "| Update to Latest Version");
		player.sendMessage(ChatColor.GOLD + "/pm reload " + ChatColor.WHITE + "| Reload PvPManager");
		player.sendMessage(ChatColor.GOLD + "-------------------------------------------------");
	}

	public static List<String> getNewbieBlacklist() {
		return newbieBlacklist;
	}

	public static List<String> getCommandsAllowed() {
		return commandsAllowed;
	}

	public static List<String> getCommandsOnKill() {
		return commandsOnKill;
	}

	public static List<String> getCommandsOnPvPLog() {
		return commandsOnPvPLog;
	}

	public static List<String> getCommandsPvPOff() {
		return commandsPvPOff;
	}

	public static List<String> getCommandsPvPOn() {
		return commandsPvPOn;
	}

	public static DropMode getDropMode() {
		return dropMode;
	}

	public static double getFineAmount() {
		return fineAmount;
	}

	public static void setFineAmount(final double fineAmount) {
		Settings.fineAmount = fineAmount;
	}

	public static List<String> getKillAbuseCommands() {
		return killAbuseCommands;
	}

	public static int getKillAbuseMaxKills() {
		return killAbuseMaxKills;
	}

	public static int getKillAbuseTime() {
		return killAbuseTime;
	}

	public static String getLocale() {
		return locale;
	}

	public static double getMoneyPenalty() {
		return moneyPenalty;
	}

	public static double getMoneyReward() {
		return moneyReward;
	}

	public static String getNameTagColor() {
		return nameTagPrefix;
	}

	public static int getNewbieProtectionTime() {
		return newbieProtectionTime;
	}

	public static int getRespawnProtection() {
		return respawnProtection;
	}

	public static int getTimeInCombat() {
		return timeInCombat;
	}

	public static String getToggleColorOff() {
		return toggleColorOff;
	}

	public static String getToggleColorOn() {
		return toggleColorOn;
	}

	public static int getToggleCooldown() {
		return toggleCooldown;
	}

	public static String getUpdateLocation() {
		return updateLocation;
	}

	public static List<String> getWorldsExcluded() {
		return worldsExcluded;
	}

	public static boolean isAutoUpdate() {
		return autoUpdate;
	}

	public static boolean isBlockEnderPearl() {
		return blockEnderPearl;
	}

	public static boolean isBlockPickNewbies() {
		return blockPickNewbies;
	}

	public static boolean isBlockPlaceBlocks() {
		return blockPlaceBlocks;
	}

	public static boolean isCommandsWhitelist() {
		return commandsWhitelist;
	}

	public static boolean isDefaultPvp() {
		return defaultPvp;
	}

	public static boolean isDisableDisguise() {
		return disableDisguise;
	}

	public static boolean isDisableFly() {
		return disableFly;
	}

	public static boolean isDisableGamemode() {
		return disableGamemode;
	}

	public static boolean isDisableInvisibility() {
		return disableInvisibility;
	}

	public static boolean isDropArmor() {
		return dropArmor;
	}

	public static boolean isDropExp() {
		return dropExp;
	}

	public static boolean isDropInventory() {
		return dropInventory;
	}

	public static boolean isIgnoreNoDamageHits() {
		return ignoreNoDamageHits;
	}

	public static boolean isInCombatEnabled() {
		return inCombatEnabled;
	}

	public static boolean isKillAbuseEnabled() {
		return killAbuseEnabled;
	}

	public static boolean isKillOnLogout() {
		return killOnLogout;
	}

	public static boolean isLogToFile() {
		return logToFile;
	}

	public static boolean isNewbieGodMode() {
		return newbieGodMode;
	}

	public static boolean isNewbieProtectionEnabled() {
		return newbieProtectionEnabled;
	}

	public static boolean isPvpBlood() {
		return pvpBlood;
	}

	public static boolean borderHoppingVulnerable() {
		return borderHoppingVulnerable;
	}

	public static boolean borderHoppingPushback() {
		return borderHoppingPushback;
	}

	public static boolean isStopCommands() {
		return stopCommands;
	}

	public static boolean isToggleNametagsEnabled() {
		return toggleNametagsEnabled;
	}

	public static boolean isUpdate() {
		return update;
	}

	public static boolean isUpdateCheck() {
		return updateCheck;
	}

	public static boolean isOptOutMetrics() {
		return optOutMetrics;
	}

	public static boolean punishOnKick() {
		return punishOnKick;
	}

	public static boolean isDisableGodMode() {
		return disableGodMode;
	}

	public static void setMoneyPenalty(final double moneyPenalty) {
		Settings.moneyPenalty = moneyPenalty;
	}

	public static void setMoneyReward(final double moneyReward) {
		Settings.moneyReward = moneyReward;
	}

	public static void setToggleNametagsEnabled(final boolean toggleNametagsEnabled) {
		Settings.toggleNametagsEnabled = toggleNametagsEnabled;
	}

	public static void setUpdate(final boolean update) {
		Settings.update = update;
	}

	public static boolean isUseNameTag() {
		return useNameTag;
	}

	public static void setUseNameTag(final boolean useNameTag) {
		Settings.useNameTag = useNameTag;
	}

	public static int getConfigVersion() {
		return configVersion;
	}

	public static boolean isAutoSoupEnabled() {
		return getSoupHealth() > 0;
	}

	public static double getSoupHealth() {
		return soupHealth;
	}

	public static boolean isBlockTeleport() {
		return blockTeleport;
	}

	public static boolean isForcePvPOnWorldChange() {
		return forcePvPOnWorldChange;
	}

	public static boolean isWorldguardOverrides() {
		return worldguardOverrides;
	}

}
