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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Player.CancelResult;
import me.NoChance.PvPManager.Utils.ChatUtils;
import me.chancesd.sdutils.utils.Log;

public class Messages {

	private static PvPManager plugin;
	private static final Properties LANG = new Properties();
	private static final Queue<String> messageQueue = new LinkedList<>();
	public static final String PREFIXMSG = "§c[§8PvPManager§c]";
	private static File messagesFile;
	private static String errorPermission;
	private static String errorPvpCooldown;
	private static String errorPvpToggleNoPvp;
	private static String errorPvpToggleForcePvp;
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
	private static String taggedAttackerActionbar;
	private static String taggedDefenderActionbar;
	private static String outOfCombatActionbar;
	private static String newbieProtection;
	private static String newbieProtectionEnd;
	private static String newbieProtectionOnHit;
	private static String newbieProtectionAttacker;
	private static String newbieCommandBlocked;
	private static String enderpearlBlockedIncombat;
	private static String chorusBlockedInCombat;
	private static String totemBlockedInCombat;
	private static String inventoryBlockedInCombat;
	private static String pushbackWarning;
	private static String errorCommand;
	private static String currentVersion;
	private static String moneyReward;
	private static String moneyPenalty;
	private static String moneySteal;
	private static Locale locale;
	private static String tagTimeLeft;
	private static String tagNotInCombat;
	private static String newVersion;
	private static String pvpListTitle;
	private static String pvpListEnabled;
	private static String pvpListDisabled;
	private static String pvpListNoResults;
	private static String pvpToggleAdminChanged;
	private static String pvpToggleAlreadyDisabled;
	private static String pvpToggleAlreadyEnabled;
	private static String errorPlayerNotFound;
	private static String errorNotNewbie;
	private static String errorNotPlayer;
	private static String newbieTimeCheck;
	private static String newbieTimeCheckOther;
	private static String newbieProtectionRemoved;
	private static String newbieForceRemovedWG;
	private static String respawnProtectionSelf;
	private static String respawnProtectionOther;
	private static String worldProtection;
	private static String afkProtection;
	private static String globalProtection;
	private static String pvpForceEnabledWG;
	private static String teleportBlockedInCombat;
	private static String blockPlaceBlockedInCombat;
	private static String blockBreakBlockedInCombat;
	private static String eatBlockedInCombat;
	private static String interactBlockedInCombat;
	private static String newbiePickupItemBlocked;
	private static String enabled;
	private static String disabled;
	private static String pvpDisabledFee;
	private static String pvpFeeNotEnough;

	public static void setup(final PvPManager plugin) {
		Messages.plugin = plugin;
		currentVersion = plugin.getDescription().getVersion();
		messageQueue.clear();
		try {
			locale = Locale.valueOf(Settings.getLocale());
		} catch (final IllegalArgumentException e) {
			Log.warning("Error! Locale '" + Settings.getLocale() + "' does not exist! Using default messages");
			locale = Locale.EN;
		}
		load();
	}

	private static void load() {
		messagesFile = new File(plugin.getDataFolder(), locale.fileName());
		if (!messagesFile.exists()) {
			int readBytes;
			final byte[] buffer = new byte[4096];
			try (InputStream input = plugin.getResource("locale/" + locale.fileName());
					OutputStream resStreamOut = new FileOutputStream(new File(plugin.getDataFolder() + File.separator + locale.fileName()))) {
				while ((readBytes = input.read(buffer)) != -1) {
					resStreamOut.write(buffer, 0, readBytes);
				}
			} catch (final IOException e) {
				Log.severe("Error reading default locale from jar", e);
			}
			Log.infoColor(ChatColor.DARK_GREEN + "New messages file created successfully!");
		}
		final File[] listFiles = plugin.getDataFolder().listFiles();
		if (listFiles != null) {
			for (final File file : listFiles) {
				final String fileName = file.getName();
				if (fileName.contains("messages") && !fileName.equalsIgnoreCase(locale.fileName())) {
					file.delete();
				}
			}
		}
		try (FileInputStream in = new FileInputStream(messagesFile)) {
			if (messagesFile.exists()) {
				LANG.clear();
				LANG.load(in);
				checkChanges();
				getMessages();
			}
		} catch (final IOException e) {
			Log.severe("Error reading locale file", e);
		}
	}

	@NotNull
	public static String getString(final String key) {
		String message;
		try {
			message = new String(LANG.getProperty(key).getBytes("ISO-8859-1"), "UTF-8");
		} catch (final UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return "Encoding error! Please report this bug!";
		}
		return ChatUtils.colorize(message);
	}

