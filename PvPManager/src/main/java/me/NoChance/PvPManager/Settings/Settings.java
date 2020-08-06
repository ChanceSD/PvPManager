package me.NoChance.PvPManager.Settings;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class Settings {

	public enum DropMode {
		ALWAYS, DROP, KEEP, TRANSFER
	}

	public static boolean DEBUG = false;
	private static int configVersion;
	private static String minecraftVersion;
	private static boolean autoUpdate;
	private static boolean blockEnderPearl;
	private static boolean blockTeleport;
	private static boolean blockPickNewbies;
	private static boolean blockPlaceBlocks;
	private static boolean blockEat;
	private static List<String> commandsAllowed;
	private static List<String> commandsOnKill;
	private static List<String> commandsOnPvPLog;
	private static List<String> commandsPvPOff;
	private static List<String> commandsPvPOn;
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
	private static List<String> killAbuseCommands;
	private static boolean killAbuseEnabled;
	private static int killAbuseMaxKills;
	private static int killAbuseTime;
	private static boolean killOnLogout;
	private static String locale;
	private static boolean logToFile;
	private static double moneyPenalty;
	private static double moneyReward;
	private static boolean moneySteal;
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
	private static boolean optOutMetrics;
	private static double soupHealth;
	private static boolean borderHoppingVulnerable;
	private static boolean borderHoppingPushback;
	private static boolean borderHoppingResetCombatTag;
	private static boolean worldguardOverrides;
	private static boolean glowingInCombat;
	private static boolean blockInteractInCombat;
	private static boolean untagEnemy;
	private static String teamColor;
	private static String actionBarMessage;
	private static String actionBarSymbol;
	private static int actionBarTotalBars;
	private static List<String> newbieBlacklist;
	private static Set<String> worldsExcluded;
	private static Set<String> playerKillsWGExclusions;
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

	public static void initizalizeVariables(final Config c) {
		assignSections(c);

		minecraftVersion = Bukkit.getBukkitVersion().isEmpty() ? "0" : Bukkit.getBukkitVersion().replaceAll("-.+", "");
		locale = GENERAL.getString("Locale", "en").toUpperCase();
		defaultPvp = GENERAL.getBoolean("Default PvP", true);
		pvpBlood = GENERAL.getBoolean("PvP Blood", true);
		dropMode = DropMode.valueOf(GENERAL.getString("Player Drop Mode", "ALWAYS").toUpperCase());
		ignoreNoDamageHits = GENERAL.getBoolean("Ignore No Damage Hits", false);
		soupHealth = GENERAL.getDouble("Auto Soup Health", 0);
		worldsExcluded = new HashSet<>(getList(GENERAL.getStringList("World Exclusions")));

		borderHoppingVulnerable = BORDERHOPPING.getBoolean("Vulnerable", true);
		borderHoppingPushback = BORDERHOPPING.getBoolean("Push Back", true);
		borderHoppingResetCombatTag = BORDERHOPPING.getBoolean("Reset Combat Tag", true);

		disableFly = DISABLE.getBoolean("Fly", true);
		disableGamemode = DISABLE.getBoolean("GameMode", true);
		disableDisguise = DISABLE.getBoolean("Disguise", true);
		disableGodMode = DISABLE.getBoolean("GodMode", true);
		disableInvisibility = DISABLE.getBoolean("Invisibility", false);

		inCombatEnabled = TAGGEDCOMBAT.getBoolean("Enabled", true);
		timeInCombat = TAGGEDCOMBAT.getInt("Time", 10);
		nameTagPrefix = TAGGEDCOMBAT.getString("NameTag Prefix", "&c");
		teamColor = TAGGEDCOMBAT.getString("Color", "&c").replace("&", "");
		glowingInCombat = TAGGEDCOMBAT.getBoolean("Glowing", true);
		useNameTag = !nameTagPrefix.equalsIgnoreCase("none") && !nameTagPrefix.isEmpty();
		actionBarMessage = Messages.colorize(TAGGEDCOMBAT.getString("Action Bar.Message", ""));
		actionBarSymbol = TAGGEDCOMBAT.getString("Action Bar.Symbol", "▊");
		actionBarTotalBars = TAGGEDCOMBAT.getInt("Action Bar.Total Bars", 10);
		untagEnemy = TAGGEDCOMBAT.getBoolean("Untag Enemy", false);
		blockEnderPearl = TAGGEDCOMBAT.getBoolean("Block.EnderPearls", true);
		blockTeleport = TAGGEDCOMBAT.getBoolean("Block.Teleport", true);
		blockPlaceBlocks = TAGGEDCOMBAT.getBoolean("Block.Place Blocks", false);
		blockInteractInCombat = TAGGEDCOMBAT.getBoolean("Block.Interact", false);
		blockEat = TAGGEDCOMBAT.getBoolean("Block.Eat", false);
		stopCommands = TAGGEDCOMBAT.getBoolean("Block.Commands.Enabled", true);
		commandsWhitelist = TAGGEDCOMBAT.getBoolean("Block.Commands.Whitelist", true);
		commandsAllowed = getList(TAGGEDCOMBAT.getStringList("Block.Commands.Command List"));
		punishOnKick = TAGGEDCOMBAT.getBoolean("Punishments.Punish On Kick", true);
		fineAmount = TAGGEDCOMBAT.getDouble("Punishments.Money Penalty", 0.00);
		logToFile = TAGGEDCOMBAT.getBoolean("Punishments.Log To File", true);
		killOnLogout = TAGGEDCOMBAT.getBoolean("Punishments.Kill on Logout.Enabled", true);
		dropInventory = TAGGEDCOMBAT.getBoolean("Punishments.Kill on Logout.Player Drops.Inventory", true);
		dropExp = TAGGEDCOMBAT.getBoolean("Punishments.Kill on Logout.Player Drops.Experience", true);
		dropArmor = TAGGEDCOMBAT.getBoolean("Punishments.Kill on Logout.Player Drops.Armor", true);
		commandsOnPvPLog = getList(TAGGEDCOMBAT.getStringList("Punishments.Commands On PvPLog"));

		newbieProtectionEnabled = NEWBIEPROTECTION.getBoolean("Enabled", true);
		newbieProtectionTime = NEWBIEPROTECTION.getInt("Time(minutes)", 5);
		blockPickNewbies = NEWBIEPROTECTION.getBoolean("Block Pick Items", false);
		newbieGodMode = NEWBIEPROTECTION.getBoolean("Protect From Everything", false);
		newbieBlacklist = getList(NEWBIEPROTECTION.getStringList("Command Blacklist"));

		killAbuseEnabled = KILLABUSE.getBoolean("Enabled", true);
		killAbuseMaxKills = KILLABUSE.getInt("Max Kills", 5);
		killAbuseTime = KILLABUSE.getInt("Time Limit", 60);
		killAbuseCommands = getList(KILLABUSE.getStringList("Commands on Abuse"));
		respawnProtection = KILLABUSE.getInt("Respawn Protection", 5);

		setMoneyReward(PLAYERKILLS.getDouble("Money Reward", 10));
		setMoneyPenalty(PLAYERKILLS.getDouble("Money Penalty", 10));
		moneySteal = PLAYERKILLS.getBoolean("Money Steal", false);
		commandsOnKill = getList(PLAYERKILLS.getStringList("Commands On Kill"));
		playerKillsWGExclusions = new HashSet<>(getList(PLAYERKILLS.getStringList("WorldGuard Exclusions")));

		toggleCooldown = PVPTOGGLE.getInt("Cooldown", 15);
		setToggleNametagsEnabled(PVPTOGGLE.getBoolean("NameTags.Enabled", false));
		toggleColorOn = PVPTOGGLE.getString("NameTags.Prefix On", "&1");
		toggleColorOff = PVPTOGGLE.getString("NameTags.Prefix Off", "&2");
		commandsPvPOn = getList(PVPTOGGLE.getStringList("Commands PvP On"));
		commandsPvPOff = getList(PVPTOGGLE.getStringList("Commands PvP Off"));
		forcePvPOnWorldChange = PVPTOGGLE.getBoolean("Force On Change World", false);
		worldguardOverrides = PVPTOGGLE.getBoolean("WorldGuard Overrides", true);

		updateCheck = UPDATECHECK.getBoolean("Enabled", true);
		autoUpdate = UPDATECHECK.getBoolean("Auto Update", true);

		optOutMetrics = c.getBoolean("Metrics.Opt-out", false);
		configVersion = c.getInt("Config Version");

	}

	public static void helpMenu(final Player player) {
		player.sendMessage(ChatColor.GOLD + "-------------- PvPManager Help Page --------------");
		player.sendMessage(ChatColor.GOLD + "/pvp [player] " + ChatColor.WHITE + "| Set PvP Enabled or Disabled");
		player.sendMessage(ChatColor.GOLD + "/pvpinfo [player] " + ChatColor.WHITE + "| Check your or other player info");
		player.sendMessage(ChatColor.GOLD + "/pvplist " + ChatColor.WHITE + "| List all players with PvP enabled");
		player.sendMessage(ChatColor.GOLD + "/pvpo " + ChatColor.WHITE + "| Override all PvP protections");
		player.sendMessage(ChatColor.GOLD + "/pvpstatus [player] " + ChatColor.WHITE + "| Check yours or other player PvP status");
		player.sendMessage(ChatColor.GOLD + "/newbie disable " + ChatColor.WHITE + "| Disable Newbie Protection");
		player.sendMessage(ChatColor.GOLD + "/pm " + ChatColor.WHITE + "| Show This Help Page");
		player.sendMessage(ChatColor.GOLD + "/pm update " + ChatColor.WHITE + "| Update to Latest Version");
		player.sendMessage(ChatColor.GOLD + "/pm reload " + ChatColor.WHITE + "| Reload PvPManager");
		player.sendMessage(ChatColor.GOLD + "/pm cleanup " + ChatColor.WHITE + "| Cleanup inactive users from database");
		player.sendMessage(ChatColor.GOLD + "-------------------------------------------------");
	}

	private static List<String> getList(final List<String> list) {
		for (final Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
			final String string = iterator.next();
			if (string.startsWith("example")) {
				iterator.remove();
			}
		}
		return list;
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

	public static Set<String> getWorldsExcluded() {
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

	public static boolean isBlockEat() {
		return blockEat;
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

	public static boolean isMoneySteal() {
		return moneySteal;
	}

	public static boolean isUntagEnemy() {
		return untagEnemy;
	}

	public static void setUntagEnemy(final boolean untagEnemy) {
		Settings.untagEnemy = untagEnemy;
	}

	public static boolean borderHoppingVulnerable() {
		return borderHoppingVulnerable;
	}

	public static boolean borderHoppingPushback() {
		return borderHoppingPushback;
	}

	public static void setBorderHoppingPushback(final boolean borderHoppingPushback) {
		Settings.borderHoppingPushback = borderHoppingPushback;
	}

	public static boolean borderHoppingResetCombatTag() {
		return borderHoppingResetCombatTag;
	}

	public static void setBorderHoppingResetCombatTag(final boolean borderHoppingResetCombatTag) {
		Settings.borderHoppingResetCombatTag = borderHoppingResetCombatTag;
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

	public static void setPvpBlood(final boolean pvpBlood) {
		Settings.pvpBlood = pvpBlood;
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

	public static boolean isGlowingInCombat() {
		return glowingInCombat;
	}

	public static boolean blockInteract() {
		return blockInteractInCombat;
	}

	public static String getTeamColor() {
		return teamColor;
	}

	public static String getActionBarMessage() {
		return actionBarMessage;
	}

	public static String getActionBarSymbol() {
		return actionBarSymbol;
	}

	public static int getActionBarBars() {
		return actionBarTotalBars;
	}

	public static Set<String> getKillsWGExclusions() {
		return playerKillsWGExclusions;
	}

	public static String getMinecraftVersion() {
		return minecraftVersion;
	}
}
