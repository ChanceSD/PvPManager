package me.chancesd.pvpmanager.setting;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.CommandSender;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import me.chancesd.pvpmanager.utils.ChatUtils;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.MCVersion;

public final class Settings {

	public enum DropMode {
		ALWAYS, DROP, KEEP, TRANSFER, CLEAR
	}

	private static boolean debug = false;
	private static boolean globalStatus = true;
	private static int configVersion;
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
	private static double expSteal;
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
	private static boolean killAbuseWarn;
	private static boolean actionBarEnabled;
	private static String actionBarMessage;
	private static String actionBarSymbol;
	private static int actionBarTotalBars;
	private static boolean bossBarEnabled;
	private static String bossBarMessage;
	private static BarColor bossBarColor;
	private static BarStyle bossBarStyle;
	private static boolean healthBelowName;
	private static String healthBelowNameSymbol;
	private static List<String> newbieBlacklist;
	private static Set<String> worldsExcluded;
	private static Set<String> playerKillsWGExclusions;
	private static String cooldownsxEnderpearlID;
	private static Set<String> harmfulPotions;
	private static Map<Material, Integer> itemCooldowns;
	private static ConfigurationSection generalSection;
	private static ConfigurationSection borderHoppingSection;
	private static ConfigurationSection disableActionsSection;
	private static ConfigurationSection combatTagSection;
	private static ConfigurationSection newbieSection;
	private static ConfigurationSection killAbuseSection;
	private static ConfigurationSection playerKillsSection;
	private static ConfigurationSection pvpToggleSection;
	private static ConfigurationSection pluginHookSection;
	private static ConfigurationSection updateSection;

	private Settings() {
	}

	private static void assignSections(final YamlConfiguration config) {
		generalSection = config.getConfigurationSection("General");
		borderHoppingSection = config.getConfigurationSection("Anti Border Hopping");
		disableActionsSection = config.getConfigurationSection("Disable");
		combatTagSection = config.getConfigurationSection("Tagged In Combat");
		newbieSection = config.getConfigurationSection("Newbie Protection");
		killAbuseSection = config.getConfigurationSection("Kill Abuse");
		playerKillsSection = config.getConfigurationSection("Player Kills");
		pvpToggleSection = config.getConfigurationSection("PvP Toggle");
		pluginHookSection = config.getConfigurationSection("Plugin Hooks");
		updateSection = config.getConfigurationSection("Update Check");
	}

