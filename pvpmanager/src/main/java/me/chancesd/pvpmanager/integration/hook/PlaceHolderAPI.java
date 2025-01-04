package me.chancesd.pvpmanager.integration.hook;

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
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceHolderAPI extends PlaceholderExpansion {

	private final PvPManager plugin;
	@NotNull
	private final List<String> placeholders = new ArrayList<>();
	private final DecimalFormat df = new DecimalFormat("#.##");

	public PlaceHolderAPI(final PvPManager plugin) {
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
		registerPlaceholder("player_health");
		registerPlaceholder("current_enemy");
		registerPlaceholder("current_enemy_health");
		registerPlaceholder("combat_prefix");
		registerPlaceholder("global_pvp_status");
	}

	@Override
	public String onPlaceholderRequest(final Player player, final String identifier) {
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
			return String.valueOf(p.getTagTimeLeft() / 1000);
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
			return PlaceholderAPIPlugin.booleanTrue();
		else
			return PlaceholderAPIPlugin.booleanFalse();
	}

	private void registerPlaceholder(final String name) {
		placeholders.add(getIdentifier() + "_" + name);
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public boolean canRegister() {
		return true;
	}

	@SuppressWarnings("null")
	@Override
	public String getAuthor() {
		return plugin.getDescription().getAuthors().toString();
	}

	@Override
	public String getName() {
		return plugin.getDescription().getName();
	}

	@Override
	public String getIdentifier() {
		return "pvpmanager";
	}

	@Override
	public String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public List<String> getPlaceholders() {
		return placeholders;
	}

}
