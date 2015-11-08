package me.NoChance.PvPManager.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class Variables {

	public enum DropMode {
		ALWAYS, DROP, KEEP, TRANSFER
	}

	private static boolean autoUpdate;
	private static boolean blockEnderPearl;
	private static boolean blockPickNewbies;
	private static boolean blockPlaceBlocks;
	private static List<String> commandsAllowed = Collections.singletonList("tag");
	private static List<String> commandsOnKill = new ArrayList<>();
	private static List<String> commandsOnPvPLog = new ArrayList<>();
	private static List<String> commandsPvPOff = new ArrayList<>();
	private static List<String> commandsPvPOn = new ArrayList<>();
	private static boolean commandsWhitelist;
	private static boolean configUpdated;
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
	private static boolean stopBorderHopping;
	private static boolean stopCommands;
	private static int timeInCombat;
	private static String toggleColorOff;
	private static String toggleColorOn;
	private static int toggleCooldown;
	private static boolean toggleNametagsEnabled;
	private static boolean update = false;
	private static boolean updateCheck;
	private static String updateLocation;
	private static boolean optOutMetrics;
	private static double soupHealth;
	private static List<String> worldsExcluded = Arrays.asList("Example", "Example2");
	private static ConfigurationSection GENERAL;
	private static ConfigurationSection DISABLE;
	private static ConfigurationSection TAGGEDCOMBAT;
	private static ConfigurationSection NEWBIEPROTECTION;
	private static ConfigurationSection KILLABUSE;
	private static ConfigurationSection PLAYERKILLS;
	private static ConfigurationSection PVPTOGGLE;
	private static ConfigurationSection UPDATECHECK;

	private Variables() {
	}

	private static void assignSections(final Config config) {
		GENERAL = config.getConfigurationSection("General");
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
		stopBorderHopping = GENERAL.getBoolean("Stop Border Hopping", true);
		soupHealth = GENERAL.getDouble("Auto Soup Health", 0);
		worldsExcluded = (List<String>) GENERAL.getList("World Exclusions", worldsExcluded);

		disableFly = DISABLE.getBoolean("Fly", true);
		disableGamemode = DISABLE.getBoolean("GameMode", true);
		disableDisguise = DISABLE.getBoolean("Disguise", true);
		disableGodMode = DISABLE.getBoolean("GodMode", true);
		disableInvisibility = DISABLE.getBoolean("Invisibility", false);

		inCombatEnabled = TAGGEDCOMBAT.getBoolean("Enabled", true);
		timeInCombat = TAGGEDCOMBAT.getInt("Time", 10);
		nameTagPrefix = TAGGEDCOMBAT.getString("NameTag Prefix", "&c");
		useNameTag = nameTagPrefix == "none" || nameTagPrefix.isEmpty() ? false : true;
		blockEnderPearl = TAGGEDCOMBAT.getBoolean("Block.EnderPearls", true);
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

		updateCheck = UPDATECHECK.getBoolean("Enabled", true);
		updateLocation = UPDATECHECK.getString("Update Location", "Bukkit");
		autoUpdate = UPDATECHECK.getBoolean("Auto Update", true);

		optOutMetrics = config.getBoolean("Metrics.Opt-out", false);
	}

	public static void updateDefaultConfig(final Config config, final int newVersion) {
		config.set("General.Default PvP", Variables.isDefaultPvp());
		config.set("General.PvP Blood", Variables.isPvpBlood());
		config.set("General.Stop Border Hopping", Variables.isStopBorderHopping());
		config.set("General.Ignore No Damage Hits", Variables.isIgnoreNoDamageHits());
		config.set("General.Auto Soup Health", Variables.getSoupHealth());

		config.set("Disable.Fly", Variables.isDisableFly());
		config.set("Disable.GameMode", Variables.isDisableGamemode());
		config.set("Disable.Disguise", Variables.isDisableDisguise());
		config.set("Disable.Invisibility", Variables.isDisableInvisibility());

		config.set("Tagged In Combat.Enabled", Variables.isInCombatEnabled());
		config.set("Tagged In Combat.Time", Variables.getTimeInCombat());
		config.set("Tagged In Combat.NameTag Prefix", Variables.getNameTagColor());
		config.set("Tagged In Combat.Block.EnderPearls", Variables.isBlockEnderPearl());
		config.set("Tagged In Combat.Block.Place Blocks", Variables.isBlockPlaceBlocks());
		config.set("Tagged In Combat.Block.Commands.Enabled", Variables.isStopCommands());
		config.set("Tagged In Combat.Block.Commands.Whitelist", Variables.isCommandsWhitelist());
		config.set("Tagged In Combat.Block.Commands.Command List", Variables.getCommandsAllowed());
		config.set("Tagged In Combat.Punishments.Punish On Kick", Variables.punishOnKick());
		config.set("Tagged In Combat.Punishments.Commands On PvPLog", Variables.getCommandsOnPvPLog());
		config.set("Tagged In Combat.Punishments.Log To File", Variables.isLogToFile());
		config.set("Tagged In Combat.Punishments.Kill on Logout.Enabled", Variables.isKillOnLogout());
		config.set("Tagged In Combat.Punishments.Kill on Logout.Player Drops.Inventory", Variables.isDropInventory());
		config.set("Tagged In Combat.Punishments.Kill on Logout.Player Drops.Experience", Variables.isDropExp());
		config.set("Tagged In Combat.Punishments.Kill on Logout.Player Drops.Armor", Variables.isDropArmor());
		config.set("Tagged In Combat.Punishments.Money Penalty", Variables.getFineAmount());

		config.set("Player Kills.Money Reward", Variables.getMoneyReward());
		config.set("Player Kills.Money Penalty", Variables.getMoneyPenalty());
		config.set("Player Kills.Commands On Kill", Variables.getCommandsOnKill());

		config.set("PvP Toggle.Cooldown", Variables.getToggleCooldown());
		config.set("PvP Toggle.NameTags.Enabled", Variables.isToggleNametagsEnabled());
		config.set("PvP Toggle.NameTags.Prefix On", Variables.getToggleColorOn());
		config.set("PvP Toggle.NameTags.Prefix Off", Variables.getToggleColorOff());
		config.set("PvP Toggle.Commands PvP On", Variables.getCommandsPvPOn());
		config.set("PvP Toggle.Commands PvP Off", Variables.getCommandsPvPOff());

		config.set("Kill Abuse.Enabled", Variables.isKillAbuseEnabled());
		config.set("Kill Abuse.Max Kills", Variables.getKillAbuseMaxKills());
		config.set("Kill Abuse.Time Limit", Variables.getKillAbuseTime());
		config.set("Kill Abuse.Commands on Abuse", Variables.getKillAbuseCommands());
		config.set("Kill Abuse.Respawn Protection", Variables.getRespawnProtection());

		config.set("Newbie Protection.Enabled", Variables.isNewbieProtectionEnabled());
		config.set("Newbie Protection.Time(minutes)", Variables.getNewbieProtectionTime());
		config.set("Newbie Protection.Block Pick Items", Variables.isBlockPickNewbies());
		config.set("Newbie Protection.Protect From Everything", Variables.isNewbieGodMode());

		config.set("Config Version", newVersion);
		config.set("Update Check.Enabled", Variables.isUpdateCheck());
		config.set("Update Check.Update Location", Variables.getUpdateLocation());
		config.set("Update Check.Auto Update", Variables.isUpdate());
		config.saveConfig();
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
		Variables.fineAmount = fineAmount;
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

	public static boolean isConfigUpdated() {
		return configUpdated;
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

	public static boolean isStopBorderHopping() {
		return stopBorderHopping;
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

	public static void setConfigUpdated(final boolean configUpdated) {
		Variables.configUpdated = configUpdated;
	}

	public static void setMoneyPenalty(final double moneyPenalty) {
		Variables.moneyPenalty = moneyPenalty;
	}

	public static void setMoneyReward(final double moneyReward) {
		Variables.moneyReward = moneyReward;
	}

	public static void setToggleNametagsEnabled(final boolean toggleNametagsEnabled) {
		Variables.toggleNametagsEnabled = toggleNametagsEnabled;
	}

	public static void setUpdate(final boolean update) {
		Variables.update = update;
	}

	public static boolean isUseNameTag() {
		return useNameTag;
	}

	public static void setUseNameTag(final boolean useNameTag) {
		Variables.useNameTag = useNameTag;
	}

	public static boolean isAutoSoupEnabled() {
		return getSoupHealth() > 0;
	}

	public static double getSoupHealth() {
		return soupHealth;
	}

}
