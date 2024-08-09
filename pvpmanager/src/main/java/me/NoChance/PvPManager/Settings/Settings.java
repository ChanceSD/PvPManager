package me.NoChance.PvPManager.Settings;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;


import me.NoChance.PvPManager.Utils.ChatUtils;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.MCVersion;

public final class Settings {

	public enum DropMode {
		ALWAYS, DROP, KEEP, TRANSFER, CLEAR
	}

	public static boolean DEBUG = false;
	private static boolean globalStatus = true;
	private static int configVersion;
	private static String minecraftVersion;
	private static boolean isReloading;
	private static boolean autoUpdate;
	private static boolean blockEnderPearl;
	private static boolean blockChorusFruit;
	private static boolean blockTeleport;
	private static boolean blockPickNewbies;
	private static boolean blockPlaceBlocksNewbie;
	private static boolean blockPlaceBlocks;
	private static boolean blockBreakBlocksNewbie;
	private static boolean blockBreakBlocks;
	private static boolean blockGlideInCombat;
	private static boolean blockEat;
	private static boolean blockTotemUndying;
	private static boolean blockInventoryOpen;
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
	private static boolean disableELytra;
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
	private static String nameTagSuffix;
	private static boolean newbieGodMode;
	private static boolean newbieProtectionEnabled;
	private static int newbieProtectionTime;
	private static boolean newbieAllowDisable;
	private static boolean punishOnKick;
	private static boolean matchKickReason;
	private static List<String> forcePunishKickReason;
	private static boolean pvpBlood;
	private static int respawnProtection;
	private static boolean stopCommands;
	private static int timeInCombat;
	private static long timeInCombatMs;
	private static String toggleColorOff;
	private static String toggleColorOn;
	private static int toggleCooldown;
	private static boolean toggleNametagsEnabled;
	private static int pvpDisabledFee;
	private static boolean update = false;
	private static boolean checkUpdates;
	private static boolean optOutMetrics;
	private static double soupHealth;
	private static boolean soupBowlDisappear;
	private static boolean recyclePotionBottles;
	private static boolean recycleMilkBucket;
	private static boolean borderHoppingVulnerable;
	private static boolean borderHoppingPushback;
	private static boolean borderPushbackTakeElytra;
	private static boolean borderHoppingResetCombatTag;
	private static boolean worldguardOverrides;
	private static Set<String> worldguardOverridesList;
	private static int enderPearlCooldown;
	private static boolean enderPearlRenewTag;
	private static boolean glowingInCombat;
	private static boolean selfTag;
	private static boolean blockInteractInCombat;
	private static List<String> blockInteractItemList;
	private static boolean untagEnemy;
	private static boolean actionBarEnabled;
	private static String actionBarMessage;
	private static String actionBarSymbol;
	private static int actionBarTotalBars;
	private static boolean bossBarEnabled;
	private static String bossBarMessage;
	private static BarColor bossBarColor;
	private static BarStyle bossBarStyle;
	private static List<String> newbieBlacklist;
	private static Set<String> worldsExcluded;
	private static Set<String> playerKillsWGExclusions;
	private static boolean simpleClansNoPvPInWar;
	private static String cooldownsxEnderpearlID;
	private static Set<String> harmfulPotions;
	private static ConfigurationSection GENERAL;
	private static ConfigurationSection BORDERHOPPING;
	private static ConfigurationSection DISABLE;
	private static ConfigurationSection TAGGEDCOMBAT;
	private static ConfigurationSection NEWBIEPROTECTION;
	private static ConfigurationSection KILLABUSE;
	private static ConfigurationSection PLAYERKILLS;
	private static ConfigurationSection PVPTOGGLE;
	private static ConfigurationSection PLUGINHOOKS;
	private static ConfigurationSection UPDATECHECK;

	private Settings() {
	}

	private static void assignSections(final YamlConfiguration config) {
		GENERAL = config.getConfigurationSection("General");
		BORDERHOPPING = config.getConfigurationSection("Anti Border Hopping");
		DISABLE = config.getConfigurationSection("Disable");
		TAGGEDCOMBAT = config.getConfigurationSection("Tagged In Combat");
		NEWBIEPROTECTION = config.getConfigurationSection("Newbie Protection");
		KILLABUSE = config.getConfigurationSection("Kill Abuse");
		PLAYERKILLS = config.getConfigurationSection("Player Kills");
		PVPTOGGLE = config.getConfigurationSection("PvP Toggle");
		PLUGINHOOKS = config.getConfigurationSection("Plugin Hooks");
		UPDATECHECK = config.getConfigurationSection("Update Check");
	}

