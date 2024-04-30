package me.chancesd.pvpmanager.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.NoChance.PvPManager.Dependencies.Hook;
import me.clip.placeholderapi.PlaceholderAPI;

public class ChatUtils {

	private static final Pattern HEX_PATTERN = Pattern.compile("&(#[a-fA-F0-9]{6})");
	private static final boolean USEPLACEHOLDERAPI = Hook.PLACEHOLDERAPI.isEnabled();
	private static final boolean hexSupported = checkForBungeeAPI();

	private ChatUtils() {
	}

	private static boolean checkForBungeeAPI() {
		try {
			net.md_5.bungee.api.ChatColor.of("#FFFFFF");
		} catch (final NoClassDefFoundError | NoSuchMethodError e) {
			return false;
		}
		return true;
	}

	public static String setPlaceholders(final Player player, final String message) {
		return USEPLACEHOLDERAPI ? PlaceholderAPI.setPlaceholders(player, message) : message;
	}

	@NotNull
	public static String colorize(@NotNull final String message) {
		return colorizeHex(ChatColor.translateAlternateColorCodes('&', message));
	}

	@NotNull
	private static String colorizeHex(@NotNull final String message) {
		if (!hexSupported)
			return message;

		final Matcher matcher = HEX_PATTERN.matcher(message);
		final StringBuffer buffer = new StringBuffer();

		while (matcher.find()) {
			matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of(matcher.group(1)).toString());
		}

		return matcher.appendTail(buffer).toString();
	}

	public static void send(@NotNull final CommandSender sender, @NotNull final String message) {
		sender.sendMessage(message);
	}

	public static List<String> getMatchingEntries(final String token, final List<String> toFilter) {
		toFilter.removeIf(s -> !s.toLowerCase().contains(token.toLowerCase()));
		return toFilter;
	}
}
