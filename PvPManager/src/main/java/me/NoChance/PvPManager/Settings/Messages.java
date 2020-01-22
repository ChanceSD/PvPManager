package me.NoChance.PvPManager.Settings;

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
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

import org.bukkit.ChatColor;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Utils.Log;

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
	private static String newbieProtectionOnHit;
	private static String newbieProtectionAttacker;
	private static String enderpearlBlockedIncombat;
	private static String errorCommand;
	private static String currentVersion;
	private static String moneyReward;
	private static String moneyPenalty;
	private static String moneySteal;
	private static Locale locale;
	private static final Queue<String> messageQueue = new LinkedList<>();
	private static String newVersion;
	private static String pvpListNoResults;
	private static String errorPlayerNotFound;
	private static String errorNotNewbie;
	private static String newbieTimeCheck;
	private static String newbieProtectionRemoved;

	public static void setup(final PvPManager plugin) {
		Messages.plugin = plugin;
		currentVersion = plugin.getDescription().getVersion();
		try {
			locale = Locale.valueOf(Settings.getLocale());
		} catch (final IllegalArgumentException e) {
			Log.warning("Error! Locale '" + Settings.getLocale() + "' does not exist! Using default messages");
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
		final File[] listFiles = plugin.getDataFolder().listFiles();
		if (listFiles != null) {
			for (final File file : listFiles) {
				final String fileName = file.getName();
				if (fileName.contains("messages") && !fileName.equalsIgnoreCase(locale.toString())) {
					file.delete();
				}
			}
		}
		try (FileInputStream in = new FileInputStream(messagesFile)) {
			if (messagesFile.exists()) {
				LANG.load(in);
				checkChanges();
				getMessages();
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private static String getString(final String key) {
		String message;
		try {
			message = new String(LANG.getProperty(key).getBytes("ISO-8859-1"), "UTF-8");
		} catch (final UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return "Encoding error! Please report this bug!";
		}
		return colorize(message);
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
		newbieProtectionRemoved = getString("Newbie_Protection_Removed");
		newbieProtectionOnHit = getString("Newbie_Protection_On_Hit");
		newbieProtectionAttacker = getString("Newbie_Protection_Atacker");
		newbieTimeCheck = getString("Newbie_Time_Check");
		enderpearlBlockedIncombat = getString("EnderPearl_Blocked_InCombat");
		errorCommand = getString("Error_Command");
		errorNotNewbie = getString("Error_Not_Newbie");
		errorPvpCooldown = getString("Error_PvP_Cooldown");
		errorPermission = getString("Error_Permission");
		moneyReward = getString("Money_Reward");
		moneyPenalty = getString("Money_Penalty");
		moneySteal = getString("Money_Steal");
		pvpListNoResults = getString("PvPList_Nothing_Found");
		errorPlayerNotFound = getString("Error_Player_Not_Found");
	}

	private static void checkChanges() {
		final Properties original = new Properties();
		try {
			original.load(plugin.getResource("locale/" + Locale.EN.toString()));
			final Enumeration<Object> originalKeys = original.keys();
			while (originalKeys.hasMoreElements()) {
				final String a = (String) originalKeys.nextElement();
				if (!LANG.containsKey(a)) {
					Log.info("Added missing '" + a + "' key to messages file.");
					addMessage(a + " = " + new String(original.getProperty(a).getBytes("ISO-8859-1"), "UTF-8"));
					LANG.setProperty(a, original.getProperty(a));
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private static void addMessage(final String a) {
		try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(messagesFile, true), "UTF-8"))) {
			pw.println(a);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static String colorize(final String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public static String getErrorPlayerNotFound() {
		return errorPlayerNotFound;
	}

	public static String getPvpListNoResults() {
		return pvpListNoResults;
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

	public static String newbieBlocked() {
		return newbieProtectionOnHit;
	}

	public static String newbieBlockedOther(final String name) {
		return newbieProtectionAttacker.replace("%p", name);
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

	public static String getMoneyReward() {
		return moneyReward;
	}

	public static String getMoneyPenalty() {
		return moneyPenalty;
	}

	public static String getMoneySteal() {
		return moneySteal;
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

	public static Queue<String> getMessageQueue() {
		return messageQueue;
	}

	public static String getErrorNotNewbie() {
		return errorNotNewbie;
	}

	public static String getNewbieTimeCheck() {
		return newbieTimeCheck;
	}

	public static String getNewbieProtectionRemoved() {
		return newbieProtectionRemoved;
	}

}