	public static void initizalizeVariables(final YamlConfiguration c) {
		assignSections(c);

		minecraftVersion = Bukkit.getBukkitVersion().isEmpty() ? "0" : Bukkit.getBukkitVersion().replaceAll("-.+", "");
		locale = GENERAL.getString("Locale", "en").toUpperCase();
		defaultPvp = GENERAL.getBoolean("Default PvP", true);
		pvpBlood = GENERAL.getBoolean("PvP Blood", true);
		dropMode = DropMode.valueOf(GENERAL.getString("Player Drop Mode", "ALWAYS").toUpperCase());
		ignoreNoDamageHits = GENERAL.getBoolean("Ignore No Damage Hits", true);
		soupHealth = GENERAL.getDouble("Auto Soup.Health", 0);
		soupBowlDisappear = GENERAL.getBoolean("Auto Soup.Bowl Disappear", false);
		recyclePotionBottles = GENERAL.getBoolean("Recycling.Potion Bottle", false);
		recycleMilkBucket = GENERAL.getBoolean("Recycling.Milk Bucket", false);
		worldsExcluded = new HashSet<>(getList(GENERAL.getStringList("World Exclusions")));

		borderHoppingVulnerable = BORDERHOPPING.getBoolean("Vulnerable", true);
		borderHoppingPushback = BORDERHOPPING.getBoolean("Push Back.Enabled", true);
		borderPushbackTakeElytra = BORDERHOPPING.getBoolean("Push Back.Remove Elytra", false);
		borderHoppingResetCombatTag = BORDERHOPPING.getBoolean("Reset Combat Tag", true);

		disableFly = DISABLE.getBoolean("Fly", true);
		disableGamemode = DISABLE.getBoolean("GameMode", true);
		disableDisguise = DISABLE.getBoolean("Disguise", true);
		disableGodMode = DISABLE.getBoolean("GodMode", true);
		disableELytra = MCVersion.isAtLeast(MCVersion.V1_9) && DISABLE.getBoolean("Elytra", false);
		disableInvisibility = DISABLE.getBoolean("Invisibility", false);

		inCombatEnabled = TAGGEDCOMBAT.getBoolean("Enabled", true);
		timeInCombat = TAGGEDCOMBAT.getInt("Time", 10);
		timeInCombatMs = timeInCombat * 1000L;
		nameTagPrefix = TAGGEDCOMBAT.getString("NameTag Prefix", "&c");
		nameTagSuffix = TAGGEDCOMBAT.getString("NameTag Suffix", "");
		glowingInCombat = TAGGEDCOMBAT.getBoolean("Glowing", true);
		selfTag = TAGGEDCOMBAT.getBoolean("Self Tag", false);
		actionBarEnabled = TAGGEDCOMBAT.getBoolean("Action Bar.Enabled", true);
		actionBarMessage = ChatUtils.colorize(TAGGEDCOMBAT.getString("Action Bar.Message", ""));
		actionBarSymbol = TAGGEDCOMBAT.getString("Action Bar.Symbol", "▊");
		actionBarTotalBars = TAGGEDCOMBAT.getInt("Action Bar.Total Bars", 10);
		bossBarEnabled = CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.9") && TAGGEDCOMBAT.getBoolean("Boss Bar.Enabled", true);
		bossBarMessage = ChatUtils.colorize(TAGGEDCOMBAT.getString("Boss Bar.Message", ""));
		bossBarColor = CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.9") ? BarColor.valueOf(TAGGEDCOMBAT.getString("Boss Bar.BarColor", "RED"))
		        : null;
		bossBarStyle = CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.9")
		        ? BarStyle.valueOf(TAGGEDCOMBAT.getString("Boss Bar.BarStyle", "SEGMENTED_10"))
		        : null;
		untagEnemy = TAGGEDCOMBAT.getBoolean("Untag Enemy", false);
		enderPearlCooldown = TAGGEDCOMBAT.getInt("EnderPearl.Cooldown", 15);
		enderPearlRenewTag = TAGGEDCOMBAT.getBoolean("EnderPearl.Renew Tag", true);

		blockEnderPearl = TAGGEDCOMBAT.getBoolean("Block.EnderPearls", true);
		blockChorusFruit = TAGGEDCOMBAT.getBoolean("Block.ChorusFruits", true);
		blockTeleport = TAGGEDCOMBAT.getBoolean("Block.Teleport", true);
		blockPlaceBlocks = TAGGEDCOMBAT.getBoolean("Block.Place Blocks", false);
		blockBreakBlocks = TAGGEDCOMBAT.getBoolean("Block.Break Blocks", false);
		blockInteractInCombat = TAGGEDCOMBAT.getBoolean("Block.Interact.Enabled", false);
		blockInteractItemList = TAGGEDCOMBAT.getStringList("Block.Interact.List");
		blockGlideInCombat = TAGGEDCOMBAT.getBoolean("Block.Elytra", false);
		blockEat = TAGGEDCOMBAT.getBoolean("Block.Eat", false);
		blockTotemUndying = TAGGEDCOMBAT.getBoolean("Block.Totem of Undying", false);
		blockInventoryOpen = TAGGEDCOMBAT.getBoolean("Block.Open Inventory", true);
		stopCommands = TAGGEDCOMBAT.getBoolean("Block.Commands.Enabled", true);
		commandsWhitelist = TAGGEDCOMBAT.getBoolean("Block.Commands.Whitelist", true);
		commandsAllowed = getList(TAGGEDCOMBAT.getStringList("Block.Commands.Command List"));
		punishOnKick = TAGGEDCOMBAT.getBoolean("Punishments.Punish On Kick.Enabled", true);
		matchKickReason = TAGGEDCOMBAT.getBoolean("Punishments.Punish On Kick.Match Kick Reason", false);
		forcePunishKickReason = getList(TAGGEDCOMBAT.getStringList("Punishments.Punish On Kick.Kick Reasons"));
		fineAmount = TAGGEDCOMBAT.getDouble("Punishments.Money Penalty", 0.00);
		logToFile = TAGGEDCOMBAT.getBoolean("Punishments.Log To File", true);
		killOnLogout = TAGGEDCOMBAT.getBoolean("Punishments.Kill on Logout.Enabled", true);
		dropInventory = TAGGEDCOMBAT.getBoolean("Punishments.Kill on Logout.Player Drops.Inventory", true);
		dropExp = TAGGEDCOMBAT.getBoolean("Punishments.Kill on Logout.Player Drops.Experience", true);
		dropArmor = TAGGEDCOMBAT.getBoolean("Punishments.Kill on Logout.Player Drops.Armor", true);
		commandsOnPvPLog = getCommandList(TAGGEDCOMBAT.getStringList("Punishments.Commands On PvPLog"));

		newbieProtectionEnabled = NEWBIEPROTECTION.getBoolean("Enabled", true);
		newbieProtectionTime = NEWBIEPROTECTION.getInt("Time(minutes)", 5);
		newbieAllowDisable = NEWBIEPROTECTION.getBoolean("Allow Player Disable", true);
		blockPickNewbies = NEWBIEPROTECTION.getBoolean("Block Pick Items", false);
		blockPlaceBlocksNewbie = NEWBIEPROTECTION.getBoolean("Block Place Blocks", false);
		blockBreakBlocksNewbie = NEWBIEPROTECTION.getBoolean("Block Break Blocks", false);
		newbieGodMode = NEWBIEPROTECTION.getBoolean("Protect From Everything", false);
		newbieBlacklist = getList(NEWBIEPROTECTION.getStringList("Command Blacklist"));

		killAbuseEnabled = KILLABUSE.getBoolean("Enabled", true);
		killAbuseMaxKills = KILLABUSE.getInt("Max Kills", 5);
		killAbuseTime = KILLABUSE.getInt("Time Limit", 60);
		killAbuseCommands = getCommandList(KILLABUSE.getStringList("Commands on Abuse"));
		respawnProtection = KILLABUSE.getInt("Respawn Protection", 5);

		setMoneyReward(PLAYERKILLS.getDouble("Money Reward", 10));
		setMoneyPenalty(PLAYERKILLS.getDouble("Money Penalty", 10));
		moneySteal = PLAYERKILLS.getBoolean("Money Steal", false);
		commandsOnKill = getCommandList(PLAYERKILLS.getStringList("Commands On Kill"));
		playerKillsWGExclusions = new HashSet<>(getList(PLAYERKILLS.getStringList("WorldGuard Exclusions")));

		toggleCooldown = PVPTOGGLE.getInt("Cooldown", 15);
		setToggleNametagsEnabled(PVPTOGGLE.getBoolean("NameTags.Enabled", false));
		toggleColorOn = PVPTOGGLE.getString("NameTags.Prefix On", "&1");
		toggleColorOff = PVPTOGGLE.getString("NameTags.Prefix Off", "&2");
		pvpDisabledFee = PVPTOGGLE.getInt("PvP Disabled Money Fee", 0);
		commandsPvPOn = getCommandList(PVPTOGGLE.getStringList("Commands PvP On"));
		commandsPvPOff = getCommandList(PVPTOGGLE.getStringList("Commands PvP Off"));
		worldguardOverrides = PVPTOGGLE.getBoolean("WorldGuard Overrides", true);
		worldguardOverridesList = new HashSet<>(getList(PVPTOGGLE.getStringList("WorldGuard Overrides Region List")));

		cooldownsxEnderpearlID = PLUGINHOOKS.getString("CooldownsX.Enderpearl", "");

		useNameTag = !nameTagPrefix.isEmpty() || !nameTagSuffix.isEmpty() || toggleNametagsEnabled;
		Log.infoColor(ChatColor.GREEN + "Using player nametags: " + ChatColor.AQUA + useNameTag);

		checkUpdates = UPDATECHECK.getBoolean("Enabled", true);
		autoUpdate = UPDATECHECK.getBoolean("Auto Update", true);

		optOutMetrics = c.getBoolean("Metrics.Opt-out", false);

		harmfulPotions = new HashSet<>(c.getStringList("Harmful Potions"));
		Settings.setDEBUG(c.getBoolean("Debug Mode", false));
		configVersion = c.getInt("Config Version");
	}

