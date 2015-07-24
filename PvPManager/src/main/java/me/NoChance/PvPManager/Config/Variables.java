package me.NoChance.PvPManager.Config;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.NoChance.PvPManager.Managers.ConfigManager;

public final class Variables {

	public enum DropMode {
		ALWAYS, DROP, KEEP, TRANSFER
	}

	private static boolean inCombatEnabled;
	private static int timeInCombat;
	private static boolean stopCommands;
	private static boolean punishmentsEnabled;
	private static boolean dropInventory;
	private static boolean dropExp;
	private static boolean dropArmor;
	private static boolean killOnLogout;
	private static List<String> worldsExcluded = Arrays.asList(new String[] { "Example", "Example2" });
	private static boolean disableFly;
	private static boolean onlyTagAttacker;
	private static boolean updateCheck;
	private static boolean autoUpdate;
	private static boolean newbieProtectionEnabled;
	private static int newbieProtectionTime;
	private static boolean fineEnabled;
	private static double fineAmount;
	private static boolean pvpBlood;
	private static boolean disableGamemode;
	private static boolean update = false;
	private static List<String> commandsAllowed = Arrays.asList(new String[] { "tell", "tag" });
	private static int toggleCooldown;
	private static boolean defaultPvp;
	private static boolean disableDisguise;
	private static boolean killAbuseEnabled;
	private static int killAbuseMaxKills;
	private static int killAbuseTime;
	private static List<String> killAbuseCommands = Arrays.asList("kick <player> Kill Abuse Is Not Allowed!");
	private static boolean toggleBroadcast;
	private static boolean blockEnderPearl;
	private static boolean autoSoupEnabled;
	private static double soupHealth;
	private static boolean inCombatSilent;
	private static boolean configUpdated;
	private static boolean stopBorderHopping;
	private static boolean ignoreNoDamageHits;
	private static String nameTagColor;
	private static boolean useNameTag;
	private static String locale;
	private static int respawnProtection;
	private static double moneyReward;
	private static boolean commandsOnKillEnabled;
	private static List<String> commandsOnKill = Arrays.asList("heal <player>");
	private static boolean disableInvisibility;
	private static boolean toggleNametagsEnabled;
	private static String toggleColorOn;
	private static String toggleColorOff;
	private static boolean logToFile;
	private static double moneyPenalty;
	private static boolean blockPickNewbies;
	private static boolean newbieGodMode;
	private static boolean commandsWhitelist;
	private static boolean blockPlaceBlocks;
	private static DropMode dropMode;
	private static String updateLocation;
	private static ConfigManager cm;
	private static List<String> commandsOnPvPLog = Arrays.asList("say &6[&8PvPManager&6] %p tried to escape combat and got punished!");

	private Variables() {
	}

