package me.NoChance.PvPManager;

import me.NoChance.PvPManager.Config.Variables;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TeamProfile {

	private static Team inCombat;
	private static Team pvpOn;
	private static Team pvpOff;
	private Team previousTeam;
	private PvPlayer pvPlayer;
	private Scoreboard scoreboard;

	public TeamProfile(PvPlayer p) {
		this.pvPlayer = p;
		scoreboard = p.getPlayer().getScoreboard();
		previousTeam = scoreboard.getPlayerTeam(p.getPlayer());
		if (inCombat == null) {
			if (scoreboard.getTeam("InCombat") != null)
				inCombat = scoreboard.getTeam("InCombat");
			else
				inCombat = scoreboard.registerNewTeam("InCombat");
			inCombat.setPrefix(ChatColor.translateAlternateColorCodes('&', Variables.nameTagColor));
		}
		if (pvpOn == null) {
			if (scoreboard.getTeam("PvPOn") != null)
				pvpOn = scoreboard.getTeam("PvPOn");
			else
				pvpOn = scoreboard.registerNewTeam("PvPOn");
			pvpOn.setPrefix(ChatColor.translateAlternateColorCodes('&', Variables.toggleColorOn));
		}
		if (pvpOff == null) {
			if (scoreboard.getTeam("PvPOff") != null)
				pvpOff = scoreboard.getTeam("PvPOff");
			else
				pvpOff = scoreboard.registerNewTeam("PvPOff");
			pvpOff.setPrefix(ChatColor.translateAlternateColorCodes('&', Variables.toggleColorOff));
		}
	}

	public void setInCombat() {
		inCombat.addPlayer(pvPlayer.getPlayer());
	}

	public void restoreTeam() {
		if (previousTeam != null && scoreboard.getTeam(previousTeam.getName()) != null)
			previousTeam.addPlayer(pvPlayer.getPlayer());
		else
			inCombat.removePlayer(pvPlayer.getPlayer());
	}

	public void setPvP(boolean state) {
		if (state)
			pvpOn.addPlayer(pvPlayer.getPlayer());
		else
			pvpOff.addPlayer(pvPlayer.getPlayer());
	}
}