	public static void helpMenu(final Player player) {
		player.sendMessage(ChatColor.GOLD + "-------------- PvPManager Help Page --------------");
		player.sendMessage(ChatColor.GOLD + "/pvp [player]" + helpSeparator() + ChatColor.WHITE + "Set PvP enabled or disabled");
		player.sendMessage(ChatColor.GOLD + "/pvpinfo [player]" + helpSeparator() + ChatColor.WHITE + "Check your or other player info");
		player.sendMessage(ChatColor.GOLD + "/pvplist [offline]" + helpSeparator() + ChatColor.WHITE + "List all online or offline players");
		player.sendMessage(ChatColor.GOLD + "/pvpo " + helpSeparator() + ChatColor.WHITE + "Override all PvP protections");
		player.sendMessage(ChatColor.GOLD + "/pvpstatus [player]" + helpSeparator() + ChatColor.WHITE + "Check your or other player PvP status");
		player.sendMessage(ChatColor.GOLD + "/pvpglobal <on|off>" + helpSeparator() + ChatColor.WHITE + "Toggle PvP for the whole server");
		player.sendMessage(ChatColor.GOLD + "/pvptag <player> <time>" + helpSeparator() + ChatColor.WHITE + "Check tag time left or tag a player");
		player.sendMessage(ChatColor.GOLD + "/untag <player>" + helpSeparator() + ChatColor.WHITE + "Untags a player");
		player.sendMessage(ChatColor.GOLD + "/newbie disable" + helpSeparator() + ChatColor.WHITE + "Disable newbie protection");
		player.sendMessage(ChatColor.GOLD + "/pmr" + helpSeparator() + ChatColor.WHITE + "Shows this help page");
		player.sendMessage(ChatColor.GOLD + "/pmr worlds" + helpSeparator() + ChatColor.WHITE + "Control panel to manage world PvP");
		player.sendMessage(ChatColor.GOLD + "/pmr reload" + helpSeparator() + ChatColor.WHITE + "Reload PvPManager");
		player.sendMessage(ChatColor.GOLD + "/pmr cleanup" + helpSeparator() + ChatColor.WHITE + "Cleanup inactive users from database");
		player.sendMessage(ChatColor.GOLD + "/pmr update" + helpSeparator() + ChatColor.WHITE + "Update to latest version");
		player.sendMessage(ChatColor.GOLD + "-------------------------------------------------");
	}

