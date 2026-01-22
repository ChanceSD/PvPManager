package me.chancesd.pvpmanager.integration.hook.placeholderapi;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.sdutils.utils.Utils;

/**
 * Standalone placeholder processor for PvPManager placeholders.
 * This class provides placeholder replacement functionality without depending on PlaceholderAPI classes.
 * Contains the core logic that is shared between PlaceholderAPI integration and fallback processing.
 */
public class PlaceholderProcessor {

	private final PvPManager plugin;
	@NotNull
	private final List<String> placeholders = new ArrayList<>();
	private final DecimalFormat df = new DecimalFormat("#.##");

	public PlaceholderProcessor(final PvPManager plugin) {
		this.plugin = plugin;
		registerPlaceholder("in_combat");
		registerPlaceholder("combat_timeleft");
		registerPlaceholder("combat_timeleft_ms");
		registerPlaceholder("pvp_status");
		registerPlaceholder("pvp_command_timeleft");
		registerPlaceholder("is_newbie");
		registerPlaceholder("newbie_timeleft");
		registerPlaceholder("has_override");
		registerPlaceholder("has_respawn_prot");
		registerPlaceholder("combat_prefix");
		registerPlaceholder("player_health");
		registerPlaceholder("current_enemy");
		registerPlaceholder("current_enemy_health");
		registerPlaceholder("global_pvp_status");
	}

	public String replacePlaceholders(final Player player, String message) {
		if (message == null || player == null) {
			return message;
		}

		String result = message;
		for (final String placeholder : placeholders) {
			if (result.contains("%" + placeholder + "%")) {
				final String identifier = placeholder.substring("pvpmanager_".length());
				final String replacement = processPlaceholder(player, identifier);
				if (replacement != null) {
					result = result.replace("%" + placeholder + "%", replacement);
				}
			}
		}

		return result;
	}

	/**
	 * Core placeholder processing logic shared by both PlaceholderAPI integration and fallback processing.
	 * This method contains the actual placeholder replacement logic.
	 *
	 * @param player     The player to get placeholder values for
	 * @param identifier The placeholder identifier (without pvpmanager_ prefix)
	 * @return The placeholder value or null if not found
	 */
	public String processPlaceholder(final Player player, final String identifier) {
		if (player == null)
			return "";

		final CombatPlayer p = plugin.getPlayerManager().get(player);
		switch (identifier) {
		case "in_combat":
			return getPAPIBoolean(p.isInCombat());
		case "pvp_status":
			return getPAPIBoolean(p.hasPvPEnabled());
		case "is_newbie":
			return getPAPIBoolean(p.isNewbie());
		case "has_override":
			return getPAPIBoolean(p.hasOverride());
		case "has_respawn_prot":
			return getPAPIBoolean(p.hasRespawnProtection());
		case "combat_prefix":
			return p.isInCombat() ? Conf.NAMETAG_PREFIX.asString() : "";
		case "combat_timeleft":
			return String.valueOf(Math.round(p.getTagTimeLeft() / 1000D));
		case "combat_timeleft_ms":
			return String.valueOf(Utils.roundTo1Decimal(p.getTagTimeLeft() / 1000D));
		case "newbie_timeleft":
			return String.valueOf(p.getNewbieTimeLeft() / 1000);
		case "pvp_command_timeleft":
			final int timeLeft = CombatUtils.getTimeLeft(p.getToggleTime(), Conf.TOGGLE_COOLDOWN.asInt());
			return String.valueOf(Math.max(timeLeft, 0));
		case "player_health":
			return df.format(p.getPlayer().getHealth());
		case "current_enemy":
			final CombatPlayer pPlayer = p.getEnemy();
			return pPlayer != null ? pPlayer.getName() : "None";
		case "current_enemy_health":
			final CombatPlayer enemy = p.getEnemy();
			return enemy != null ? df.format(enemy.getPlayer().getHealth()) : "0";
		case "global_pvp_status":
			return getPAPIBoolean(plugin.getPlayerManager().getGlobalStatus());
		default:
			return null;
		}
	}

	private String getPAPIBoolean(final boolean value) {
		if (value)
			return Conf.PLACEHOLDER_BOOLEAN_TRUE.asString();
		else
			return Conf.PLACEHOLDER_BOOLEAN_FALSE.asString();
	}

	private void registerPlaceholder(final String name) {
		placeholders.add("pvpmanager_" + name);
	}

	public List<String> getPlaceholders() {
		return placeholders;
	}

}