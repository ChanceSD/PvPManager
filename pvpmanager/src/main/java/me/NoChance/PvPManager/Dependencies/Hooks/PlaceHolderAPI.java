package me.NoChance.PvPManager.Dependencies.Hooks;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceHolderAPI extends PlaceholderExpansion {

	private final PvPManager plugin;
	private final List<String> placeholders = new ArrayList<>();
	private final DecimalFormat df = new DecimalFormat("#.##");

	public PlaceHolderAPI(final PvPManager plugin) {
		this.plugin = plugin;
		registerPlaceholder("in_combat");
		registerPlaceholder("combat_timeleft");
		registerPlaceholder("pvp_status");
		registerPlaceholder("pvp_command_timeleft");
		registerPlaceholder("is_newbie");
		registerPlaceholder("newbie_timeleft");
		registerPlaceholder("has_override");
		registerPlaceholder("has_respawn_prot");
		registerPlaceholder("current_enemy");
		registerPlaceholder("current_enemy_health");
		registerPlaceholder("combat_prefix");
		registerPlaceholder("global_pvp_status");
	}

	@Override
	public String onPlaceholderRequest(final Player player, final String identifier) {
		if (player == null)
			return "";

		final PvPlayer p = plugin.getPlayerHandler().get(player);
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
			return p.isInCombat() ? Settings.getNameTagPrefix() : "";
		case "combat_timeleft":
			return String.valueOf(p.getTagTimeLeft() / 1000);
		case "newbie_timeleft":
			return String.valueOf(p.getNewbieTimeLeft() / 1000);
		case "pvp_command_timeleft":
			final int timeLeft = CombatUtils.getTimeLeft(p.getToggleTime(), Settings.getToggleCooldown());
			return String.valueOf(timeLeft > 0 ? timeLeft : 0);
		case "current_enemy":
			final PvPlayer pPlayer = p.getEnemy();
			return pPlayer != null ? pPlayer.getName() : "None";
		case "current_enemy_health":
			final PvPlayer enemy = p.getEnemy();
			return enemy != null ? df.format(enemy.getPlayer().getHealth()) : "0";
		case "global_pvp_status":
			return getPAPIBoolean(Settings.isGlobalStatus());
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