	private static void getMessages() {
		otherStatusEnabled = getString("Other_Status_Enabled");
		othersStatusDisabled = getString("Others_Status_Disabled");
		pvpDisabled = getString("PvP_Disabled");
		pvpEnabled = getString("PvP_Enabled");
		selfStatusDisabled = getString("Self_Status_Disabled");
		selfStatusEnabled = getString("Self_Status_Enabled");
		commandDeniedIncombat = getString("Command_Denied_InCombat");
		attackDeniedYou = getString("Attack_Denied_You");
		attackDeniedOther = getString("Attack_Denied_Other");
		pvpDisabledFee = getString("PvP_Disabled_Fee");
		pvpFeeNotEnough = getString("PvP_Disabled_Fee_Not_Enough");
		taggedAttacker = getString("Tagged_Attacker");
		taggedAttackerActionbar = getString("Tagged_Attacker_ActionBar");
		taggedDefender = getString("Tagged_Defender");
		taggedDefenderActionbar = getString("Tagged_Defender_ActionBar");
		outOfCombat = getString("Out_Of_Combat");
		outOfCombatActionbar = getString("Out_Of_Combat_ActionBar");
		newbieProtection = getString("Newbie_Protection");
		newbieProtectionEnd = getString("Newbie_Protection_End");
		newbieProtectionRemoved = getString("Newbie_Protection_Removed");
		newbieProtectionOnHit = getString("Newbie_Protection_On_Hit");
		newbieProtectionAttacker = getString("Newbie_Protection_Atacker");
		newbieTimeCheck = getString("Newbie_Time_Check");
		newbieTimeCheckOther = getString("Newbie_Time_Check_Other");
		newbieCommandBlocked = getString("Newbie_Command_Blocked");
		newbiePickupItemBlocked = getString("Newbie_Pickup_Items_Blocked");
		newbieForceRemovedWG = getString("Newbie_Force_Removed_WorldGuard");
		blockPlaceBlockedInCombat = getString("Block_Place_Blocked_InCombat");
		blockBreakBlockedInCombat = getString("Block_Break_Blocked_InCombat");
		eatBlockedInCombat = getString("Eating_Blocked_InCombat");
		enderpearlBlockedIncombat = getString("EnderPearl_Blocked_InCombat");
		chorusBlockedInCombat = getString("ChorusFruit_Blocked_InCombat");
		interactBlockedInCombat = getString("Interact_Blocked_InCombat");
		teleportBlockedInCombat = getString("Teleport_Blocked_InCombat");
		totemBlockedInCombat = getString("Totem_Blocked_InCombat");
		inventoryBlockedInCombat = getString("Inventory_Blocked_InCombat");
		pushbackWarning = getString("Pushback_Warning");
		errorCommand = getString("Error_Command");
		errorNotNewbie = getString("Error_Not_Newbie");
		errorPvpCooldown = getString("Error_PvP_Cooldown");
		errorPvpToggleNoPvp = getString("Error_PvPToggle_NoPvP");
		errorPvpToggleForcePvp = getString("Error_PvPToggle_ForcePvP");
		errorPermission = getString("Error_Permission");
		errorNotPlayer = getString("Error_Not_Player");
		moneyReward = getString("Money_Reward");
		moneyPenalty = getString("Money_Penalty");
		moneySteal = getString("Money_Steal");
		pvpListTitle = getString("PvPList_Title");
		pvpListEnabled = getString("PvPList_Enabled");
		pvpListDisabled = getString("PvPList_Disabled");
		pvpListNoResults = getString("PvPList_Nothing_Found");
		pvpToggleAdminChanged = getString("PvPToggle_Admin_Changed");
		pvpToggleAlreadyDisabled = getString("PvPToggle_Already_Disabled");
		pvpToggleAlreadyEnabled = getString("PvPToggle_Already_Enabled");
		errorPlayerNotFound = getString("Error_Player_Not_Found");
		tagTimeLeft = getString("Tag_Timeleft");
		tagNotInCombat = getString("Tag_Not_In_Combat");
		respawnProtectionSelf = getString("Respawn_Protection");
		respawnProtectionOther = getString("Respawn_Protection_Other");
		worldProtection = getString("World_Protection");
		afkProtection = getString("AFK_Protection");
		globalProtection = getString("Global_Protection");
		pvpForceEnabledWG = getString("PvP_Force_Enabled_WorldGuard");
		enabled = getString("Enabled");
		disabled = getString("Disabled");
	}

