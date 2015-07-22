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
import me.NoChance.PvPManager.Utils.Log;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Messages {

	private static PvPManager plugin;
	private static final Properties LANG = new Properties();
	private static File messagesFile;
	private static String errorPermission;
	private static String errorPvpCooldown;
	private static String alreadyDisabled;
	private static String alreadyEnabled;
	private static String otherStatusEnabled;
	private static String othersStatusDisabled;
	private static String pvpDisabled;
	private static String pvpEnabled;
	private static String selfStatusDisabled;
	private static String selfStatusEnabled;
	private static String commandDeniedIncombat;
	private static String attackDeniedYou;
	private static String attackDeniedOther;
	private static String taggedAttacker;
	private static String taggedDefender;
	private static String outOfCombat;
	private static String newbieProtection;
	private static String newbieProtectionEnd;
	private static String pvplogBroadcast;
	private static String newbieProtectionOnHit;
	private static String newbieProtectionAtacker;
	private static String pvptoggleOnBroadcast;
	private static String pvptoggleOffBroadcast;
	private static String enderpearlBlockedIncombat;
	private static String errorCommand;
	private static String currentVersion;
	private static String newVersion;
	private static String moneyReward;
	private static String moneyPenalty;
	private static Locale locale;

	public static void setup(final PvPManager plugin) {
		Messages.plugin = plugin;
		currentVersion = plugin.getDescription().getVersion();
		try {
			locale = Locale.valueOf(Variables.getLocale());
		} catch (final IllegalArgumentException e) {
			Log.warning("Error! Locale '" + Variables.getLocale() + "' does not exist! Using default messages");
			locale = Locale.EN;
		}
		load();
	}

	private static void load() {
		messagesFile = new File(plugin.getDataFolder(), locale.toString());
		if (!messagesFile.exists()) {
			int readBytes;
			final byte[] buffer = new byte[4096];
			try (InputStream input = plugin.getResource("locale/" + locale.toString());
					OutputStream resStreamOut = new FileOutputStream(new File(plugin.getDataFolder() + File.separator + locale.toString()))) {
				while ((readBytes = input.read(buffer)) != -1) {
					resStreamOut.write(buffer, 0, readBytes);
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
			Log.info("New Messages File Created Successfully!");
		}
		File[] listFiles = plugin.getDataFolder().listFiles();
		if (listFiles != null)
			for (final File file : listFiles) {
				final String fileName = file.getName();
				if (fileName.contains("messages") && !fileName.equalsIgnoreCase(locale.toString()))
					file.delete();
			}
		try (FileInputStream in = new FileInputStream(messagesFile)) {
			if (messagesFile.exists()) {
				LANG.load(in);
				checkChanges();
				getMessages();
			}
		} catch (final IOException e) {
		}
	}

	private static String getString(final String key) {
		String message = null;
		try {
			message = new String(LANG.getProperty(key).getBytes("ISO-8859-1"), "UTF-8");
		} catch (final UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return "Encoding error! Please report to the developer";
		}
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	private static void getMessages() {
		alreadyDisabled = getString("Already_Disabled");
		alreadyEnabled = getString("Already_Enabled");
		otherStatusEnabled = getString("Other_Status_Enabled");
		othersStatusDisabled = getString("Others_Status_Disabled");
		pvpDisabled = getString("PvP_Disabled");
		pvpEnabled = getString("PvP_Enabled");
		selfStatusDisabled = getString("Self_Status_Disabled");
		selfStatusEnabled = getString("Self_Status_Enabled");
		commandDeniedIncombat = getString("Command_Denied_InCombat");
		attackDeniedYou = getString("Attack_Denied_You");
		attackDeniedOther = getString("Attack_Denied_Other");
		taggedAttacker = getString("Tagged_Attacker");
		taggedDefender = getString("Tagged_Defender");
		outOfCombat = getString("Out_Of_Combat");
		newbieProtection = getString("Newbie_Protection");
		newbieProtectionEnd = getString("Newbie_Protection_End");
		pvplogBroadcast = getString("PvPLog_Broadcast");
		newbieProtectionOnHit = getString("Newbie_Protection_On_Hit");
		newbieProtectionAtacker = getString("Newbie_Protection_Atacker");
		pvptoggleOnBroadcast = getString("PvPToggle_On_Broadcast");
		pvptoggleOffBroadcast = getString("PvPToggle_Off_Broadcast");
		enderpearlBlockedIncombat = getString("EnderPearl_Blocked_InCombat");
		errorCommand = getString("Error_Command");
		errorPvpCooldown = getString("Error_PvP_Cooldown");
		errorPermission = getString("Error_Permission");
		moneyReward = getString("Money_Reward");
		moneyPenalty = getString("Money_Penalty");
	}

	private static void checkChanges() {
		final Properties original = new Properties();
		try {
			original.load(plugin.getResource("locale/" + locale.toString()));
			final Enumeration<Object> originalKeys = original.keys();
			while (originalKeys.hasMoreElements()) {
				final String a = (String) originalKeys.nextElement();
				if (!LANG.containsKey(a)) {
					addMessage(a + " = " + new String(original.getProperty(a).getBytes("ISO-8859-1"), "UTF-8"));
					LANG.setProperty(a, original.getProperty(a));
				}
			}
		} catch (final IOException e) {
		}
	}

	private static void addMessage(final String a) {
		try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(messagesFile, true), "UTF-8"))) {
			pw.println(a);
		} catch (final IOException e) {
		}
	}

	public static void updateMessage(final Player player) {
		player.sendMessage("§6[§fPvPManager§6] " + "§2An update is available: §e" + getNewVersion());
		player.sendMessage("§6[§fPvPManager§6] " + "§2Your current version is: §ePvPManager v" + currentVersion);
		player.sendMessage("§2Go to this page to download the latest version:");
		player.sendMessage("§2Link: §ehttp://dev.bukkit.org/bukkit-plugins/pvpmanager/");
		player.sendMessage("§2Use §e/pm update §2to update automatically");
	}

	public static void configUpdated(final Player player) {
		player.sendMessage("§6[§fPvPManager§6] " + "§2Configuration file was updated to version §e" + plugin.getConfigM().getConfigVersion());
		player.sendMessage("§6[§fPvPManager§6] " + "§2It's recommended that you check for changes and adjust the file to your liking");
	}

	public static String getErrorPermission() {
		return errorPermission;
	}

	public static String getErrorPvpCooldown() {
		return errorPvpCooldown;
	}

	public static String getAlreadyDisabled() {
		return alreadyDisabled;
	}

	public static String getAlreadyEnabled() {
		return alreadyEnabled;
	}

	public static String getOtherStatusEnabled() {
		return otherStatusEnabled;
	}

	public static String getOthersStatusDisabled() {
		return othersStatusDisabled;
	}

	public static String getPvpDisabled() {
		return pvpDisabled;
	}

	public static String getPvpEnabled() {
		return pvpEnabled;
	}

	public static String getSelfStatusDisabled() {
		return selfStatusDisabled;
	}

	public static String getSelfStatusEnabled() {
		return selfStatusEnabled;
	}

	public static String getCommandDeniedIncombat() {
		return commandDeniedIncombat;
	}

	public static String pvpDisabled() {
		return attackDeniedYou;
	}

	public static String pvpDisabledOther(final String name) {
		return attackDeniedOther.replace("%p", name);
	}

	public static String getTaggedAttacker() {
		return taggedAttacker;
	}

	public static String getTaggedDefender() {
		return taggedDefender;
	}

	public static String getOutOfCombat() {
		return outOfCombat;
	}

	public static String getNewbieProtection() {
		return newbieProtection;
	}

	public static String getNewbieProtectionEnd() {
		return newbieProtectionEnd;
	}

	public static String getPvplogBroadcast() {
		return pvplogBroadcast;
	}

	public static String newbieBlocked() {
		return newbieProtectionOnHit;
	}

	public static String newbieBlockedOther(final String name) {
		return newbieProtectionAtacker.replace("%p", name);
	}

	public static String getPvptoggleOnBroadcast() {
		return pvptoggleOnBroadcast;
	}

	public static String getPvptoggleOffBroadcast() {
		return pvptoggleOffBroadcast;
	}

	public static String getEnderpearlBlockedIncombat() {
		return enderpearlBlockedIncombat;
	}

	public static String getErrorCommand() {
		return errorCommand;
	}

	public static String getCurrentversion() {
		return currentVersion;
	}

	public static String getNewversion() {
		return getNewVersion();
	}

	public static String getMoneyReward() {
		return moneyReward;
	}

	public static String getMoneyPenalty() {
		return moneyPenalty;
	}

	public final Locale getLocale() {
		return locale;
	}

	public static String getNewVersion() {
		return newVersion;
	}

	public static void setNewVersion(final String newVersion) {
		Messages.newVersion = newVersion;
	}
}
