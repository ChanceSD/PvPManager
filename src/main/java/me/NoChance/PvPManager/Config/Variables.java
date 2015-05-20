package me.NoChance.PvPManager.Config;

import java.util.Arrays;
import java.util.List;

import me.NoChance.PvPManager.Managers.ConfigManager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class Variables {

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
	private static boolean toggleSignsEnabled;
	private static boolean disableToggleCommand;
	private static boolean onlyTagAttacker;
	private static boolean updateCheck;
	private static boolean autoUpdate;
	private static boolean newbieProtectionEnabled;
	private static int newbieProtectionTime;
	private static boolean broadcastPvpLog;
	private static boolean fineEnabled;
	private static double fineAmount;
	private static boolean pvpBlood;
	private static boolean disableGamemode;
	private static boolean update = false;
	private static List<String> commandsAllowed = Arrays.asList(new String[] { "tell", "money" });
	private static int toggleCooldown;
	private static boolean defaultPvp;
	private static boolean disableDisguise;
	private static boolean killAbuseEnabled;
	private static int killAbuseMaxKills;
	private static int killAbuseTime;
	private static List<String> killAbuseCommands = Arrays.asList("kick <player> Kill Abuse Is Not Allowed!");
	private static boolean toggleBroadcast;
	private static boolean toggleOffOnDeath;
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
	private static boolean transferDrops;
	private static double moneyPenalty;
	private static boolean blockPickNewbies;
	private static boolean newbieGodMode;
	private static boolean commandsWhitelist;
	private static boolean blockPlaceBlocks;

	private Variables() {
	}

	@SuppressWarnings("unchecked")
	public static void initizalizeVariables(final ConfigManager cm) {
		inCombatEnabled = cm.getConfig().getBoolean("In Combat.Enabled", true);
		timeInCombat = cm.getConfig().getInt("In Combat.Time(seconds)", 10);
		blockEnderPearl = cm.getConfig().getBoolean("In Combat.Block EnderPearl", true);
		stopCommands = cm.getConfig().getBoolean("In Combat.Stop Commands.Enabled", true);
		punishmentsEnabled = cm.getConfig().getBoolean("In Combat.Punishments.Enabled", true);
		dropInventory = cm.getConfig().getBoolean("In Combat.Punishments.Drops.Inventory", true);
		dropExp = cm.getConfig().getBoolean("In Combat.Punishments.Drops.Experience", true);
		dropArmor = cm.getConfig().getBoolean("In Combat.Punishments.Drops.Armor", true);
		killOnLogout = cm.getConfig().getBoolean("In Combat.Punishments.Kill on Logout", true);
		worldsExcluded = (List<String>) cm.getConfig().getList("World Exclusions", worldsExcluded);
		disableFly = cm.getConfig().getBoolean("Disable Fly", true);
		toggleSignsEnabled = cm.getConfig().getBoolean("Toggle Signs.Enabled", true);
		disableToggleCommand = cm.getConfig().getBoolean("Toggle Signs.Disable Toggle Command", false);
		onlyTagAttacker = cm.getConfig().getBoolean("In Combat.Only Tag Attacker", false);
		updateCheck = cm.getConfig().getBoolean("Update Check.Enabled", true);
		autoUpdate = cm.getConfig().getBoolean("Update Check.Auto Update", true);
		newbieProtectionEnabled = cm.getConfig().getBoolean("Newbie Protection.Enabled", true);
		newbieProtectionTime = cm.getConfig().getInt("Newbie Protection.Time(minutes)", 5);
		broadcastPvpLog = cm.getConfig().getBoolean("In Combat.Punishments.Broadcast PvPLog", true);
		setFineEnabled(cm.getConfig().getBoolean("In Combat.Punishments.Fine.Enabled", false));
		fineAmount = cm.getConfig().getDouble("In Combat.Punishments.Fine.Amount", 10.00);
		pvpBlood = cm.getConfig().getBoolean("PvP Blood", true);
		disableGamemode = cm.getConfig().getBoolean("Disable GameMode", true);
		commandsAllowed = (List<String>) cm.getConfig().getList("In Combat.Stop Commands.Commands", commandsAllowed);
		toggleCooldown = cm.getConfig().getInt("PvP Toggle.Cooldown(seconds)", 15);
		toggleBroadcast = cm.getConfig().getBoolean("PvP Toggle.Broadcast", false);
		toggleOffOnDeath = cm.getConfig().getBoolean("PvP Toggle.Toggle Off on Death", false);
		defaultPvp = cm.getConfig().getBoolean("Default PvP", true);
		disableDisguise = cm.getConfig().getBoolean("Disable Disguise", true);
		killAbuseMaxKills = cm.getConfig().getInt("Kill Abuse.Max Kills", 5);
		killAbuseTime = cm.getConfig().getInt("Kill Abuse.Time Limit", 60);
		killAbuseCommands = (List<String>) cm.getConfig().getList("Kill Abuse.Commands on Abuse", killAbuseCommands);
		killAbuseEnabled = cm.getConfig().getBoolean("Kill Abuse.Enabled", true);
		autoSoupEnabled = cm.getConfig().getBoolean("Auto Soup.Enabled", false);
		soupHealth = cm.getConfig().getDouble("Auto Soup.Health Gain", 6);
		inCombatSilent = cm.getConfig().getBoolean("In Combat.Silent", false);
		stopBorderHopping = cm.getConfig().getBoolean("Ignore Zones For Tagged", true);
		nameTagColor = cm.getConfig().getString("In Combat.Name Tag Color", "&c");
		useNameTag = nameTagColor.equalsIgnoreCase("none") ? false : true;
		locale = cm.getConfig().getString("Locale", "en").toUpperCase();
		ignoreNoDamageHits = cm.getConfig().getBoolean("Ignore No Damage Hits", false);
		respawnProtection = cm.getConfig().getInt("Kill Abuse.Respawn Protection", 5);
		moneyReward = cm.getConfig().getDouble("Player Kills.Money Reward", 10);
		commandsOnKillEnabled = cm.getConfig().getBoolean("Player Kills.Commands On Kill.Enabled", false);
		commandsOnKill = (List<String>) cm.getConfig().getList("Player Kills.Commands On Kill.Commands", commandsOnKill);
		disableInvisibility = cm.getConfig().getBoolean("Disable Invisibility", false);
		toggleNametagsEnabled = cm.getConfig().getBoolean("PvP Toggle.NameTags.Enabled", false);
		toggleColorOn = cm.getConfig().getString("PvP Toggle.NameTags.Color On", "&1");
		toggleColorOff = cm.getConfig().getString("PvP Toggle.NameTags.Color Off", "&2");
		logToFile = cm.getConfig().getBoolean("In Combat.Punishments.Log To File", true);
		transferDrops = cm.getConfig().getBoolean("Player Kills.Transfer Drops", false);
		moneyPenalty = cm.getConfig().getDouble("Player Kills.Money Penalty", 10);
		blockPickNewbies = cm.getConfig().getBoolean("Newbie Protection.Block Pick Items", false);
		newbieGodMode = cm.getConfig().getBoolean("Newbie Protection.Protect From Everything", false);
		commandsWhitelist = cm.getConfig().getBoolean("In Combat.Stop Commands.Whitelist", true);
		blockPlaceBlocks = cm.getConfig().getBoolean("In Combat.Block Place Blocks", false);
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

	public static boolean isToggleSignsEnabled() {
		return toggleSignsEnabled;
	}

	public static boolean isDisableToggleCommand() {
		return disableToggleCommand;
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

	public static boolean isBroadcastPvpLog() {
		return broadcastPvpLog;
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

	public static boolean isToggleOffOnDeath() {
		return toggleOffOnDeath;
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

	public static boolean isTransferDrops() {
		return transferDrops;
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
}
