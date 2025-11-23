package me.chancesd.pvpmanager.setting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
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
import me.chancesd.sdutils.utils.ChatUtils;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.TimeUtil;
import me.chancesd.sdutils.utils.TimeUtil.TimeLangProvider;
import me.chancesd.sdutils.utils.Utils;

/**
 * Message class to easily send messages to players, with replacements if necessary
 *
 * @author ChanceSD
 */
public enum Lang implements TimeLangProvider {

	PREFIX("Prefix", "&#992222&lPvP &#FF5555➤"),
	OTHER_STATUS_ENABLED("Other_Status_Enabled", Replacement.PLAYER),
	OTHERS_STATUS_DISABLED("Others_Status_Disabled", Replacement.PLAYER),
	PVPDISABLED("PvP_Disabled"),
	PVPENABLED("PvP_Enabled"),
	SELF_STATUS_DISABLED("Self_Status_Disabled"),
	SELF_STATUS_ENABLED("Self_Status_Enabled"),
	COMMAND_DENIED_INCOMBAT("Command_Denied_InCombat"),
	ATTACK_DENIED_YOU("Attack_Denied_You"),
	ATTACK_DENIED_OTHER("Attack_Denied_Other", Replacement.PLAYER),
	PVP_DISABLED_FEE("PvP_Disabled_Fee", Replacement.MONEY),
	PVP_FEE_NOT_ENOUGH("PvP_Disabled_Fee_Not_Enough"),
	TAGGED_ATTACKER("Tagged_Attacker", Replacement.PLAYER),
	TAGGED_DEFENDER("Tagged_Defender", Replacement.PLAYER),
	OUT_OF_COMBAT("Out_Of_Combat"),
	OUT_OF_COMBAT_ACTIONBAR("Out_Of_Combat_ActionBar"),
	NEWBIE_PROTECTION("Newbie_Protection", Replacement.TIME),
	NEWBIE_PROTECTION_END("Newbie_Protection_End"),
	NEWBIE_PROTECTION_REMOVED("Newbie_Protection_Removed"),
	NEWBIE_PROTECTION_ON_HIT("Newbie_Protection_On_Hit"),
	NEWBIE_PROTECTION_ATTACKER("Newbie_Protection_Attacker", Replacement.PLAYER),
	NEWBIE_TIME_CHECK("Newbie_Time_Check", Replacement.TIME),
	NEWBIE_TIME_CHECK_OTHER("Newbie_Time_Check_Other", Replacement.PLAYER, Replacement.TIME),
	NEWBIE_COMMAND_BLOCKED("Newbie_Command_Blocked"),
	NEWBIE_PICKUP_ITEM_BLOCKED("Newbie_Pickup_Items_Blocked"),
	NEWBIE_FORCE_REMOVED_WG("Newbie_Force_Removed_WorldGuard"),
	BLOCK_PLACE_BLOCKED_IN_COMBAT("Block_Place_Blocked_InCombat"),
	BLOCK_BREAK_BLOCKED_IN_COMBAT("Block_Break_Blocked_InCombat"),
	EAT_BLOCKED_IN_COMBAT("Eating_Blocked_InCombat"),
	ELYTRA_BLOCKED_IN_COMBAT("Elytra_Blocked_InCombat"),
	ENDERPEARL_BLOCKED_INCOMBAT("EnderPearl_Blocked_InCombat"),
	CHORUS_BLOCKED_IN_COMBAT("ChorusFruit_Blocked_InCombat"),
	INTERACT_BLOCKED_IN_COMBAT("Interact_Blocked_InCombat"),
	TELEPORT_BLOCKED_IN_COMBAT("Teleport_Blocked_InCombat"),
	TOTEM_BLOCKED_IN_COMBAT("Totem_Blocked_InCombat"),
	INVENTORY_BLOCKED_IN_COMBAT("Inventory_Blocked_InCombat"),
	FIREWORK_BLOCKED_IN_COMBAT("Firework_Blocked_InCombat"),
	FIREWORK_POWER_LIMITED_IN_COMBAT("Firework_Power_Limited_InCombat", Replacement.POWER),
	PUSHBACK_WARNING("Pushback_Warning"),
	ERROR_NOT_NEWBIE("Error_Not_Newbie"),
	ERROR_PVP_COOLDOWN("Error_PvP_Cooldown", Replacement.TIME),
	ERROR_PVP_TOGGLE_NO_PVP("Error_PvPToggle_NoPvP"),
	ERROR_PVP_TOGGLE_FORCE_PVP("Error_PvPToggle_ForcePvP"),
	ERROR_PERMISSION("Error_Permission"),
	ERROR_NOT_PLAYER("Error_Not_Player"),
	MONEY_REWARD("Money_Reward", Replacement.VICTIM, Replacement.MONEY),
	MONEY_PENALTY("Money_Penalty", Replacement.MONEY),
	MONEY_STEAL("Money_Steal", Replacement.PLAYER, Replacement.MONEY),
	EXP_WON("Exp_Won", Replacement.VICTIM, Replacement.EXP),
	EXP_STOLEN("Exp_Stolen", Replacement.PLAYER, Replacement.EXP),
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
	private static File messagesFile;
	private static Locale locale;
	private final String messageKey;
	private final Replacement[] replacements;
	private String message;
	private DisplayMode displayMode = DisplayMode.CHAT;