	@SuppressWarnings("unchecked")
	public static void initizalizeVariables(final ConfigManager configManager) {
		cm = configManager;
		inCombatEnabled = getBoolean("In Combat.Enabled", true);
		timeInCombat = cm.getConfig().getInt("In Combat.Time(seconds)", 10);
		blockEnderPearl = getBoolean("In Combat.Block EnderPearl", true);
		stopCommands = getBoolean("In Combat.Stop Commands.Enabled", true);
		punishmentsEnabled = getBoolean("In Combat.Punishments.Enabled", true);
		dropInventory = getBoolean("In Combat.Punishments.Kill on Logout.Drops.Inventory", true);
		dropExp = getBoolean("In Combat.Punishments.Kill on Logout.Drops.Experience", true);
		dropArmor = getBoolean("In Combat.Punishments.Kill on Logout.Drops.Armor", true);
		killOnLogout = getBoolean("In Combat.Punishments.Kill on Logout.Enabled", true);
		worldsExcluded = (List<String>) cm.getConfig().getList("World Exclusions", worldsExcluded);
		disableFly = getBoolean("Disable Fly", true);
		onlyTagAttacker = getBoolean("In Combat.Only Tag Attacker", false);
		updateCheck = getBoolean("Update Check.Enabled", true);
		autoUpdate = getBoolean("Update Check.Auto Update", true);
		newbieProtectionEnabled = getBoolean("Newbie Protection.Enabled", true);
		newbieProtectionTime = cm.getConfig().getInt("Newbie Protection.Time(minutes)", 5);
		setFineEnabled(getBoolean("In Combat.Punishments.Fine.Enabled", false));
		fineAmount = cm.getConfig().getDouble("In Combat.Punishments.Fine.Amount", 10.00);
		pvpBlood = getBoolean("PvP Blood", true);
		disableGamemode = getBoolean("Disable GameMode", true);
		commandsAllowed = (List<String>) cm.getConfig().getList("In Combat.Stop Commands.Commands", commandsAllowed);
		toggleCooldown = cm.getConfig().getInt("PvP Toggle.Cooldown(seconds)", 15);
		toggleBroadcast = getBoolean("PvP Toggle.Broadcast", false);
		defaultPvp = getBoolean("Default PvP", true);
		disableDisguise = getBoolean("Disable Disguise", true);
		killAbuseMaxKills = cm.getConfig().getInt("Kill Abuse.Max Kills", 5);
		killAbuseTime = cm.getConfig().getInt("Kill Abuse.Time Limit", 60);
		killAbuseCommands = (List<String>) cm.getConfig().getList("Kill Abuse.Commands on Abuse", killAbuseCommands);
		killAbuseEnabled = getBoolean("Kill Abuse.Enabled", true);
		autoSoupEnabled = getBoolean("Auto Soup.Enabled", false);
		soupHealth = cm.getConfig().getDouble("Auto Soup.Health Gain", 6);
		inCombatSilent = getBoolean("In Combat.Silent", false);
		stopBorderHopping = getBoolean("Ignore Zones For Tagged", true);
		nameTagColor = getString("In Combat.Name Tag Color", "&c");
		setUseNameTag(nameTagColor.equalsIgnoreCase("none") ? false : true);
		locale = getString("Locale", "en").toUpperCase();
		ignoreNoDamageHits = getBoolean("Ignore No Damage Hits", false);
		respawnProtection = cm.getConfig().getInt("Kill Abuse.Respawn Protection", 5);
		setMoneyReward(cm.getConfig().getDouble("Player Kills.Money Reward", 10));
		commandsOnKillEnabled = getBoolean("Player Kills.Commands On Kill.Enabled", false);
		commandsOnKill = (List<String>) cm.getConfig().getList("Player Kills.Commands On Kill.Commands", commandsOnKill);
		disableInvisibility = getBoolean("Disable Invisibility", false);
		setToggleNametagsEnabled(getBoolean("PvP Toggle.NameTags.Enabled", false));
		toggleColorOn = getString("PvP Toggle.NameTags.Color On", "&1");
		toggleColorOff = getString("PvP Toggle.NameTags.Color Off", "&2");
		logToFile = getBoolean("In Combat.Punishments.Log To File", true);
		setMoneyPenalty(cm.getConfig().getDouble("Player Kills.Money Penalty", 10));
		blockPickNewbies = getBoolean("Newbie Protection.Block Pick Items", false);
		newbieGodMode = getBoolean("Newbie Protection.Protect From Everything", false);
		commandsWhitelist = getBoolean("In Combat.Stop Commands.Whitelist", true);
		blockPlaceBlocks = getBoolean("In Combat.Block Place Blocks", false);
		dropMode = DropMode.valueOf(getString("Player Drops.Mode", "ALWAYS").toUpperCase());
		updateLocation = getString("Update Check.Update Location", "Spigot");
		commandsOnPvPLog = (List<String>) cm.getConfig().getList("In Combat.Punishments.Commands On PvPLog", commandsOnPvPLog);
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

	private static String getString(final String message, final String defaultValue) {
		return cm.getConfig().getString(message, defaultValue);
	}

	private static boolean getBoolean(final String message, final boolean defaultValue) {
		return cm.getConfig().getBoolean(message, defaultValue);
	}

	public static String getUpdateLocation() {
		return updateLocation;
	}

	public static boolean isInCombatEnabled() {
		return inCombatEnabled;
	}

	public static int getTimeInCombat() {
		return timeInCombat;
	}

	public static boolean isStopCommands() {
		return stopCommands;
	}

	public static boolean isPunishmentsEnabled() {
		return punishmentsEnabled;
	}

	public static boolean isDropInventory() {
		return dropInventory;
	}

	public static boolean isDropExp() {
		return dropExp;
	}

	public static boolean isDropArmor() {
		return dropArmor;
	}

	public static boolean isKillOnLogout() {
		return killOnLogout;
	}

	public static List<String> getWorldsExcluded() {
		return worldsExcluded;
	}

	public static boolean isDisableFly() {
		return disableFly;
	}

	public static boolean isOnlyTagAttacker() {
		return onlyTagAttacker;
	}

	public static boolean isUpdateCheck() {
		return updateCheck;
	}

	public static boolean isAutoUpdate() {
		return autoUpdate;
	}

	public static boolean isNewbieProtectionEnabled() {
		return newbieProtectionEnabled;
	}

	public static int getNewbieProtectionTime() {
		return newbieProtectionTime;
	}

	public static boolean isFineEnabled() {
		return fineEnabled;
	}

	public static double getFineAmount() {
		return fineAmount;
	}

	public static boolean isPvpBlood() {
		return pvpBlood;
	}

	public static boolean isDisableGamemode() {
		return disableGamemode;
	}

	public static boolean isUpdate() {
		return update;
	}

	public static List<String> getCommandsAllowed() {
		return commandsAllowed;
	}

	public static int getToggleCooldown() {
		return toggleCooldown;
	}

	public static boolean isDefaultPvp() {
		return defaultPvp;
	}

	public static boolean isDisableDisguise() {
		return disableDisguise;
	}

	public static boolean isKillAbuseEnabled() {
		return killAbuseEnabled;
	}

	public static int getKillAbuseMaxKills() {
		return killAbuseMaxKills;
	}

	public static int getKillAbuseTime() {
		return killAbuseTime;
	}

	public static List<String> getKillAbuseCommands() {
		return killAbuseCommands;
	}

	public static boolean isToggleBroadcast() {
		return toggleBroadcast;
	}

	public static boolean isBlockEnderPearl() {
		return blockEnderPearl;
	}

	public static boolean isAutoSoupEnabled() {
		return autoSoupEnabled;
	}

	public static double getSoupHealth() {
		return soupHealth;
	}

	public static boolean isInCombatSilent() {
		return inCombatSilent;
	}

	public static boolean isConfigUpdated() {
		return configUpdated;
	}

	public static boolean isStopBorderHopping() {
		return stopBorderHopping;
	}

	public static boolean isIgnoreNoDamageHits() {
		return ignoreNoDamageHits;
	}

	public static String getNameTagColor() {
		return nameTagColor;
	}

	public static boolean isUseNameTag() {
		return useNameTag;
	}

	public static String getLocale() {
		return locale;
	}

	public static int getRespawnProtection() {
		return respawnProtection;
	}

	public static double getMoneyReward() {
		return moneyReward;
	}

	public static boolean isCommandsOnKillEnabled() {
		return commandsOnKillEnabled;
	}

	public static List<String> getCommandsOnKill() {
		return commandsOnKill;
	}

	public static boolean isDisableInvisibility() {
		return disableInvisibility;
	}

	public static boolean isToggleNametagsEnabled() {
		return toggleNametagsEnabled;
	}

	public static String getToggleColorOn() {
		return toggleColorOn;
	}

	public static String getToggleColorOff() {
		return toggleColorOff;
	}

	public static boolean isLogToFile() {
		return logToFile;
	}

	public static double getMoneyPenalty() {
		return moneyPenalty;
	}

	public static boolean isBlockPickNewbies() {
		return blockPickNewbies;
	}

	public static boolean isNewbieGodMode() {
		return newbieGodMode;
	}

	public static boolean isCommandsWhitelist() {
		return commandsWhitelist;
	}

	public static boolean isBlockPlaceBlocks() {
		return blockPlaceBlocks;
	}

	public static void setUpdate(final boolean update) {
		Variables.update = update;
	}

	public static void setConfigUpdated(final boolean configUpdated) {
		Variables.configUpdated = configUpdated;
	}

	public static void setFineEnabled(final boolean fineEnabled) {
		Variables.fineEnabled = fineEnabled;
	}

	public static void setMoneyReward(final double moneyReward) {
		Variables.moneyReward = moneyReward;
	}

	public static void setMoneyPenalty(final double moneyPenalty) {
		Variables.moneyPenalty = moneyPenalty;
	}

	public static DropMode getDropMode() {
		return dropMode;
	}

	public static void setUseNameTag(final boolean useNameTag) {
		Variables.useNameTag = useNameTag;
	}

	public static void setToggleNametagsEnabled(final boolean toggleNametagsEnabled) {
		Variables.toggleNametagsEnabled = toggleNametagsEnabled;
	}

	public static List<String> getCommandsOnPvPLog() {
		return commandsOnPvPLog;
	}
}
