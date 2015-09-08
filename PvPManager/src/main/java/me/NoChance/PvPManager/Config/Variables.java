package me.NoChance.PvPManager.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.NoChance.PvPManager.Managers.ConfigManager;

public final class Variables {

	public enum DropMode {
		ALWAYS, DROP, KEEP, TRANSFER
	}

	private static boolean autoUpdate;
	private static boolean blockEnderPearl;
	private static boolean blockPickNewbies;
	private static boolean blockPlaceBlocks;
	private static ConfigManager cm;
	private static List<String> commandsAllowed = Collections.singletonList("tag");
	private static List<String> commandsOnKill = Collections.singletonList("heal <player>");
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
	private static boolean dropArmor;
	private static boolean dropExp;
	private static boolean dropInventory;
	private static DropMode dropMode;
	private static double fineAmount;
	private static boolean fineEnabled;
	private static boolean ignoreNoDamageHits;
	private static boolean inCombatEnabled;
	private static boolean inCombatSilent;
	private static List<String> killAbuseCommands = Collections.singletonList("kick <player> Kill Abuse Is Not Allowed!");
	private static boolean killAbuseEnabled;
	private static int killAbuseMaxKills;
	private static int killAbuseTime;
	private static boolean killOnLogout;
	private static String locale;
	private static boolean logToFile;
	private static double moneyPenalty;
	private static double moneyReward;
	private static String nameTagColor;
	private static boolean newbieGodMode;
	private static boolean newbieProtectionEnabled;
	private static int newbieProtectionTime;
	private static boolean punishmentsEnabled;
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
	private static List<String> worldsExcluded = Arrays.asList("Example", "Example2");
	private static ConfigurationSection GENERAL;
	private static ConfigurationSection DISABLE;
	private static ConfigurationSection TAGGEDCOMBAT;
	private static ConfigurationSection NEWBIEPROTECTION;
	private static ConfigurationSection KILLABUSE;
	private static ConfigurationSection PLAYERKILLS;
	private static ConfigurationSection PVPTOGGLE;
	private static boolean disableGodMode;

	private Variables() {
	}

	private static boolean getBoolean(final String message, final boolean defaultValue) {
		return cm.getConfig().getBoolean(message, defaultValue);
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
		return nameTagColor;
	}

	public static int getNewbieProtectionTime() {
		return newbieProtectionTime;
	}

	public static int getRespawnProtection() {
		return respawnProtection;
	}