	Lang(final String messageKey, final Replacement... replacements) {
		this.messageKey = messageKey;
		this.replacements = replacements;
	}

	Lang(final String messageKey, final String defaultMessage) {
		this(messageKey);
		this.message = ChatUtils.colorize(defaultMessage);
	}

	Lang(final String messageKey) {
		this(messageKey, new Replacement[0]);
	}

	public void loadMessage() {
		final String[] parsed = parseMessageWithMode(messageKey);
		this.message = parsed[0];
		this.displayMode = DisplayMode.valueOf(parsed[1]);
	}

	@NotNull
	public String msg() {
		return message;
	}

	public Replacement[] getReplacements() {
		return replacements;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public DisplayMode getDisplayMode() {
		return displayMode;
	}

	@NotNull
	public String msg(final String... arguments) {
		String finalMessage = message;
		for (int i = 0; i < replacements.length; i++) {
			final String placeholder = replacements[i].getPlaceholder();
			finalMessage = finalMessage.replace(placeholder, arguments[i]);
		}
		return finalMessage;
	}

	@NotNull
	public String msg(final Object... arguments) {
		String finalMessage = message;
		for (int i = 0; i < replacements.length && i < arguments.length; i++) {
			final String placeholder = replacements[i].getPlaceholder();
			final String replacement = arguments[i] == null ? "null" : arguments[i].toString();
			finalMessage = finalMessage.replace(placeholder, replacement);
		}
		return finalMessage;
	}

	@NotNull
	public String msgTimeUntil(final long time) {
		return message.replace(Replacement.TIME.getPlaceholder(), TimeUtil.getDiffUntil(this, time));
	}

	@NotNull
	public String msgTime(final long time) {
		return message.replace(Replacement.TIME.getPlaceholder(), TimeUtil.getDiffDuration(this, time));
	}

	@SuppressWarnings("java:S3066")
	public static void setup(final PvPManager plugin) {
		Lang.plugin = plugin;
		final String localeString = Conf.LOCALE.asString();
		try {
			locale = Locale.valueOf(localeString);
		} catch (final IllegalArgumentException e) {
			Log.warning("Error! Locale '" + localeString + "' does not exist! Using default messages");
			locale = Locale.EN;
		}
		load();
	}

	private static void load() {
		messagesFile = new File(plugin.getDataFolder(), locale.fileName());
		checkForVersionUpgrade();
		deletePreviousMessageFile();

		if (!messagesFile.exists()) {
			createMessagesFile();
		}

		try (FileInputStream in = new FileInputStream(messagesFile)) {
			if (messagesFile.exists()) {
				LANG_PROPERTIES.clear();
				LANG_PROPERTIES.load(in);
				checkChanges();
				migrateProtectionMessages();
				Stream.of(Lang.values()).forEach(Lang::loadMessage);
			}
		} catch (final IOException e) {
			Log.severe("Error reading locale file", e);
		}
	}

	private static void checkForVersionUpgrade() {
		if (plugin.getConfigM().isMajorVersionUpgrade()) {
			final String fileName = locale.fileName();
			Utils.renameFile(messagesFile, fileName.substring(0, fileName.length() - 11) + "_v3_old" + ".properties");
		}
	}

	private static void deletePreviousMessageFile() {
		final File[] listFiles = plugin.getDataFolder().listFiles();
		if (listFiles != null) {
			for (final File file : listFiles) {
				final String fileName = file.getName();
				if (fileName.contains("messages") && !fileName.equalsIgnoreCase(locale.fileName()) && !fileName.contains("old")) {
					try {
						Files.delete(file.toPath());
					} catch (final IOException e) {
						Log.warning("Failed to delete messages file", e);
					}
				}
			}
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
		return ChatUtils.colorize(message).replace(Replacement.PREFIX.getPlaceholder(), PREFIX.msg());
	}

	@NotNull
	private static String[] parseMessageWithMode(final String key) {
		String message = new String(LANG_PROPERTIES.getProperty(key).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
		DisplayMode mode = DisplayMode.CHAT;

		// Check for display mode prefix (case-insensitive)
		if (message.toLowerCase().startsWith("!actionbar ")) {
			mode = DisplayMode.ACTION_BAR;
			message = message.substring(11); // Remove "!actionbar "
		} else if (message.toLowerCase().startsWith("!chat ")) {
			mode = DisplayMode.CHAT;
			message = message.substring(6); // Remove "!chat "
		}

		message = ChatUtils.colorize(message).replace(Replacement.PREFIX.getPlaceholder(), PREFIX.msg());
		return new String[] { message, mode.name() };
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
					final String messageToAdd = a + " = " + new String(newProperty.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
					addMessage(messageToAdd);
					LANG_PROPERTIES.setProperty(a, newProperty);
				}
			}
		} catch (final IOException e) {
			Log.severe(e.getMessage(), e);
		}
	}

	private static void addMessage(final String messageToAdd) {
		final String messageKey = messageToAdd.split(" = ")[0];
		final String escapedMessage = messageToAdd.replace("\n", "\\n");
		String previousKey = null;

		// Find insertion position from internal file
		try (InputStream inputStream = plugin.getResource(LOCALE_FOLDER + Locale.EN.fileName());
			 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.contains(" = ") && !line.startsWith("#")) {
					final String currentKey = line.split(" = ")[0];
					if (currentKey.equals(messageKey)) {
						// Find existing previous key by walking backwards
						while (previousKey != null && !LANG_PROPERTIES.containsKey(previousKey)) {
							previousKey = findKeyBefore(previousKey);
						}
						break;
					}
					previousKey = currentKey;
				}
			}

			// Insert at found position or append
			if (previousKey != null && LANG_PROPERTIES.containsKey(previousKey)) {
				final List<String> lines = Files.readAllLines(messagesFile.toPath(), StandardCharsets.UTF_8);
				for (int i = 0; i < lines.size(); i++) {
					if (lines.get(i).trim().startsWith(previousKey + " = ")) {
						lines.add(i + 1, escapedMessage);
						Files.write(messagesFile.toPath(), lines, StandardCharsets.UTF_8);
						return;
					}
				}
			}
		} catch (final IOException e) {
			Log.severe(e.getMessage(), e);
		}

