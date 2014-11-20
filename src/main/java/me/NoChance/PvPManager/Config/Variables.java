package me.NoChance.PvPManager.Config;

import java.util.Arrays;
import java.util.List;

import me.NoChance.PvPManager.Managers.ConfigManager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Variables {

	private ConfigManager cm;
	public static boolean inCombatEnabled;
	public static int timeInCombat;
	public static boolean stopCommands;
	public static boolean punishmentsEnabled;
	public static boolean dropInventory;
	public static boolean dropExp;
	public static boolean dropArmor;
	public static boolean killOnLogout;
	public static List<String> worldsExcluded = Arrays.asList(new String[] { "Example", "Example2" });
	public static boolean disableFly;
	public static boolean toggleSignsEnabled;
	public static boolean disableToggleCommand;
	public static boolean onlyTagAttacker;
	public static boolean updateCheck;
	public static boolean autoUpdate;
	public static boolean newbieProtectionEnabled;
	public static int newbieProtectionTime;
	public static boolean broadcastPvpLog;
	public static boolean fineEnabled;
	public static double fineAmount;
	public static boolean pvpBlood;
	public static boolean disableGamemode;
	public static boolean update = false;
	public static List<String> commandsAllowed = Arrays.asList(new String[] { "tell", "money" });
	public static int toggleCooldown;
	public static boolean defaultPvp;
	public static boolean disableDisguise;
	public static boolean killAbuseEnabled;
	public static int killAbuseMaxKills;
	public static int killAbuseTime;
	public static List<String> killAbuseCommands = Arrays.asList("kick <player> Kill Abuse Is Not Allowed!");
	public static boolean toggleBroadcast;
	public static boolean toggleOffOnDeath;
	public static boolean blockEnderPearl;
	public static boolean autoSoupEnabled;
	public static double soupHealth;
	public static boolean inCombatSilent;
	public static boolean configUpdated;
	public static boolean stopBorderHopping;
	public static boolean ignoreNoDamageHits;
	public static String nameTagColor;
	public static boolean useNameTag;
	public static String locale;
	public static int respawnProtection;
	public static double moneyReward;
	public static boolean commandsOnKillEnabled;
	public static List<String> commandsOnKill = Arrays.asList("heal <player>");
	public static boolean disableInvisibility;
	public static boolean toggleNametagsEnabled;
	public static String toggleColorOn;
	public static String toggleColorOff;
	public static boolean logToFile;
	public static boolean transferDrops;
	public static double moneyPenalty;
	public static boolean blockPickNewbies;

	public Variables(ConfigManager configM) {
		this.cm = configM;
		InitialiseVariables();
	}

	@SuppressWarnings("unchecked")
	private void InitialiseVariables() {
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
		fineEnabled = cm.getConfig().getBoolean("In Combat.Punishments.Fine.Enabled", false);
		fineAmount = cm.getConfig().getDouble("In Combat.Punishments.Fine.Amount", 10.00);
		pvpBlood = cm.getConfig().getBoolean("PvP Blood", true);
		disableGamemode = cm.getConfig().getBoolean("Disable GameMode", true);
		commandsAllowed = (List<String>) cm.getConfig().getList("In Combat.Stop Commands.Allowed Commands", commandsAllowed);
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
	}

	public static void helpMenu(Player player) {
		player.sendMessage(ChatColor.GOLD + "-------------- PvPManager Help Page --------------");
		player.sendMessage(ChatColor.GOLD + "/pvp " + ChatColor.WHITE + "| Set PvP Enabled or Disabled.");
		player.sendMessage(ChatColor.GOLD + "/pvp info " + ChatColor.WHITE + "| Check Your Player Info");
		player.sendMessage(ChatColor.GOLD + "/pvp status " + ChatColor.WHITE + "| Check What is your PvP Status.");
		player.sendMessage(ChatColor.GOLD + "/pvp status <player> " + ChatColor.WHITE + "| Check Another Player PvP Status.");
		player.sendMessage(ChatColor.GOLD + "/pvpo " + ChatColor.WHITE + "| Override all PvP protections");
		player.sendMessage(ChatColor.GOLD + "/pvp disable protection " + ChatColor.WHITE + "| Disable Newbie Protection");
		player.sendMessage(ChatColor.GOLD + "/pm " + ChatColor.WHITE + "| Show This Help Page");
		player.sendMessage(ChatColor.GOLD + "/pm update " + ChatColor.WHITE + "| Update to Latest Version");
		player.sendMessage(ChatColor.GOLD + "/pm reload " + ChatColor.WHITE + "| Reload PvPManager");
		player.sendMessage(ChatColor.GOLD + "-------------------------------------------------");
	}
}
