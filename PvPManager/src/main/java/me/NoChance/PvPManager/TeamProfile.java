package me.NoChance.PvPManager;

import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Utils.Log;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

class TeamProfile {

	private Team inCombat;
	private Team pvpOn;
	private Team pvpOff;
	private Team previousTeam;
	private final PvPlayer pvPlayer;
	private final Scoreboard scoreboard;

	public TeamProfile(final PvPlayer p) {
		this.pvPlayer = p;
		scoreboard = p.getPlayer().getScoreboard();
		setupTeams();
		try {
			previousTeam = scoreboard.getEntryTeam(p.getName());
		} catch (final NoSuchMethodError e) {
			Variables.setUseNameTag(false);
			Variables.setToggleNametagsEnabled(false);
			Log.severe("Colored nametags disabled. Update your Spigot version.");
		}
	}

	private void setupTeams() {
		if (scoreboard.getTeam("InCombat") != null)
			inCombat = scoreboard.getTeam("InCombat");
		else {
			inCombat = scoreboard.registerNewTeam("InCombat");
			inCombat.setPrefix(ChatColor.translateAlternateColorCodes('&', Variables.getNameTagColor()));
			inCombat.setCanSeeFriendlyInvisibles(false);
		}
		if (!Variables.getToggleColorOn().equalsIgnoreCase("none")) {
			if (scoreboard.getTeam("PvPOn") != null)
				pvpOn = scoreboard.getTeam("PvPOn");
			else {
				pvpOn = scoreboard.registerNewTeam("PvPOn");
				pvpOn.setPrefix(ChatColor.translateAlternateColorCodes('&', Variables.getToggleColorOn()));
				pvpOn.setCanSeeFriendlyInvisibles(false);
			}
		}
		if (!Variables.getToggleColorOff().equalsIgnoreCase("none")) {
			if (scoreboard.getTeam("PvPOff") != null)
				pvpOff = scoreboard.getTeam("PvPOff");
			else {
				pvpOff = scoreboard.registerNewTeam("PvPOff");
				pvpOff.setPrefix(ChatColor.translateAlternateColorCodes('&', Variables.getToggleColorOff()));
				pvpOff.setCanSeeFriendlyInvisibles(false);
			}
		}
	}

	public final void setInCombat() {
		if (pvpOn != null || pvpOff != null)
			previousTeam = scoreboard.getEntryTeam(pvPlayer.getName());
		inCombat.addEntry(pvPlayer.getName());
	}

	public final void restoreTeam() {
		try {
			if (previousTeam != null && scoreboard.getTeam(previousTeam.getName()) != null)
				previousTeam.addEntry(pvPlayer.getName());
			else
				inCombat.removeEntry(pvPlayer.getName());
		} catch (final IllegalStateException e) {
			System.out.println("[PvPManager] Error restoring nametag for: " + pvPlayer.getName());
			inCombat.removeEntry(pvPlayer.getName());
		}
	}

	public final void setPvP(final boolean state) {
		if (state) {
			if (pvpOn == null)
				restoreTeam();
			else
				pvpOn.addEntry(pvPlayer.getName());
		} else {
			if (pvpOff == null)
				restoreTeam();
			else
				pvpOff.addEntry(pvPlayer.getName());
		}
	}
}
