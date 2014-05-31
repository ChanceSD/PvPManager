package me.NoChance.PvPManager;

import me.NoChance.PvPManager.Config.Variables;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TeamProfile {

	private static Team inCombat;
	private Team previousTeam;
	private PvPlayer pvPlayer;
	private Scoreboard scoreboard;

	public TeamProfile(PvPlayer p) {
		this.pvPlayer = p;
		scoreboard = p.getPlayer().getScoreboard();
		previousTeam = scoreboard.getPlayerTeam(p.getPlayer());
		if (inCombat == null) {
			inCombat = scoreboard.registerNewTeam("InCombat");
			inCombat.setPrefix(ChatColor.translateAlternateColorCodes('&', Variables.nameTagColor));
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

}