	private static void checkChanges() {
		final Properties originalEN = new Properties();
		final Properties original = new Properties();
		try (InputStream inputStreamEN = plugin.getResource("locale/" + Locale.EN.fileName());
				InputStream inputStream = plugin.getResource("locale/" + locale.fileName())) {
			originalEN.load(inputStreamEN);
			original.load(inputStream);
			final Enumeration<Object> originalKeys = originalEN.keys();
			while (originalKeys.hasMoreElements()) {
				final String a = (String) originalKeys.nextElement();
				if (!LANG.containsKey(a)) {
					Log.info("Added missing '" + a + "' key to messages file.");
					final String newProperty = original.getProperty(a) != null ? original.getProperty(a) : originalEN.getProperty(a);
					addMessage(a + " = " + new String(newProperty.getBytes("ISO-8859-1"), "UTF-8"));
					LANG.setProperty(a, newProperty);
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

	public static void messageProtection(final CancelResult result, final Player player, final Player attacked) {
		final String message = getProtectionMessage(result, attacked);
		final PvPlayer receiver = plugin.getPlayerHandler().get(player);
		receiver.message(message);
	}

	public static String getProtectionMessage(final CancelResult result, final Player attacked) {
		switch (result) {
		case NEWBIE:
			return result.attackerCaused() ? newbieBlocked() : newbieBlockedOther(attacked.getName());
		case PVPDISABLED:
			return result.attackerCaused() ? pvpDisabled() : pvpDisabledOther(attacked.getName());
		case RESPAWN_PROTECTION:
			return result.attackerCaused() ? respawnProtSelf() : respawnProtOther(attacked.getName());
		case WORLD_PROTECTION:
			return worldProtection();
		case AFK_PROTECTION:
			return getAfkProtection();
		case GLOBAL_PROTECTION:
			return getGlobalProtection();
		default:
			return "";
		}
	}

	public static String getErrorPlayerNotFound() {
		return errorPlayerNotFound;
	}

	public static String getPvpListNoResults() {
		return pvpListNoResults;
	}

	public static String getPvpListTitle() {
		return pvpListTitle;
	}

	public static String getPvpListEnabled() {
		return pvpListEnabled;
	}

	public static String getPvpListDisabled() {
		return pvpListDisabled;
	}

	public static String getPvPToggleAdminChanged() {
		return pvpToggleAdminChanged;
	}

	public static String getErrorPermission() {
		return errorPermission;
	}

	public static String getErrorPvpCooldown() {
		return errorPvpCooldown;
	}

	public static String getErrorPvPToggleNoPvP() {
		return errorPvpToggleNoPvp;
	}

	public static String getErrorPvPToggleForcePvP() {
		return errorPvpToggleForcePvp;
	}

	public static String getAlreadyDisabled() {
		return pvpToggleAlreadyDisabled;
	}

	public static String getAlreadyEnabled() {
		return pvpToggleAlreadyEnabled;
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

	public static String getChorusBlockedInCombat() {
		return chorusBlockedInCombat;
	}

	public static String getPushbackWarning() {
		return pushbackWarning;
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

	public static Locale getLocale() {
		return locale;
	}

	public static String getTagTimeLeft() {
		return tagTimeLeft;
	}

	public static String getTagNotInCombat() {
		return tagNotInCombat;
	}

	public static String getNewVersion() {
		return newVersion;
	}

	public static void setNewVersion(final String newVersion) {
		Messages.newVersion = newVersion;
	}

	public static String getNewbieCommandBlocked() {
		return newbieCommandBlocked;
	}

	public static String getPvpForceEnabledWG() {
		return pvpForceEnabledWG;
	}

	public static void sendQueuedMsgs(final PvPlayer player) {
		messageQueue.forEach(player::message);
	}

	public static void queueAdminMsg(final String message) {
		if (messageQueue.contains(message))
			return;
		messageQueue.add(message);
	}

	public static String getErrorNotNewbie() {
		return errorNotNewbie;
	}

	public static String getNewbieTimeCheck() {
		return newbieTimeCheck;
	}

	public static String getNewbieTimeCheckOther() {
		return newbieTimeCheckOther;
	}

	public static String getNewbieProtectionRemoved() {
		return newbieProtectionRemoved;
	}

	public static String getNewbieForceRemovedWG() {
		return newbieForceRemovedWG;
	}

	public static String getErrorNotPlayer() {
		return errorNotPlayer;
	}

	public static String respawnProtSelf() {
		return respawnProtectionSelf;
	}

	public static String respawnProtOther(final String name) {
		return respawnProtectionOther.replace("%p", name);
	}

	public static String worldProtection() {
		return worldProtection;
	}

	public static String getAfkProtection() {
		return afkProtection;
	}

	public static String getGlobalProtection() {
		return globalProtection;
	}

	public static String getTaggedAttackerABar() {
		return taggedAttackerActionbar;
	}

	public static String getTaggedDefenderABar() {
		return taggedDefenderActionbar;
	}

	public static String getOutOfCombatABar() {
		return outOfCombatActionbar;
	}

	public static String getTeleportBlockedInCombat() {
		return teleportBlockedInCombat;
	}

	public static String getBlockPlaceBlockedInCombat() {
		return blockPlaceBlockedInCombat;
	}

	public static String getBlockBreakBlockedInCombat() {
		return blockBreakBlockedInCombat;
	}

	public static String getEatBlockedInCombat() {
		return eatBlockedInCombat;
	}

	public static String getInteractBlockedInCombat() {
		return interactBlockedInCombat;
	}

	public static String getTotemBlockedInCombat() {
		return totemBlockedInCombat;
	}

	public static String getInventoryBlockedInCombat() {
		return inventoryBlockedInCombat;
	}

	public static String getNewbiePickupItemBlocked() {
		return newbiePickupItemBlocked;
	}

	public static String getEnabled() {
		return enabled;
	}

	public static String getDisabled() {
		return disabled;
	}

	public static String getPvPDisabledFee() {
		return pvpDisabledFee;
	}

	public static String getPvpFeeNotEnough() {
		return pvpFeeNotEnough;
	}
}