		try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(messagesFile, true), StandardCharsets.UTF_8))) {
			pw.println(escapedMessage);
		} catch (final IOException e) {
			Log.severe(e.getMessage(), e);
		}
	}

	private static String findKeyBefore(final String targetKey) {
		try (InputStream inputStream = plugin.getResource(LOCALE_FOLDER + Locale.EN.fileName());
			 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			String line;
			String previousKey = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.contains(" = ") && !line.startsWith("#")) {
					final String currentKey = line.split(" = ")[0];
					if (currentKey.equals(targetKey)) return previousKey;
					previousKey = currentKey;
				}
			}
		} catch (final IOException e) {
			Log.severe(e.getMessage(), e);
		}
		return null;
	}

	private static void migrateProtectionMessages() {
		final int configVersion = plugin.getConfigM().getOldVersion();
		if (configVersion >= 205) {
			return;
		}

		final String[] protectionKeys = {
			"Respawn_Protection",
			"Respawn_Protection_Other",
			"World_Protection",
			"AFK_Protection",
			"Global_Protection",
			"Newbie_Protection_Attacker",
			"Attack_Denied_You",
			"Attack_Denied_Other",
			"Block_Place_Blocked_InCombat",
			"Block_Break_Blocked_InCombat",
			"Eating_Blocked_InCombat",
			"Elytra_Blocked_InCombat",
			"Firework_Blocked_InCombat",
			"Firework_Power_Limited_InCombat",
			"Interact_Blocked_InCombat",
			"Newbie_Pickup_Items_Blocked",
			"Inventory_Blocked_InCombat",
			"Item_Cooldown"
		};

		final String[] timeKeys = {
			"Time_Days",
			"Time_Hours",
			"Time_Minutes",
			"Time_Seconds"
		};

		boolean modified = false;
		for (final String key : protectionKeys) {
			final String value = LANG_PROPERTIES.getProperty(key);
			if (value != null && !value.toLowerCase().startsWith("!actionbar ") && !value.toLowerCase().startsWith("!chat ")) {
				LANG_PROPERTIES.setProperty(key, "!actionbar " + value);
				modified = true;
			}
		}

		for (final String key : timeKeys) {
			final String value = LANG_PROPERTIES.getProperty(key);
			if (value != null && !value.startsWith(" ") && !value.equals("now")) {
				LANG_PROPERTIES.setProperty(key, " " + value);
				modified = true;
			}
		}

		if (modified) {
			try {
				// Read current file content to preserve formatting and comments
				final List<String> lines = Files.readAllLines(messagesFile.toPath(), StandardCharsets.UTF_8);
				final List<String> updatedLines = new LinkedList<>();
				boolean documentationAdded = false;

				for (final String line : lines) {
					String updatedLine = line;

					// Add documentation after the first comment block (before Prefix line)
					if (!documentationAdded && line.trim().startsWith("Prefix = ")) {
						updatedLines.add("# You can prefix any message with !actionbar or !chat to control where it appears");
						updatedLines.add("# - !actionbar = Message shows above hotbar (recommended for frequent spam messages)");
						updatedLines.add("# - !chat = Message shows in chat (default if no prefix)");
						updatedLines.add("# Example: Item_Cooldown = !actionbar {prefix} &cYou can't use this yet! Available in &e{time}");
						updatedLines.add("");
						documentationAdded = true;
					}

					for (final String key : protectionKeys) {
						if (line.trim().startsWith(key + " = ")) {
							final String newValue = LANG_PROPERTIES.getProperty(key);
							updatedLine = key + " = " + newValue;
							break;
						}
					}
					for (final String key : timeKeys) {
						if (line.trim().startsWith(key + " = ")) {
							final String newValue = LANG_PROPERTIES.getProperty(key);
							updatedLine = key + " = \\" + newValue;
							break;
						}
					}
					updatedLines.add(updatedLine);
				}

				Files.write(messagesFile.toPath(), updatedLines, StandardCharsets.UTF_8);
				Log.infoColor(ChatColor.AQUA + "Migrated messages to use action bar display mode and added spacing to time units");
			} catch (final IOException e) {
				Log.warning("Failed to save migrated protection messages", e);
			}
		}
	}

	public static void messageProtection(final ProtectionResult result, final Player player, final Player attacked) {
		final CombatPlayer receiver = plugin.getPlayerManager().get(player);
		final Lang message = getProtectionMessage(result);
		if (message != null) {
			if (result.isAttacker()) {
				receiver.messageWithDuration(message, 500);
			} else {
				receiver.messageWithDuration(message, 500, attacked.getName());
			}
		}
	}

	public static Lang getProtectionMessage(final ProtectionResult result) {
		return switch (result.type()) {
		case NEWBIE ->
			result.isAttacker() ? NEWBIE_PROTECTION_ON_HIT : NEWBIE_PROTECTION_ATTACKER;
		case PVPDISABLED ->
			result.isAttacker() ? ATTACK_DENIED_YOU : ATTACK_DENIED_OTHER;
		case RESPAWN_PROTECTION ->
			result.isAttacker() ? RESPAWN_PROTECTION_SELF : RESPAWN_PROTECTION_OTHER;
		case WORLD_PROTECTION -> WORLD_PROTECTION;
		case AFK_PROTECTION -> AFK_PROTECTION;
		case GLOBAL_PROTECTION -> GLOBAL_PROTECTION;
		default -> null;
		};
	}

	@Override
	public String getTime(final ChronoUnit time) {
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
		messageQueue.forEach(msg -> player.getPlayer().sendMessage(msg));
	}

	public static void queueAdminMsg(final String message) {
		if (messageQueue.contains(message))
			return;
		messageQueue.add(message);
	}

	@Override
	public String toString() {
		return message;
	}

}
