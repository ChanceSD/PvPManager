package me.NoChance.PvPManager.Dependencies.Hooks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceHolderAPI extends PlaceholderExpansion {

	private final PvPManager plugin;
	private final List<String> placeholders = new ArrayList<>();

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
	}

	@Override
	public String onPlaceholderRequest(final Player player, final String identifier) {
		if (player == null)
			return "";

		final PvPlayer p = plugin.getPlayerHandler().get(player);
		switch (identifier) {
		case "in_combat":
			return p.isInCombat() ? "true" : "false";
		case "pvp_status":
			return p.hasPvPEnabled() ? "true" : "false";
		case "is_newbie":
			return p.isNewbie() ? "true" : "false";
		case "has_override":
			return p.hasOverride() ? "true" : "false";
		case "has_respawn_prot":
			return p.hasRespawnProtection() ? "true" : "false";
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
		}

		return null;
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
