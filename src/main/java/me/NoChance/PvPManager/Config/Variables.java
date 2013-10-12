package me.NoChance.PvPManager.Config;

import java.util.List;

import me.NoChance.PvPManager.PvPManager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Variables {

	private PvPManager plugin;

	public static boolean inCombatEnabled;
	public static int timeInCombat;
	public static boolean stopCommands;
	public static boolean punishmentsEnabled;
	public static boolean keepItems;
	public static boolean keepExp;
	public static boolean killOnLogout;
	public static List<String> worldsExcluded;
	public static boolean disableFly;
	public static boolean pvpTimerEnabled;
	public static boolean toggleSignsEnabled;
	public static boolean disableToggleCommand;
	public static boolean onlyTagAttacker;
	public static boolean updateCheck;
	public static boolean newbieProtectionEnabled;
	public static int newbieProtectionTime;
	public static String pvpOffSound;
	public static String pvpOnSound;
	public static boolean enableSound;
	public static boolean announcePvpOnWorldChange;
	public static boolean broadcastPvpLog;

	public Variables(PvPManager plugin) {
		this.plugin = plugin;
		InitialiseVariables();
	}

	public void InitialiseVariables() {
		inCombatEnabled = getBoolean("In Combat.Enabled");
		timeInCombat = getInt("In Combat.Time(seconds)");
		stopCommands = getBoolean("In Combat.Stop Commands");
		punishmentsEnabled = getBoolean("In Combat.Punishments.Enabled");
		keepItems = getBoolean("In Combat.Punishments.Kill on Logout.Keep Items");
		keepExp = getBoolean("In Combat.Punishments.Kill on Logout.Keep Exp");
		killOnLogout = getBoolean("In Combat.Punishments.Kill on Logout.Enabled");
		worldsExcluded = getStringList("World Exclusions");
		disableFly = getBoolean("In Combat.Disable Fly");
		pvpTimerEnabled = getBoolean("PvP Timer.Enabled");
		toggleSignsEnabled = getBoolean("Toggle Signs.Enabled");
		disableToggleCommand = getBoolean("Toggle Signs.Disable Toggle Command");
		onlyTagAttacker = getBoolean("In Combat.Only Tag Attacker");
		updateCheck = getBoolean("Update Check");
		newbieProtectionEnabled = getBoolean("Newbie Protection.Enabled");
		newbieProtectionTime = getInt("Newbie Protection.Time(minutes)");
		pvpOffSound = getString("PvP Timer.Sound.PvP Off Sound");
		pvpOnSound = getString("PvP Timer.Sound.PvP On Sound");
		enableSound = getBoolean("PvP Timer.Sound.Enabled");
		announcePvpOnWorldChange = getBoolean("PvP Timer.Announce On World Change");
		broadcastPvpLog = getBoolean("In Combat.Punishments.Broadcast PvPLog");
	}

	public boolean getBoolean(String a) {
		return plugin.getConfig().getBoolean(a);
	}

	public int getInt(String a) {
		return plugin.getConfig().getInt(a);
	}
	
	public List<String> getStringList(String a){
		return plugin.getConfig().getStringList(a);
	}
	
	public String getString(String a){
		return plugin.getConfig().getString(a);
	}
	
	public void helpMenu(Player player){
		player.sendMessage(ChatColor.GOLD
				+ "-------------- PvPManager Help Page --------------");
		player.sendMessage(ChatColor.GOLD + "/pvp "
				+ ChatColor.WHITE + "| Set PvP Enabled or Disabled.");
		player.sendMessage(ChatColor.GOLD + "/pvp status "
				+ ChatColor.WHITE + "| Check What is your PvP Status.");
		player.sendMessage(ChatColor.GOLD + "/pvp status <player> "
				+ ChatColor.WHITE + "| Check Another Player PvP Status.");
		player.sendMessage(ChatColor.GOLD + "/pm "
				+ ChatColor.WHITE + "| Show This Help Page");
		player.sendMessage(ChatColor.GOLD + "/pm reload "
				+ ChatColor.WHITE + "| Reload PvPManager");
		player.sendMessage(ChatColor.GOLD + "/pm pvpstart <time> [world] "
				+ ChatColor.WHITE + "| Change the time PvP Starts in a world");
		player.sendMessage(ChatColor.GOLD + "/pm pvpend <time> [world] "
				+ ChatColor.WHITE + "| Change the time PvP Ends in a world");
		player.sendMessage(ChatColor.GOLD
				+ "-------------------------------------------------");
	}
	
}
