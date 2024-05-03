package me.chancesd.pvpmanager.setting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Properties;
import java.util.Queue;
import java.util.stream.Stream;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.NoChance.PvPManager.Settings.Settings;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.ProtectionResult;
import me.chancesd.pvpmanager.setting.lang.Replacement;
import me.chancesd.pvpmanager.utils.ChatUtils;
import me.chancesd.sdutils.utils.Log;

public enum Messages {

	prefix("Prefix", "§c[§8PvPManager§c]"),
	otherStatusEnabled("Other_Status_Enabled", Replacement.PLAYER),
	othersStatusDisabled("Others_Status_Disabled",Replacement.PLAYER),
	pvpDisabled("PvP_Disabled"),
	pvpEnabled("PvP_Enabled"),
	selfStatusDisabled("Self_Status_Disabled"),
	selfStatusEnabled("Self_Status_Enabled"),
	commandDeniedIncombat("Command_Denied_InCombat"),
	attackDeniedYou("Attack_Denied_You"),
	attackDeniedOther("Attack_Denied_Other",Replacement.PLAYER),
	pvpDisabledFee("PvP_Disabled_Fee", Replacement.MONEY),
	pvpFeeNotEnough("PvP_Disabled_Fee_Not_Enough"),
	taggedAttacker("Tagged_Attacker",Replacement.PLAYER),
	taggedAttackerActionbar("Tagged_Attacker_ActionBar",Replacement.PLAYER),
	taggedDefender("Tagged_Defender", Replacement.PLAYER),
	taggedDefenderActionbar("Tagged_Defender_ActionBar",Replacement.PLAYER),
	outOfCombat("Out_Of_Combat"),
	outOfCombatActionbar("Out_Of_Combat_ActionBar"),
	newbieProtection("Newbie_Protection", Replacement.TIME),
	newbieProtectionEnd("Newbie_Protection_End"),
	newbieProtectionRemoved("Newbie_Protection_Removed"),
	newbieProtectionOnHit("Newbie_Protection_On_Hit"),
	newbieProtectionAttacker("Newbie_Protection_Atacker",Replacement.PLAYER),
	newbieTimeCheck("Newbie_Time_Check", Replacement.TIME),
	newbieTimeCheckOther("Newbie_Time_Check_Other",Replacement.PLAYER,Replacement.TIME),
	newbieCommandBlocked("Newbie_Command_Blocked"),
	newbiePickupItemBlocked("Newbie_Pickup_Items_Blocked"),
	newbieForceRemovedWG("Newbie_Force_Removed_WorldGuard"),
	blockPlaceBlockedInCombat("Block_Place_Blocked_InCombat"),
	blockBreakBlockedInCombat("Block_Break_Blocked_InCombat"),
	eatBlockedInCombat("Eating_Blocked_InCombat"),
	elytraBlockedInCombat("Elytra_Blocked_InCombat"),
	enderpearlBlockedIncombat("EnderPearl_Blocked_InCombat"),
	chorusBlockedInCombat("ChorusFruit_Blocked_InCombat"),
	interactBlockedInCombat("Interact_Blocked_InCombat"),
	teleportBlockedInCombat("Teleport_Blocked_InCombat"),
	totemBlockedInCombat("Totem_Blocked_InCombat"),
	inventoryBlockedInCombat("Inventory_Blocked_InCombat"),
	pushbackWarning("Pushback_Warning"),
	errorCommand("Error_Command"),
	errorNotNewbie("Error_Not_Newbie"),
	errorPvpCooldown("Error_PvP_Cooldown", Replacement.TIME),
	errorPvpToggleNoPvp("Error_PvPToggle_NoPvP"),
	errorPvpToggleForcePvp("Error_PvPToggle_ForcePvP"),
	errorPermission("Error_Permission"),
	errorNotPlayer("Error_Not_Player"),
	moneyReward("Money_Reward", Replacement.VICTIM, Replacement.MONEY),
	moneyPenalty("Money_Penalty", Replacement.MONEY),
	moneySteal("Money_Steal",Replacement.PLAYER, Replacement.MONEY),
	expWon("Exp_Won", Replacement.VICTIM, Replacement.EXP),
	expStolen("Exp_Stolen",Replacement.PLAYER, Replacement.EXP),
	pvpListTitle("PvPList_Title"),
	pvpListEnabled("PvPList_Enabled"),
	pvpListDisabled("PvPList_Disabled"),
	pvpListNoResults("PvPList_Nothing_Found"),
	pvpToggleAdminChanged("PvPToggle_Admin_Changed", Replacement.PLAYER, Replacement.STATE),
	pvpToggleAlreadyDisabled("PvPToggle_Already_Disabled"),
	pvpToggleAlreadyEnabled("PvPToggle_Already_Enabled"),
	errorPlayerNotFound("Error_Player_Not_Found", Replacement.PLAYER),
	tagTimeLeft("Tag_Timeleft", Replacement.TIME),
	tagNotInCombat("Tag_Not_In_Combat"),
	respawnProtectionSelf("Respawn_Protection"),
	respawnProtectionOther("Respawn_Protection_Other", Replacement.PLAYER),
	worldProtection("World_Protection"),
	afkProtection("AFK_Protection"),
	globalProtection("Global_Protection"),
	pvpForceEnabledWG("PvP_Force_Enabled_WorldGuard"),
	enabled("Enabled"),
	disabled("Disabled"),
	killAbuseWarning("Kill_Abuse_Warning"),
	timeDays("Time_Days"),
	timeHours("Time_Hours"),
	timeMinutes("Time_Minutes"),
	timeSeconds("Time_Seconds"),
	timeNow("Time_Now"),
	itemCooldown("Item_Cooldown", Replacement.TIME);

