package me.NoChance.PvPManager.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

public class ChatUtils {

	private static final Pattern HEX_PATTERN = Pattern.compile("&(#[a-fA-F0-9]{6})");
	private static boolean hexSupported = checkForBungeeAPI();

	private static boolean checkForBungeeAPI() {
		try {
			net.md_5.bungee.api.ChatColor.of("#FFFFFF");
		} catch (final NoClassDefFoundError | NoSuchMethodError e) {
			return false;
		}
		return true;
	}

	public static String colorize(final String message) {
		return colorizeHex(ChatColor.translateAlternateColorCodes('&', message));
	}

	private static String colorizeHex(final String message) {
		if (!hexSupported)
			return message;

		final Matcher matcher = HEX_PATTERN.matcher(message);
		final StringBuffer buffer = new StringBuffer();

		while (matcher.find()) {
			matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of(matcher.group(1)).toString());
		}

		return matcher.appendTail(buffer).toString();
	}
}