	private static String getString(final String message, final String defaultValue) {
		return cm.getConfig().getString(message, defaultValue);
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

	public static void assignSections() {
		GENERAL = cm.getConfig().getConfigurationSection("General.");
		DISABLE = cm.getConfig().getConfigurationSection("Disable.");
		TAGGEDCOMBAT = cm.getConfig().getConfigurationSection("Tagged ");
		NEWBIEPROTECTION = cm.getConfig().getConfigurationSection("Newbie Protection.");
		KILLABUSE = cm.getConfig().getConfigurationSection("Kill Abuse.");
		PLAYERKILLS = cm.getConfig().getConfigurationSection("Player Kills.");
		PVPTOGGLE = cm.getConfig().getConfigurationSection("PvP Toggle.");
	}

	@SuppressWarnings("unchecked")
	public static void initizalizeVariables(final ConfigManager configManager) {
		cm = configManager;
		assignSections();

		locale = GENERAL.getString("Locale", "en").toUpperCase();
		defaultPvp = GENERAL.getBoolean("Default PvP", true);
		pvpBlood = GENERAL.getBoolean("PvP Blood", true);
		dropMode = DropMode.valueOf(GENERAL.getString("Player Drop Mode", "ALWAYS").toUpperCase());
		ignoreNoDamageHits = GENERAL.getBoolean("Ignore No Damage Hits", false);
		stopBorderHopping = GENERAL.getBoolean("Stop Border Hopping", true);
		worldsExcluded = (List<String>) GENERAL.getList("World Exclusions", worldsExcluded);

		disableFly = DISABLE.getBoolean("Fly", true);
		disableGamemode = DISABLE.getBoolean("GameMode", true);
		disableDisguise = DISABLE.getBoolean("Disguise", true);
		disableGodMode = DISABLE.getBoolean("GodMode", true);
		disableInvisibility = DISABLE.getBoolean("Invisibility", false);

		inCombatEnabled = TAGGEDCOMBAT.getBoolean("Enabled", true);
		timeInCombat = TAGGEDCOMBAT.getInt("Time", 10);
		nameTagColor = TAGGEDCOMBAT.getString("NameTag Prefix", "&c");
		blockEnderPearl = TAGGEDCOMBAT.getBoolean("Block.EnderPearls", true);
		blockPlaceBlocks = TAGGEDCOMBAT.getBoolean("Block.Place Blocks", false);
		stopCommands = TAGGEDCOMBAT.getBoolean("Stop Commands.Enabled", true);
		commandsWhitelist = getBoolean("Stop Commands.Whitelist", true);
		commandsAllowed = (List<String>) cm.getConfig().getList("Stop Commands.Commands", commandsAllowed);
		punishmentsEnabled = getBoolean("Punishments.Enabled", true);
		punishOnKick = getBoolean("Punishments.Punish On Kick", true);
		fineAmount = cm.getConfig().getDouble("Punishments.Fine.Amount", 10.00);
		logToFile = getBoolean("Punishments.Log To File", true);
		killOnLogout = getBoolean("Punishments.Kill on Logout.Enabled", true);
		dropInventory = getBoolean("Punishments.Kill on Logout.Drops.Inventory", true);
		dropExp = getBoolean("Punishments.Kill on Logout.Drops.Experience", true);
		dropArmor = getBoolean("Punishments.Kill on Logout.Drops.Armor", true);
		commandsOnPvPLog = (List<String>) cm.getConfig().getList("Punishments.Commands On PvPLog", new ArrayList<>());

		newbieProtectionEnabled = getBoolean("Newbie Protection.Enabled", true);
		newbieProtectionTime = cm.getConfig().getInt("Newbie Protection.Time(minutes)", 5);
		blockPickNewbies = getBoolean("Newbie Protection.Block Pick Items", false);
		newbieGodMode = getBoolean("Newbie Protection.Protect From Everything", false);

		killAbuseEnabled = getBoolean("Kill Abuse.Enabled", true);
		killAbuseMaxKills = cm.getConfig().getInt("Kill Abuse.Max Kills", 5);
		killAbuseTime = cm.getConfig().getInt("Kill Abuse.Time Limit", 60);
		killAbuseCommands = (List<String>) cm.getConfig().getList("Kill Abuse.Commands on Abuse", killAbuseCommands);
		respawnProtection = cm.getConfig().getInt("Kill Abuse.Respawn Protection", 5);

		setMoneyReward(cm.getConfig().getDouble("Player Kills.Money Reward", 10));
		setMoneyPenalty(cm.getConfig().getDouble("Player Kills.Money Penalty", 10));
		commandsOnKill = (List<String>) cm.getConfig().getList("Player Kills.Commands On Kill.Commands", commandsOnKill);

		toggleCooldown = cm.getConfig().getInt("PvP Toggle.Cooldown(seconds)", 15);
		setToggleNametagsEnabled(getBoolean("PvP Toggle.NameTags.Enabled", false));
		toggleColorOn = getString("PvP Toggle.NameTags.Color On", "&1");
		toggleColorOff = getString("PvP Toggle.NameTags.Color Off", "&2");
		commandsPvPOn = (List<String>) cm.getConfig().getList("PvP Toggle.NameTags.Commands PvP On", new ArrayList<>());
		commandsPvPOff = (List<String>) cm.getConfig().getList("PvP Toggle.NameTags.Commands PvP Off", new ArrayList<>());

		updateCheck = getBoolean("Update Check.Enabled", true);
		updateLocation = getString("Update Check.Update Location", "Bukkit");
		autoUpdate = getBoolean("Update Check.Auto Update", true);

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

	public static boolean isFineEnabled() {
		return fineEnabled;
	}

	public static boolean isIgnoreNoDamageHits() {
		return ignoreNoDamageHits;
	}

	public static boolean isInCombatEnabled() {
		return inCombatEnabled;
	}

	public static boolean isInCombatSilent() {
		return inCombatSilent;
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

	public static boolean isPunishmentsEnabled() {
		return punishmentsEnabled;
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

	public static boolean punishOnKick() {
		return punishOnKick;
	}

	public static void setConfigUpdated(final boolean configUpdated) {
		Variables.configUpdated = configUpdated;
	}

	public static void setFineEnabled(final boolean fineEnabled) {
		Variables.fineEnabled = fineEnabled;
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

}