	private static final String LOCALE_FOLDER = "locale/";
	private static PvPManager plugin;
	private static final Properties LANG = new Properties();
	private static final Queue<String> messageQueue = new LinkedList<>();
	public static final String PREFIXMSG = "§c[§8PvPManager§c]";
	private static File messagesFile;
	private static String currentVersion;
	private static Locale locale;
	private static String newVersion;

	private final String messageKey;
	private final String[] replacements;
	private String message;

	private Messages(final String messageKey, final String... replacements) {
		this.messageKey = messageKey;
		this.replacements = replacements;
	}

	private Messages(final String messageKey, final String defaultMessage) {
		this(messageKey);
		this.message = defaultMessage;
	}

	private Messages(final String messageKey) {
		this(messageKey, new String[0]);
	}

	public void loadMessage() {
		this.message = getString(messageKey);
	}

	public String getMsg() {
		return message;
	}

	@NotNull
	public String getMsg(final String... arguments) {
		String finalMessage = message;
		for (int i = 0; i < replacements.length; i++) {
			final String placeholder = replacements[i];
			finalMessage = finalMessage.replace(placeholder, arguments[i]);
		}
		return finalMessage;
	}

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
			createMessagesFile();
		}
		final File[] listFiles = plugin.getDataFolder().listFiles();
		if (listFiles != null) {
			for (final File file : listFiles) {
				final String fileName = file.getName();
				if (fileName.contains("messages") && !fileName.equalsIgnoreCase(locale.fileName())) {
					try {
						Files.delete(file.toPath());
					} catch (final IOException e) {
						Log.warning("Failed to delete messages file", e);
					}
				}
			}
		}
		try (FileInputStream in = new FileInputStream(messagesFile)) {
			if (messagesFile.exists()) {
				LANG.clear();
				LANG.load(in);
				checkChanges();
				Stream.of(Messages.values()).forEach(Messages::loadMessage);
			}
		} catch (final IOException e) {
			Log.severe("Error reading locale file", e);
		}
	}

	private static void createMessagesFile() {
		int readBytes;
		final byte[] buffer = new byte[4096];
		try (InputStream input = plugin.getResource(LOCALE_FOLDER + locale.fileName());
				OutputStream resStreamOut = new FileOutputStream(new File(plugin.getDataFolder() + File.separator + locale.fileName()))) {
			if (input == null) {
				Log.severe("Couldn't find the default locale file " + locale.fileName());
				return;
			}
			while ((readBytes = input.read(buffer)) != -1) {
				resStreamOut.write(buffer, 0, readBytes);
			}
		} catch (final IOException e) {
			Log.severe("Error reading default locale from jar", e);
		}
		Log.infoColor(ChatColor.DARK_GREEN + "New messages file created successfully!");
	}

	@NotNull
	public static String getString(final String key) {
		final String message = new String(LANG.getProperty(key).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
		return ChatUtils.colorize(message).replace(Replacement.PREFIX, prefix.getMsg());
	}

	private static void checkChanges() {
		final Properties originalEN = new Properties();
		final Properties original = new Properties();
		try (InputStream inputStreamEN = plugin.getResource(LOCALE_FOLDER + Locale.EN.fileName());
				InputStream inputStream = plugin.getResource(LOCALE_FOLDER + locale.fileName())) {
			originalEN.load(inputStreamEN);
			original.load(inputStream);
			final Enumeration<Object> originalKeys = originalEN.keys();
			while (originalKeys.hasMoreElements()) {
				final String a = (String) originalKeys.nextElement();
				if (!LANG.containsKey(a)) {
					Log.info("Added missing '" + a + "' key to messages file.");
					final String newProperty = original.getProperty(a) != null ? original.getProperty(a) : originalEN.getProperty(a);
					addMessage(a + " = " + new String(newProperty.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
					LANG.setProperty(a, newProperty);
				}
			}
		} catch (final IOException e) {
			Log.severe(e.getMessage(), e);
		}
	}

	private static void addMessage(final String a) {
		try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(messagesFile, true), StandardCharsets.UTF_8))) {
			pw.println(a);
		} catch (final IOException e) {
			Log.severe(e.getMessage(), e);
		}
	}

	public static void messageProtection(final ProtectionResult result, final Player player, final Player attacked) {
		final String message = getProtectionMessage(result, attacked);
		final CombatPlayer receiver = plugin.getPlayerHandler().get(player);
		receiver.message(message);
	}

	public static String getProtectionMessage(final ProtectionResult result, final Player attacked) {
		switch (result.type()) {
		case NEWBIE:
			return result.isAttacker() ? newbieProtectionOnHit.getMsg() : newbieProtectionAttacker.getMsg(attacked.getName());
		case PVPDISABLED:
			return result.isAttacker() ? attackDeniedYou.getMsg() : attackDeniedOther.getMsg(attacked.getName());
		case RESPAWN_PROTECTION:
			return result.isAttacker() ? respawnProtectionSelf.getMsg() : respawnProtectionOther.getMsg(attacked.getName());
		case WORLD_PROTECTION:
			return worldProtection.getMsg();
		case AFK_PROTECTION:
			return afkProtection.getMsg();
		case GLOBAL_PROTECTION:
			return globalProtection.getMsg();
		default:
			return "";
		}
	}

	public static String getTime(final ChronoUnit time) {
		switch (time) {
		case DAYS:
			return timeDays.getMsg();
		case HOURS:
			return timeHours.getMsg();
		case MINUTES:
			return timeMinutes.getMsg();
		case SECONDS:
			return timeSeconds.getMsg();
		default:
			return timeNow.getMsg();
		}
	}

	public static String getCurrentversion() {
		return currentVersion;
	}

	public static Locale getLocale() {
		return locale;
	}

	public static String getNewVersion() {
		return newVersion;
	}

	public static void setNewVersion(final String newVersion) {
		Messages.newVersion = newVersion;
	}

	public static void sendQueuedMsgs(final CombatPlayer player) {
		messageQueue.forEach(player::message);
	}

	public static void queueAdminMsg(final String message) {
		if (messageQueue.contains(message))
			return;
		messageQueue.add(message);
	}

}
