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
import java.util.Properties;
import java.util.Queue;
import java.util.stream.Stream;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.ProtectionResult;
import me.chancesd.pvpmanager.setting.lang.Replacement;
import me.chancesd.pvpmanager.utils.ChatUtils;
import me.chancesd.sdutils.utils.Log;

public enum Lang {

	PREFIX("Prefix", Lang.PREFIXMSG),
	OTHER_STATUS_ENABLED("Other_Status_Enabled", Replacement.PLAYER),
	OTHERS_STATUS_DISABLED("Others_Status_Disabled",Replacement.PLAYER),
	PVPDISABLED("PvP_Disabled"),
	PVPENABLED("PvP_Enabled"),
	SELF_STATUS_DISABLED("Self_Status_Disabled"),
	SELF_STATUS_ENABLED("Self_Status_Enabled"),
	COMMAND_DENIED_INCOMBAT("Command_Denied_InCombat"),
	ATTACK_DENIED_YOU("Attack_Denied_You"),
	ATTACK_DENIED_OTHER("Attack_Denied_Other",Replacement.PLAYER),
	PVP_DISABLED_FEE("PvP_Disabled_Fee", Replacement.MONEY),
	TAGGED_ATTACKER("Tagged_Attacker",Replacement.PLAYER),
	TAGGED_ATTACKER_ACTIONBAR("Tagged_Attacker_ActionBar",Replacement.PLAYER),
	TAGGED_DEFENDER("Tagged_Defender", Replacement.PLAYER),
	TAGGED_DEFENDER_ACTIONBAR("Tagged_Defender_ActionBar",Replacement.PLAYER),
	OUT_OF_COMBAT("Out_Of_Combat"),
	OUT_OF_COMBAT_ACTIONBAR("Out_Of_Combat_ActionBar"),
	NEWBIE_PROTECTION("Newbie_Protection", Replacement.TIME),
	NEWBIE_PROTECTION_END("Newbie_Protection_End"),
	NEWBIE_PROTECTION_REMOVED("Newbie_Protection_Removed"),
	NEWBIE_PROTECTION_ON_HIT("Newbie_Protection_On_Hit"),
	NEWBIE_PROTECTION_ATTACKER("Newbie_Protection_Atacker",Replacement.PLAYER),
	NEWBIE_TIME_CHECK("Newbie_Time_Check", Replacement.TIME),
	NEWBIE_TIME_CHECK_OTHER("Newbie_Time_Check_Other",Replacement.PLAYER,Replacement.TIME),
	NEWBIE_COMMAND_BLOCKED("Newbie_Command_Blocked"),
	NEWBIE_PICKUP_ITEM_BLOCKED("Newbie_Pickup_Items_Blocked"),
	NEWBIE_FORCE_REMOVED_WG("Newbie_Force_Removed_WorldGuard"),
	BLOCK_PLACE_BLOCKED_IN_COMBAT("Block_Place_Blocked_InCombat"),
	EAT_BLOCKED_IN_COMBAT("Eating_Blocked_InCombat"),
	ELYTRA_BLOCKED_IN_COMBAT("Elytra_Blocked_InCombat"),
	ENDERPEARL_BLOCKED_INCOMBAT("EnderPearl_Blocked_InCombat"),
	CHORUS_BLOCKED_IN_COMBAT("ChorusFruit_Blocked_InCombat"),
	INTERACT_BLOCKED_IN_COMBAT("Interact_Blocked_InCombat"),
	TELEPORT_BLOCKED_IN_COMBAT("Teleport_Blocked_InCombat"),
	TOTEM_BLOCKED_IN_COMBAT("Totem_Blocked_InCombat"),
	INVENTORY_BLOCKED_IN_COMBAT("Inventory_Blocked_InCombat"),
	PUSHBACK_WARNING("Pushback_Warning"),
	ERROR_COMMAND("Error_Command"),
	ERROR_NOT_NEWBIE("Error_Not_Newbie"),
	ERROR_PVP_COOLDOWN("Error_PvP_Cooldown", Replacement.TIME),
	ERROR_PVP_TOGGLE_NO_PVP("Error_PvPToggle_NoPvP"),
	ERROR_PVP_TOGGLE_FORCE_PVP("Error_PvPToggle_ForcePvP"),
	ERROR_PERMISSION("Error_Permission"),
	ERROR_NOT_PLAYER("Error_Not_Player"),
	MONEY_REWARD("Money_Reward", Replacement.VICTIM, Replacement.MONEY),
	MONEY_PENALTY("Money_Penalty", Replacement.MONEY),
	MONEY_STEAL("Money_Steal",Replacement.PLAYER, Replacement.MONEY),
	EXP_WON("Exp_Won", Replacement.VICTIM, Replacement.EXP),
	EXP_STOLEN("Exp_Stolen",Replacement.PLAYER, Replacement.EXP),
	PVP_LIST_TITLE("PvPList_Title"),
	PVP_LIST_ENABLED("PvPList_Enabled"),
	PVP_LIST_DISABLED("PvPList_Disabled"),
	PVP_LIST_NO_RESULTS("PvPList_Nothing_Found"),
	PVP_TOGGLE_ADMIN_CHANGED("PvPToggle_Admin_Changed", Replacement.PLAYER, Replacement.STATE),
	PVP_TOGGLE_ALREADY_DISABLED("PvPToggle_Already_Disabled"),
	PVP_TOGGLE_ALREADY_ENABLED("PvPToggle_Already_Enabled"),
	ERROR_PLAYER_NOT_FOUND("Error_Player_Not_Found", Replacement.PLAYER),
	TAG_TIME_LEFT("Tag_Timeleft", Replacement.TIME),
	TAG_NOT_IN_COMBAT("Tag_Not_In_Combat"),
	RESPAWN_PROTECTION_SELF("Respawn_Protection"),
	RESPAWN_PROTECTION_OTHER("Respawn_Protection_Other", Replacement.PLAYER),
	WORLD_PROTECTION("World_Protection"),
	AFK_PROTECTION("AFK_Protection"),
	GLOBAL_PROTECTION("Global_Protection"),
	PVP_FORCE_ENABLED_WG("PvP_Force_Enabled_WorldGuard"),
	ENABLED("Enabled"),
	DISABLED("Disabled"),
	KILL_ABUSE_WARNING("Kill_Abuse_Warning"),
	TIME_DAYS("Time_Days"),
	TIME_HOURS("Time_Hours"),
	TIME_MINUTES("Time_Minutes"),
	TIME_SECONDS("Time_Seconds"),
	TIME_NOW("Time_Now"),
	ITEM_COOLDOWN("Item_Cooldown", Replacement.TIME);

