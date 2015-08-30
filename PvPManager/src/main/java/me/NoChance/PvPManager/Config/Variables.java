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
	private static boolean commandsOnKillEnabled;
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
	private static boolean useNameTag;
	private static List<String> worldsExcluded = Arrays.asList("Example", "Example2");
	private static ConfigurationSection GENERAL;
	private static ConfigurationSection DISABLE;
	private static ConfigurationSection TAGGEDCOMBAT;
	private static ConfigurationSection NEWBIEPROTECTION;
	private static ConfigurationSection KILLABUSE;
	private static ConfigurationSection PLAYERKILLS;
	private static ConfigurationSection PVPTOGGLE;

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
		TAGGEDCOMBAT = cm.getConfig().getConfigurationSection("Tagged In Combat.");
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
		dropMode = DropMode.valueOf(GENERAL.getString("Player Drops.Mode", "ALWAYS").toUpperCase());
		ignoreNoDamageHits = GENERAL.getBoolean("Ignore No Damage Hits", false);
		stopBorderHopping = GENERAL.getBoolean("Ignore Zones For Tagged", true);
		worldsExcluded = (List<String>) GENERAL.getList("World Exclusions", worldsExcluded);

		disableFly = DISABLE.getBoolean("Disable Fly", true);
		disableGamemode = DISABLE.getBoolean("Disable GameMode", true);
		disableDisguise = DISABLE.getBoolean("Disable Disguise", true);
		disableGodMode = DISABLE.getBoolean("Disable Fly", true);
		disableInvisibility = DISABLE.getBoolean("Disable Invisibility", false);

		inCombatEnabled = TAGGEDCOMBAT.getBoolean("In Combat.Enabled", true);
		timeInCombat = TAGGEDCOMBAT.getInt("In Combat.Time(seconds)", 10);
		nameTagColor = TAGGEDCOMBAT.getString("In Combat.Name Tag Color", "&c");
		blockEnderPearl = TAGGEDCOMBAT.getBoolean("In Combat.Block EnderPearl", true);
		blockPlaceBlocks = TAGGEDCOMBAT.getBoolean("In Combat.Block Place Blocks", false);
		stopCommands = TAGGEDCOMBAT.getBoolean("In Combat.Stop Commands.Enabled", true);

		punishmentsEnabled = getBoolean("In Combat.Punishments.Enabled", true);
		dropInventory = getBoolean("In Combat.Punishments.Kill on Logout.Drops.Inventory", true);
		dropExp = getBoolean("In Combat.Punishments.Kill on Logout.Drops.Experience", true);
		dropArmor = getBoolean("In Combat.Punishments.Kill on Logout.Drops.Armor", true);
		killOnLogout = getBoolean("In Combat.Punishments.Kill on Logout.Enabled", true);
		updateCheck = getBoolean("Update Check.Enabled", true);
		autoUpdate = getBoolean("Update Check.Auto Update", true);
		newbieProtectionEnabled = getBoolean("Newbie Protection.Enabled", true);
		newbieProtectionTime = cm.getConfig().getInt("Newbie Protection.Time(minutes)", 5);
		setFineEnabled(getBoolean("In Combat.Punishments.Fine.Enabled", false));
		fineAmount = cm.getConfig().getDouble("In Combat.Punishments.Fine.Amount", 10.00);
		commandsAllowed = (List<String>) cm.getConfig().getList("In Combat.Stop Commands.Commands", commandsAllowed);
		toggleCooldown = cm.getConfig().getInt("PvP Toggle.Cooldown(seconds)", 15);
		killAbuseMaxKills = cm.getConfig().getInt("Kill Abuse.Max Kills", 5);
		killAbuseTime = cm.getConfig().getInt("Kill Abuse.Time Limit", 60);
		killAbuseCommands = (List<String>) cm.getConfig().getList("Kill Abuse.Commands on Abuse", killAbuseCommands);
		killAbuseEnabled = getBoolean("Kill Abuse.Enabled", true);
		inCombatSilent = getBoolean("In Combat.Silent", false);

		setUseNameTag(!nameTagColor.equalsIgnoreCase("none"));
		locale = getString("Locale", "en").toUpperCase();
		respawnProtection = cm.getConfig().getInt("Kill Abuse.Respawn Protection", 5);
		setMoneyReward(cm.getConfig().getDouble("Player Kills.Money Reward", 10));
		commandsOnKillEnabled = getBoolean("Player Kills.Commands On Kill.Enabled", false);
		commandsOnKill = (List<String>) cm.getConfig().getList("Player Kills.Commands On Kill.Commands", commandsOnKill);
		setToggleNametagsEnabled(getBoolean("PvP Toggle.NameTags.Enabled", false));
		toggleColorOn = getString("PvP Toggle.NameTags.Color On", "&1");
		toggleColorOff = getString("PvP Toggle.NameTags.Color Off", "&2");
		logToFile = getBoolean("In Combat.Punishments.Log To File", true);
		setMoneyPenalty(cm.getConfig().getDouble("Player Kills.Money Penalty", 10));
		blockPickNewbies = getBoolean("Newbie Protection.Block Pick Items", false);
		newbieGodMode = getBoolean("Newbie Protection.Protect From Everything", false);
		commandsWhitelist = getBoolean("In Combat.Stop Commands.Whitelist", true);

		updateLocation = getString("Update Check.Update Location", "Bukkit");
		commandsOnPvPLog = (List<String>) cm.getConfig().getList("In Combat.Punishments.Commands On PvPLog", new ArrayList<>());
		commandsPvPOn = (List<String>) cm.getConfig().getList("PvP Toggle.NameTags.Commands PvP On", new ArrayList<>());
		commandsPvPOff = (List<String>) cm.getConfig().getList("PvP Toggle.NameTags.Commands PvP Off", new ArrayList<>());
		punishOnKick = getBoolean("In Combat.Punishments.Punish On Kick", true);
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

	public static boolean isCommandsOnKillEnabled() {
		return commandsOnKillEnabled;
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

	public static boolean isUseNameTag() {
		return useNameTag;
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

	public static void setUseNameTag(final boolean useNameTag) {
		Variables.useNameTag = useNameTag;
	}

}
