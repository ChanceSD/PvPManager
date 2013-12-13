package me.NoChance.PvPManager.Config;

import java.util.ArrayList;
import java.util.List;
import me.NoChance.PvPManager.Managers.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Variables {

	private ConfigManager configManager;
	public static boolean inCombatEnabled;
	public static int timeInCombat;
	public static boolean stopCommands;
	public static boolean punishmentsEnabled;
	public static boolean dropInventory;
	public static boolean dropExp;
	public static boolean dropArmor;
	public static boolean killOnLogout;
	public static List<String> worldsExcluded = new ArrayList<String>();
	public static boolean disableFly;
	public static boolean pvpTimerEnabled;
	public static boolean toggleSignsEnabled;
	public static boolean disableToggleCommand;
	public static boolean onlyTagAttacker;
	public static boolean updateCheck;
	public static boolean autoUpdate;
	public static boolean newbieProtectionEnabled;
	public static int newbieProtectionTime;
	public static String pvpOffSound;
	public static String pvpOnSound;
	public static boolean enableSound;
	public static boolean announcePvpOnWorldChange;
	public static boolean broadcastPvpLog;
	public static boolean fineEnabled;
	public static double fineAmount;
	public static boolean pvpBlood;
	public static boolean disableGamemode;
	public static boolean update = false;
	public static String newVersion;
	public static String currentVersion;
	public static List<String> commandsAllowed = new ArrayList<String>();

	public Variables(ConfigManager configM) {
		this.configManager = configM;
		InitialiseVariables();
	}

	@SuppressWarnings("unchecked")
	private void InitialiseVariables() {
		inCombatEnabled = configManager.getConfig().getBoolean("In Combat.Enabled", true);
		timeInCombat = configManager.getConfig().getInt("In Combat.Time(seconds)", 15);
		stopCommands = configManager.getConfig().getBoolean("In Combat.Stop Commands.Enabled", true);
		punishmentsEnabled = configManager.getConfig().getBoolean("In Combat.Punishments.Enabled", true);
		dropInventory = configManager.getConfig().getBoolean("In Combat.Punishments.Drops.Inventory", true);
		dropExp = configManager.getConfig().getBoolean("In Combat.Punishments.Drops.Experience", true);
		dropArmor = configManager.getConfig().getBoolean("In Combat.Punishments.Drops.Armor", true);
		killOnLogout = configManager.getConfig().getBoolean("In Combat.Punishments.Kill on Logout", true);
		worldsExcluded = (List<String>) configManager.getConfig().getList("World Exclusions", worldsExcluded);
		disableFly = configManager.getConfig().getBoolean("In Combat.Disable Fly", true);
		pvpTimerEnabled = configManager.getConfig().getBoolean("PvP Timer.Enabled", true);
		toggleSignsEnabled = configManager.getConfig().getBoolean("Toggle Signs.Enabled", true);
		disableToggleCommand = configManager.getConfig().getBoolean("Toggle Signs.Disable Toggle Command", false);
		onlyTagAttacker = configManager.getConfig().getBoolean("In Combat.Only Tag Attacker", false);
		updateCheck = configManager.getConfig().getBoolean("Update Check.Enabled", true);
		autoUpdate = configManager.getConfig().getBoolean("Update Check.Auto Update", true);
		newbieProtectionEnabled = configManager.getConfig().getBoolean("Newbie Protection.Enabled", true);
		newbieProtectionTime = configManager.getConfig().getInt("Newbie Protection.Time(minutes)", 5);
		pvpOffSound = configManager.getConfig().getString("PvP Timer.Sound.PvP Off Sound");
		pvpOnSound = configManager.getConfig().getString("PvP Timer.Sound.PvP On Sound");
		enableSound = configManager.getConfig().getBoolean("PvP Timer.Sound.Enabled");
		announcePvpOnWorldChange = configManager.getConfig().getBoolean("PvP Timer.Announce On World Change");
		broadcastPvpLog = configManager.getConfig().getBoolean("In Combat.Punishments.Broadcast PvPLog", true);
		fineEnabled = configManager.getConfig().getBoolean("In Combat.Punishments.Fine.Enabled", true);
		fineAmount = configManager.getConfig().getDouble("In Combat.Punishments.Fine.Amount", 10.00);
		pvpBlood = configManager.getConfig().getBoolean("PvP Blood", true);
		disableGamemode = configManager.getConfig().getBoolean("In Combat.Disable GameMode", true);
		commandsAllowed = (List<String>) configManager.getConfig().getList("In Combat.Stop Commands.Allowed Commands", commandsAllowed);
		currentVersion = configManager.getPlugin().getDescription().getVersion();
	}

	public static void helpMenu(Player player) {
		player.sendMessage(ChatColor.GOLD + "-------------- PvPManager Help Page --------------");
		player.sendMessage(ChatColor.GOLD + "/pvp " + ChatColor.WHITE + "| Set PvP Enabled or Disabled.");
		player.sendMessage(ChatColor.GOLD + "/pvp status " + ChatColor.WHITE + "| Check What is your PvP Status.");
		player.sendMessage(ChatColor.GOLD + "/pvp status <player> " + ChatColor.WHITE + "| Check Another Player PvP Status.");
		player.sendMessage(ChatColor.GOLD + "/pvp disable protection " + ChatColor.WHITE + "| Disable Newbie Protection Before Expiration");
		player.sendMessage(ChatColor.GOLD + "/pm " + ChatColor.WHITE + "| Show This Help Page");
		player.sendMessage(ChatColor.GOLD + "/pm update " + ChatColor.WHITE + "| Update to Latest Version");
		player.sendMessage(ChatColor.GOLD + "/pm reload " + ChatColor.WHITE + "| Reload PvPManager");
		player.sendMessage(ChatColor.GOLD + "/pm pvpstart <time> [world] " + ChatColor.WHITE + "| Change the time PvP Starts in a world");
		player.sendMessage(ChatColor.GOLD + "/pm pvpend <time> [world] " + ChatColor.WHITE + "| Change the time PvP Ends in a world");
		player.sendMessage(ChatColor.GOLD + "-------------------------------------------------");
	}

}