	private static final String LOCALE_FOLDER = "locale/";
	private static PvPManager plugin;
	private static final Properties LANG_PROPERTIES = new Properties();
	private static final Queue<String> messageQueue = new LinkedList<>();
	public static final String PREFIXMSG = "§c[§8PvPManager§c]";
	private static File messagesFile;
	private static Locale locale;

	private final String messageKey;
	private final String[] replacements;
	private String message;

	Lang(final String messageKey, final String... replacements) {
		this.messageKey = messageKey;
		this.replacements = replacements;
	}

	Lang(final String messageKey, final String defaultMessage) {
		this(messageKey);
		this.message = defaultMessage;
	}

	Lang(final String messageKey) {
		this(messageKey, new String[0]);
	}

	public void loadMessage() {
		this.message = getString(messageKey);
	}

	@NotNull
	public String msg() {
		return message;
	}

	@NotNull
	public String msg(final String... arguments) {
		String finalMessage = message;
		for (int i = 0; i < replacements.length; i++) {
			final String placeholder = replacements[i];
			finalMessage = finalMessage.replace(placeholder, arguments[i]);
		}
		return finalMessage;
	}

	public static void setup(final PvPManager plugin) {
		Lang.plugin = plugin;
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
				LANG_PROPERTIES.clear();
				LANG_PROPERTIES.load(in);
				checkChanges();
				Stream.of(Lang.values()).forEach(Lang::loadMessage);
			}
		} catch (final IOException e) {
			Log.severe("Error reading locale file", e);
		}
	}

	private static void createMessagesFile() {
		int readBytes;
		final byte[] buffer = new byte[4096];
		try (InputStream input = plugin.getResource(LOCALE_FOLDER + locale.fileName());
				OutputStream resStreamOut = new FileOutputStream(plugin.getDataFolder() + File.separator + locale.fileName())) {
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
		final String message = new String(LANG_PROPERTIES.getProperty(key).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
		return ChatUtils.colorize(message).replace(Replacement.PREFIX, PREFIX.msg());
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
				if (!LANG_PROPERTIES.containsKey(a)) {
					Log.info("Added missing '" + a + "' key to messages file.");
					final String newProperty = original.getProperty(a) != null ? original.getProperty(a) : originalEN.getProperty(a);
					addMessage(a + " = " + new String(newProperty.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
					LANG_PROPERTIES.setProperty(a, newProperty);
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
		return switch (result.type()) {
			case NEWBIE ->
					result.isAttacker() ? NEWBIE_PROTECTION_ON_HIT.msg() : NEWBIE_PROTECTION_ATTACKER.msg(attacked.getName());
			case PVPDISABLED ->
					result.isAttacker() ? ATTACK_DENIED_YOU.msg() : ATTACK_DENIED_OTHER.msg(attacked.getName());
			case RESPAWN_PROTECTION ->
					result.isAttacker() ? RESPAWN_PROTECTION_SELF.msg() : RESPAWN_PROTECTION_OTHER.msg(attacked.getName());
			case WORLD_PROTECTION -> WORLD_PROTECTION.msg();
			case AFK_PROTECTION -> AFK_PROTECTION.msg();
			case GLOBAL_PROTECTION -> GLOBAL_PROTECTION.msg();
			default -> "";
		};
	}

	public static String getTime(final ChronoUnit time) {
		return switch (time) {
			case DAYS -> TIME_DAYS.msg();
			case HOURS -> TIME_HOURS.msg();
			case MINUTES -> TIME_MINUTES.msg();
			case SECONDS -> TIME_SECONDS.msg();
			default -> TIME_NOW.msg();
		};
	}

	public static Locale getLocale() {
		return locale;
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
