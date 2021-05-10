package me.NoChance.PvPManager.Player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.NoChance.PvPManager.Utils.Log;

public class TeamProfile {

	private Team inCombat;
	private Team pvpOn;
	private Team pvpOff;
	private Team previousTeam;
	private String previousTeamName;
	private final PvPlayer pvPlayer;
	private Scoreboard scoreboard;

	public TeamProfile(final PvPlayer p) {
		this.pvPlayer = p;
		setupScoreboard();
		setupTeams();
	}

	private void setupScoreboard() {
		scoreboard = pvPlayer.getPlayer().getScoreboard();
		if (scoreboard == null) {
			scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
			pvPlayer.getPlayer().setScoreboard(scoreboard);
		}
	}

	private void setupTeams() {
		final String id = "PVP-" + pvPlayer.getUUID().toString().replaceAll("-", "").substring(0, 12);
		if (scoreboard.getTeam(id) != null) {
			inCombat = scoreboard.getTeam(id);
		} else {
			inCombat = scoreboard.registerNewTeam(id);
			if (Settings.isUseNameTag()) {
				inCombat.setPrefix(ChatColor.translateAlternateColorCodes('&', Settings.getNameTagColor()));
			}
			if (CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.13") && Settings.getTeamColor() != null) {
				inCombat.setColor(Settings.getTeamColor());
			}
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
			// set pvp tag if player has pvp nametags on
			setPvP(pvPlayer.hasPvPEnabled());
		}
	}

	public final void setInCombat() {
		storePreviousTeam();
		try {
			inCombat.addEntry(pvPlayer.getName());
		} catch (final IllegalStateException e) {
			Log.info("Failed to add player to combat team");
			Log.info("This warning can be ignored but if it happens often it means one of your plugins is removing PvPManager teams and causing a conflict");
			setupTeams();
			inCombat.addEntry(pvPlayer.getName());
		}
	}

	private void storePreviousTeam() {
		try {
			final Team team = scoreboard.getEntryTeam(pvPlayer.getName());
			if (team != null && !team.equals(inCombat)) {
				previousTeam = team;
				previousTeamName = team.getName();
			}
		} catch (final IllegalStateException e) {
			previousTeamName = null;
			Log.debug("Failed to store previous team: " + e.getMessage());
		}
	}

	private static boolean restoringSent;

	public final void restoreTeam() {
		try {
			if (previousTeamName != null && scoreboard.getTeam(previousTeamName) != null) {
				previousTeam.addEntry(pvPlayer.getName());
			} else {
				inCombat.removeEntry(pvPlayer.getName());
			}
		} catch (final IllegalStateException e) {
			if (restoringSent)
				return;
			restoringSent = true;
			// Some plugin is unregistering teams when it shouldn't
			Log.severe("Error restoring nametag for: " + pvPlayer.getName());
		} finally {
			previousTeamName = null;
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

	private static boolean unregisteredSent;

	public void removeCombatTeam() {
		try {
			inCombat.unregister();
		} catch (final IllegalStateException e) {
			if (unregisteredSent)
				return;
			unregisteredSent = true;
			Log.severe("Team was already unregistered for player: " + pvPlayer.getName());
		}
	}
}