	public static void initizalizeVariables(final YamlConfiguration c) {
		assignSections(c);

		locale = generalSection.getString("Locale", "en").toUpperCase();
		defaultPvp = generalSection.getBoolean("Default PvP", true);
		pvpBlood = generalSection.getBoolean("PvP Blood", true);
		dropMode = DropMode.valueOf(generalSection.getString("Player Drop Mode", "ALWAYS").toUpperCase());
		ignoreNoDamageHits = generalSection.getBoolean("Ignore No Damage Hits", true);
		soupHealth = generalSection.getDouble("Auto Soup.Health", 0);
		soupBowlDisappear = generalSection.getBoolean("Auto Soup.Bowl Disappear", false);
		recyclePotionBottles = generalSection.getBoolean("Recycling.Potion Bottle", false);
		recycleMilkBucket = generalSection.getBoolean("Recycling.Milk Bucket", false);
		healthBelowName = generalSection.getBoolean("Show health under name.Enabled", true);
		healthBelowNameSymbol = ChatUtils.colorize(generalSection.getString("Show health under name.Display Name", "&c❤"));
		worldsExcluded = new HashSet<>(getList(generalSection.getStringList("World Exclusions")));

		borderHoppingVulnerable = borderHoppingSection.getBoolean("Vulnerable", true);
		borderHoppingPushback = borderHoppingSection.getBoolean("Push Back.Enabled", true);
		borderPushbackTakeElytra = borderHoppingSection.getBoolean("Push Back.Remove Elytra", false);
		borderHoppingResetCombatTag = borderHoppingSection.getBoolean("Reset Combat Tag", true);

		disableFly = disableActionsSection.getBoolean("Fly", true);
		disableGamemode = disableActionsSection.getBoolean("GameMode", true);
		disableDisguise = disableActionsSection.getBoolean("Disguise", true);
		disableGodMode = disableActionsSection.getBoolean("GodMode", true);
		disableELytra = MCVersion.isAtLeast(MCVersion.V1_9) && disableActionsSection.getBoolean("Elytra", false);
		disableInvisibility = disableActionsSection.getBoolean("Invisibility", false);

		inCombatEnabled = combatTagSection.getBoolean("Enabled", true);
		timeInCombat = combatTagSection.getInt("Time", 10);
		timeInCombatMs = timeInCombat * 1000L;
		nameTagPrefix = combatTagSection.getString("NameTag Prefix", "&c");
		nameTagSuffix = combatTagSection.getString("NameTag Suffix", "");
		glowingInCombat = combatTagSection.getBoolean("Glowing", true);
		selfTag = combatTagSection.getBoolean("Self Tag", false);
		actionBarEnabled = combatTagSection.getBoolean("Action Bar.Enabled", true);
		actionBarMessage = ChatUtils.colorize(combatTagSection.getString("Action Bar.Message", ""));
		actionBarSymbol = combatTagSection.getString("Action Bar.Symbol", "▊");
		actionBarTotalBars = combatTagSection.getInt("Action Bar.Total Bars", 10);
		bossBarEnabled = MCVersion.isAtLeast(MCVersion.V1_9) && combatTagSection.getBoolean("Boss Bar.Enabled", true);
		bossBarMessage = ChatUtils.colorize(combatTagSection.getString("Boss Bar.Message", ""));
		bossBarColor = MCVersion.isAtLeast(MCVersion.V1_9) ? BarColor.valueOf(combatTagSection.getString("Boss Bar.BarColor", "RED")) : null;
		bossBarStyle = MCVersion.isAtLeast(MCVersion.V1_9) ? BarStyle.valueOf(combatTagSection.getString("Boss Bar.BarStyle", "SEGMENTED_10")) : null;
		untagEnemy = combatTagSection.getBoolean("Untag Enemy", false);
		enderPearlCooldown = combatTagSection.getInt("EnderPearl.Cooldown", 15);
		enderPearlRenewTag = combatTagSection.getBoolean("EnderPearl.Renew Tag", true);

		blockEnderPearl = combatTagSection.getBoolean("Block.EnderPearls", true);
		blockChorusFruit = combatTagSection.getBoolean("Block.ChorusFruits", true);
		blockTeleport = combatTagSection.getBoolean("Block.Teleport", true);
		blockPlaceBlocks = combatTagSection.getBoolean("Block.Place Blocks", false);
		blockBreakBlocks = combatTagSection.getBoolean("Block.Break Blocks", false);
		blockInteractInCombat = combatTagSection.getBoolean("Block.Interact.Enabled", false);
		blockInteractItemList = combatTagSection.getStringList("Block.Interact.List");
		blockGlideInCombat = combatTagSection.getBoolean("Block.Elytra", false);
		blockEat = combatTagSection.getBoolean("Block.Eat", false);
		blockTotemUndying = combatTagSection.getBoolean("Block.Totem of Undying", false);
		blockInventoryOpen = combatTagSection.getBoolean("Block.Open Inventory", true);
		stopCommands = combatTagSection.getBoolean("Block.Commands.Enabled", true);
		commandsWhitelist = combatTagSection.getBoolean("Block.Commands.Whitelist", true);
		commandsAllowed = getList(combatTagSection.getStringList("Block.Commands.Command List"));
		punishOnKick = combatTagSection.getBoolean("Punishments.Punish On Kick.Enabled", true);
		matchKickReason = combatTagSection.getBoolean("Punishments.Punish On Kick.Match Kick Reason", false);
		forcePunishKickReason = getList(combatTagSection.getStringList("Punishments.Punish On Kick.Kick Reasons"));
		fineAmount = combatTagSection.getDouble("Punishments.Money Penalty", 0.00);
		logToFile = combatTagSection.getBoolean("Punishments.Log To File", true);
		killOnLogout = combatTagSection.getBoolean("Punishments.Kill on Logout.Enabled", true);
		dropInventory = combatTagSection.getBoolean("Punishments.Kill on Logout.Player Drops.Inventory", true);
		dropExp = combatTagSection.getBoolean("Punishments.Kill on Logout.Player Drops.Experience", true);
		dropArmor = combatTagSection.getBoolean("Punishments.Kill on Logout.Player Drops.Armor", true);
		commandsOnPvPLog = getCommandList(combatTagSection.getStringList("Punishments.Commands On PvPLog"));

		itemCooldowns = new EnumMap<>(Material.class);
		for (final Entry<String, Object> e : c.getConfigurationSection("Item Cooldowns").getValues(false).entrySet()) {
			final Material material = Material.getMaterial(e.getKey().toUpperCase());
			if (material == null) {
				Log.warning("The material " + e.getKey()
						+ " in Item Cooldowns doesn't exist. You might have typed it incorrectly or it might not exist in this MC version");
				continue;
			}
			itemCooldowns.put(material, (Integer) e.getValue());
		}

		newbieProtectionEnabled = newbieSection.getBoolean("Enabled", true);
		newbieProtectionTime = newbieSection.getInt("Time(minutes)", 5);
		newbieAllowDisable = newbieSection.getBoolean("Allow Player Disable", true);
		blockPickNewbies = newbieSection.getBoolean("Block Pick Items", false);
		blockPlaceBlocksNewbie = newbieSection.getBoolean("Block Place Blocks", false);
		blockBreakBlocksNewbie = newbieSection.getBoolean("Block Break Blocks", false);
		newbieGodMode = newbieSection.getBoolean("Protect From Everything", false);
		newbieBlacklist = getList(newbieSection.getStringList("Command Blacklist"));

		killAbuseEnabled = killAbuseSection.getBoolean("Enabled", true);
		killAbuseMaxKills = killAbuseSection.getInt("Max Kills", 5);
		killAbuseTime = killAbuseSection.getInt("Time Limit", 60);
		killAbuseWarn = killAbuseSection.getBoolean("Warn Before", true);
		killAbuseCommands = getCommandList(killAbuseSection.getStringList("Commands on Abuse"));
		respawnProtection = killAbuseSection.getInt("Respawn Protection", 5);

		setMoneyReward(playerKillsSection.getDouble("Money Reward", 10));
		setMoneyPenalty(playerKillsSection.getDouble("Money Penalty", 10));
		moneySteal = playerKillsSection.getBoolean("Money Steal", false);
		expSteal = playerKillsSection.getDouble("Exp Steal", 0.0);
		commandsOnKill = getCommandList(playerKillsSection.getStringList("Commands On Kill"));
		playerKillsWGExclusions = new HashSet<>(getList(playerKillsSection.getStringList("WorldGuard Exclusions")));

		toggleCooldown = pvpToggleSection.getInt("Cooldown", 15);
		setToggleNametagsEnabled(pvpToggleSection.getBoolean("NameTags.Enabled", false));
		toggleColorOn = pvpToggleSection.getString("NameTags.Prefix On", "&1");
		toggleColorOff = pvpToggleSection.getString("NameTags.Prefix Off", "&2");
		pvpDisabledFee = pvpToggleSection.getInt("PvP Disabled Money Fee", 0);
		commandsPvPOn = getCommandList(pvpToggleSection.getStringList("Commands PvP On"));
		commandsPvPOff = getCommandList(pvpToggleSection.getStringList("Commands PvP Off"));
		worldguardOverrides = pvpToggleSection.getBoolean("WorldGuard Overrides", true);
		worldguardOverridesList = new HashSet<>(getList(pvpToggleSection.getStringList("WorldGuard Overrides Region List")));

		cooldownsxEnderpearlID = pluginHookSection.getString("CooldownsX.Enderpearl", "");

		useNameTag = !nameTagPrefix.isEmpty() || !nameTagSuffix.isEmpty() || toggleNametagsEnabled;
		Log.infoColor(ChatColor.GREEN + "Using player nametags: " + ChatColor.AQUA + useNameTag);

		checkUpdates = updateSection.getBoolean("Enabled", true);
		autoUpdate = updateSection.getBoolean("Auto Update", true);

		optOutMetrics = c.getBoolean("Metrics.Opt-out", false);

		harmfulPotions = new HashSet<>(c.getStringList("Harmful Potions"));
		Settings.setDEBUG(c.getBoolean("Debug Mode", false));
		configVersion = c.getInt("Config Version");
	}

	public static void helpMenu(final CommandSender player) {
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
		list.removeIf(string -> string.startsWith("example"));
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

	public static double getExpSteal() {
		return expSteal;
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

	public static boolean isKillAbuseWarn() {
		return killAbuseWarn;
	}

	public static Map<Material, Integer> getItemCooldowns() {
		return itemCooldowns;
	}

	public static boolean isHealthBelowName() {
		return healthBelowName;
	}

	@NotNull
	public static String getHealthBelowNameSymbol() {
		return healthBelowNameSymbol;
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
		Settings.debug = dEBUG;
		Log.setDebug(dEBUG);
	}

	public static boolean isDebug() {
		return Settings.debug;
	}

	public static Set<String> getWorldguardOverridesList() {
		return worldguardOverridesList;
	}

	public static int getPvPDisabledFee() {
		return pvpDisabledFee;
	}

	public static ConfigurationSection getPLUGINHOOKS() {
		return pluginHookSection;
	}
}
