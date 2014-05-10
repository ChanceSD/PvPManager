package me.NoChance.PvPManager.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Properties;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Managers.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Messages {

	private PvPManager plugin;
	private Properties lang = new Properties();
	private File messagesFile;
	public static String Error_PvPCommand_Disabled;
	public static String Error_Permission;
	public static String Error_PvP_Cooldown;
	public static String Already_Disabled;
	public static String Already_Enabled;
	public static String Other_Status_Enabled;
	public static String Others_Status_Disabled;
	public static String PvP_Disabled;
	public static String PvP_Enabled;
	public static String Self_Status_Disabled;
	public static String Self_Status_Enabled;
	public static String Command_Denied_InCombat;
	public static String Attack_Denied_You;
	public static String Attack_Denied_Other;
	public static String You_Are_InCombat;
	public static String Out_Of_Combat;
	public static String Newbie_Protection;
	public static String Newbie_Protection_End;
	public static String PvPLog_Broadcast;
	public static String Newbie_Protection_On_Hit;
	public static String Newbie_Protection_Atacker;
	public static String PvPToggle_On_Broadcast;
	public static String PvPToggle_Off_Broadcast;
	public static String EnderPearl_Blocked_InCombat;
	public static String Error_Command;
	private Locale locale;

	public Messages(PvPManager plugin) {
		this.plugin = plugin;
		try {
			this.locale = Locale.valueOf(Variables.locale);
		} catch (IllegalArgumentException e) {
			plugin.getLogger().warning("Error! Locale '" + Variables.locale + "' does not exist! Using default messages");
			locale = Locale.EN;
		}
		load();
	}

	public void load() {
		this.messagesFile = new File(plugin.getDataFolder(), locale.toString());
		if (!messagesFile.exists()) {
			InputStream input = plugin.getResource("locale/" + locale.toString());
			OutputStream resStreamOut;
			int readBytes;
			byte[] buffer = new byte[4096];
			try {
				resStreamOut = new FileOutputStream(new File(plugin.getDataFolder() + File.separator + locale.toString()));
				while ((readBytes = input.read(buffer)) != -1) {
					resStreamOut.write(buffer, 0, readBytes);
				}
				resStreamOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			plugin.getLogger().info("New Messages File Created Successfully!");
		}
		for (File file : plugin.getDataFolder().listFiles()) {
			String fileName = file.getName();
			if (fileName.contains("messages") && !fileName.equalsIgnoreCase(locale.toString()))
				file.delete();
		}
		try {
			if (messagesFile.exists()) {
				FileInputStream in = new FileInputStream(messagesFile);
				lang.load(in);
				checkChanges();
				getMessages();
				in.close();
			}
		} catch (IOException e) {
		}
	}

	public String getString(String key) {
		String message = null;
		try {
			message = new String(lang.getProperty(key).getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return "Encoding error! Please report to the developer";
		}
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public void getMessages() {
		Already_Disabled = getString("Already_Disabled");
		Already_Enabled = getString("Already_Enabled");
		Other_Status_Enabled = getString("Other_Status_Enabled");
		Others_Status_Disabled = getString("Others_Status_Disabled");
		PvP_Disabled = getString("PvP_Disabled");
		PvP_Enabled = getString("PvP_Enabled");
		Self_Status_Disabled = getString("Self_Status_Disabled");
		Self_Status_Enabled = getString("Self_Status_Enabled");
		Command_Denied_InCombat = getString("Command_Denied_InCombat");
		Attack_Denied_You = getString("Attack_Denied_You");
		Attack_Denied_Other = getString("Attack_Denied_Other");
		You_Are_InCombat = getString("You_Are_InCombat");
		Out_Of_Combat = getString("Out_Of_Combat");
		Newbie_Protection = getString("Newbie_Protection");
		Newbie_Protection_End = getString("Newbie_Protection_End");
		PvPLog_Broadcast = getString("PvPLog_Broadcast");
		Newbie_Protection_On_Hit = getString("Newbie_Protection_On_Hit");
		Newbie_Protection_Atacker = getString("Newbie_Protection_Atacker");
		PvPToggle_On_Broadcast = getString("PvPToggle_On_Broadcast");
		PvPToggle_Off_Broadcast = getString("PvPToggle_Off_Broadcast");
		EnderPearl_Blocked_InCombat = getString("EnderPearl_Blocked_InCombat");
		Error_Command = getString("Error_Command");
		Error_PvP_Cooldown = getString("Error_PvP_Cooldown");
		Error_Permission = getString("Error_Permission");
		Error_PvPCommand_Disabled = getString("Error_PvPCommand_Disabled");
	}

	public void checkChanges() {
		Properties original = new Properties();
		try {
			original.load(plugin.getResource("locale/" + locale.toString()));
			Enumeration<Object> originalKeys = original.keys();
			while (originalKeys.hasMoreElements()) {
				String a = (String) originalKeys.nextElement();
				if (!lang.containsKey(a)) {
					addMessage(a + " = " + new String(original.getProperty(a).getBytes("ISO-8859-1"), "UTF-8"));
					lang.setProperty(a, original.getProperty(a));
				}
			}
		} catch (IOException e) {
		}
	}

	public void addMessage(String a) {
		try {
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(messagesFile, true), "UTF-8"));
			pw.println(a);
			pw.close();
		} catch (IOException e) {
		}
	}

	public static void updateMessage(Player player) {
		player.sendMessage("§6[§fPvPManager§6] " + "§2An update is available: §e" + Variables.newVersion);
		player.sendMessage("§6[§fPvPManager§6] " + "§2Your current version is: §ePvPManager v" + Variables.currentVersion);
		player.sendMessage("§2Go to this page to download the latest version:");
		player.sendMessage("§2Link: §ehttp://dev.bukkit.org/bukkit-plugins/pvpmanager/");
		player.sendMessage("§2Use §e/pm update §2to update automatically");
	}

	public static void configUpdated(Player player) {
		player.sendMessage("§6[§fPvPManager§6] " + "§2Configuration file was updated to version §e" + ConfigManager.configVersion);
		player.sendMessage("§6[§fPvPManager§6] " + "§2It's recommended that you check for changes and adjust the file to your liking");
	}
}