	private static String helpSeparator() {
		return ChatColor.RED + " >> ";
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

	public static void setLocale(final String locale) {
		Settings.locale = locale;
	}

	public static double getMoneyPenalty() {
		return moneyPenalty;
	}

	public static double getMoneyReward() {
		return moneyReward;
	}

	public static String getNameTagPrefix() {
		return nameTagPrefix;
	}

	public static String getNameTagSuffix() {
		return nameTagSuffix;
	}

	public static int getNewbieProtectionTime() {
		return newbieProtectionTime;
	}

	public static boolean isNewbieDisableAllowed() {
		return newbieAllowDisable;
	}

	public static int getRespawnProtection() {
		return respawnProtection;
	}

	public static int getTimeInCombat() {
		return timeInCombat;
	}

	public static long getTimeInCombatMs() {
		return timeInCombatMs;
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

	public static boolean isBlockChorusFruit() {
		return blockChorusFruit;
	}

	public static boolean isBlockPickNewbies() {
		return blockPickNewbies;
	}

	public static boolean isBlockPlaceBlocksNewbie() {
		return blockPlaceBlocksNewbie;
	}

	public static boolean isBlockBreakBlocksNewbie() {
		return blockBreakBlocksNewbie;
	}

	public static boolean isBlockPlaceBlocks() {
		return blockPlaceBlocks;
	}

	public static boolean isBlockBreakBlocks() {
		return blockBreakBlocks;
	}

	public static boolean isBlockGlide() {
		return blockGlideInCombat;
	}

	public static boolean isBlockEat() {
		return blockEat;
	}

	public static boolean isBlockTotemUndying() {
		return blockTotemUndying;
	}

	public static boolean isBlockInventoryOpen() {
		return blockInventoryOpen;
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

	public static boolean isDisableElytra() {
		return disableELytra;
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

	public static void setIgnoreNoDamageHits(final boolean ignoreNoDamageHits) {
		Settings.ignoreNoDamageHits = ignoreNoDamageHits;
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

	public static boolean isBorderPushbackTakeElytra() {
		return borderPushbackTakeElytra;
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
		return checkUpdates;
	}

	public static boolean isOptOutMetrics() {
		return optOutMetrics;
	}

	public static boolean punishOnKick() {
		return punishOnKick;
	}

	public static boolean matchKickReason() {
		return matchKickReason;
	}

	public static void setMatchKickReason(final boolean matchKickReason) {
		Settings.matchKickReason = matchKickReason;
	}

	public static List<String> getPunishKickReasons() {
		return forcePunishKickReason;
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

	public static boolean useNameTag() {
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

	public static boolean isSoupBowlDisappear() {
		return soupBowlDisappear;
	}

	public static boolean isRecyclePotionBottles() {
		return recyclePotionBottles;
	}

	public static boolean isRecycleMilkBucket() {
		return recycleMilkBucket;
	}

	public static boolean isBlockTeleport() {
		return blockTeleport;
	}

	public static boolean isWorldguardOverrides() {
		return worldguardOverrides;
	}

	public static int getEnderPearlCooldown() {
		return enderPearlCooldown;
	}

	public static boolean isEnderPearlRenewTag() {
		return enderPearlRenewTag;
	}

	public static boolean isGlowingInCombat() {
		return glowingInCombat;
	}

	public static boolean blockInteract() {
		return blockInteractInCombat;
	}

	public static List<String> getBlockInteractItemList() {
		return blockInteractItemList;
	}

	public static boolean isGlobalStatus() {
		return globalStatus;
	}

	public static void setGlobalStatus(final boolean globalStatus) {
		Settings.globalStatus = globalStatus;
	}

	public static boolean isActionBarEnabled() {
		return actionBarEnabled;
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

	public static boolean isBossBarEnabled() {
		return bossBarEnabled;
	}

	public static String getBossBarMessage() {
		return bossBarMessage;
	}

	public static BarColor getBossBarColor() {
		return bossBarColor;
	}

	public static BarStyle getBossBarStyle() {
		return bossBarStyle;
	}

	public static Set<String> getKillsWGExclusions() {
		return playerKillsWGExclusions;
	}

	public static String getMinecraftVersion() {
		return minecraftVersion;
	}

	public static boolean isSimpleClansNoPvPInWar() {
		return simpleClansNoPvPInWar;
	}

	public static String getCooldownsxEnderpearlID() {
		return cooldownsxEnderpearlID;
	}

	public static boolean isSelfTag() {
		return selfTag;
	}

	public static void setSelfTag(final boolean selfTag) {
		Settings.selfTag = selfTag;
	}

	public static boolean isReloading() {
		return isReloading;
	}

	public static void setReloading(final boolean isReloading) {
		Settings.isReloading = isReloading;
	}

	public static Set<String> getHarmfulPotions() {
		return harmfulPotions;
	}

	public static void setDEBUG(final boolean dEBUG) {
		Settings.DEBUG = dEBUG;
		Log.setDebug(dEBUG);
	}

	public static Set<String> getWorldguardOverridesList() {
		return worldguardOverridesList;
	}

	public static int getPvPDisabledFee() {
		return pvpDisabledFee;
	}

	public static ConfigurationSection getPLUGINHOOKS() {
		return PLUGINHOOKS;
	}
}
