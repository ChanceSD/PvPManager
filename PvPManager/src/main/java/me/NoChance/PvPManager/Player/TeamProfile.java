package me.NoChance.PvPManager.Player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.Log;

public class TeamProfile {

	private Team inCombat;
	private Team pvpOn;
	private Team pvpOff;
	private Team previousTeam;
	private final PvPlayer pvPlayer;
	private Scoreboard scoreboard;

	public TeamProfile(final PvPlayer p) {
		this.pvPlayer = p;
		setupScoreboard();
		setupTeams();
		try {
			final Team team = scoreboard.getEntryTeam(pvPlayer.getName());
			// player got stuck in this team somehow (server crash?)
			if (team != null && team.getPrefix().equals(ChatColor.translateAlternateColorCodes('&', Settings.getNameTagColor()))) {
				team.removeEntry(pvPlayer.getName());
			}
		} catch (final NoSuchMethodError e) {
			Settings.setUseNameTag(false);
			Settings.setToggleNametagsEnabled(false);
			Log.severe("Colored nametags disabled. You need to update your Spigot version.");
		}
	}

	private void setupScoreboard() {
		scoreboard = pvPlayer.getPlayer().getScoreboard();
		if (scoreboard == null) {
			scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
			pvPlayer.getPlayer().setScoreboard(scoreboard);
		}
	}

	private void setupTeams() {
		final String id = pvPlayer.getUUID().toString().replaceAll("-", "").substring(0, 16);
		if (scoreboard.getTeam(id) != null) {
			inCombat = scoreboard.getTeam(id);
		} else {
			inCombat = scoreboard.registerNewTeam(id);
			inCombat.setPrefix(ChatColor.translateAlternateColorCodes('&', Settings.getNameTagColor()));
		}
		if (Settings.isToggleNametagsEnabled()) {
			if (!Settings.getToggleColorOn().equalsIgnoreCase("none")) {
				if (scoreboard.getTeam("PvPOn") != null) {
					pvpOn = scoreboard.getTeam("PvPOn");
				} else {
					pvpOn = scoreboard.registerNewTeam("PvPOn");
					pvpOn.setPrefix(ChatColor.translateAlternateColorCodes('&', Settings.getToggleColorOn()));
					pvpOn.setCanSeeFriendlyInvisibles(false);
				}
			}
			if (!Settings.getToggleColorOff().equalsIgnoreCase("none")) {
				if (scoreboard.getTeam("PvPOff") != null) {
					pvpOff = scoreboard.getTeam("PvPOff");
				} else {
					pvpOff = scoreboard.registerNewTeam("PvPOff");
					pvpOff.setPrefix(ChatColor.translateAlternateColorCodes('&', Settings.getToggleColorOff()));
					pvpOff.setCanSeeFriendlyInvisibles(false);
				}
			}
		}
	}

	public final void setInCombat() {
		final Team team = scoreboard.getEntryTeam(pvPlayer.getName());
		if (team != null && !team.equals(inCombat)) {
			previousTeam = team;
		}
		inCombat.addEntry(pvPlayer.getName());
	}

	public final void restoreTeam() {
		try {
			if (previousTeam != null && scoreboard.getTeam(previousTeam.getName()) != null) {
				previousTeam.addEntry(pvPlayer.getName());
			} else {
				inCombat.removeEntry(pvPlayer.getName());
			}
		} catch (final IllegalStateException e) {
			// Some plugin is unregistering teams when it shouldn't
			Log.severe("Error restoring nametag for: " + pvPlayer.getName());
		}
	}

	public final void setPvP(final boolean state) {
		if (state) {
			if (pvpOn == null) {
				restoreTeam();
			} else {
				pvpOn.addEntry(pvPlayer.getName());
			}
		} else if (pvpOff == null) {
			restoreTeam();
		} else {
			pvpOff.addEntry(pvPlayer.getName());
		}
	}

	public void removeCombatTeam() {
		try {
			inCombat.unregister();
		} catch (final IllegalStateException e) {
			Log.severe("Team was already unregistered for player: " + pvPlayer.getName());
		}
	}
}
