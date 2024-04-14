package me.chancesd.pvpmanager.player.nametag;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Settings.Settings;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.MCVersion;

public class BukkitNameTag extends NameTag {

	private Team inCombat;
	private Team pvpOnTeam;
	private Team pvpOffTeam;
	private Team previousTeam;
	private String previousTeamName;
	private final String combatTeamID;
	private final Scoreboard scoreboard;
	private static final String PVPOFF = "PvPOff";
	private static final String PVPON = "PvPOn";
	private static final String HEALTHOBJ = "PvP_Health";
	private static Objective health;

	public BukkitNameTag(final PvPlayer p) {
		super(p);
		this.combatTeamID = "PVP-" + processPlayerID(pvPlayer.getUUID());
		this.scoreboard = pvPlayer.getPlayer().getScoreboard();
		setup();
	}

	private void setup() {
		if (!combatPrefix.isEmpty() || !combatSuffix.isEmpty()) {
			inCombat = registerTeam(combatTeamID);
			Log.debug("Creating combat team with name " + combatTeamID);
			inCombat.setPrefix(combatPrefix);
			if (MCVersion.isAtLeast(MCVersion.V1_13)) {
				final ChatColor nameColor = getLastColor(combatPrefix);
				if (nameColor != null) {
					inCombat.setColor(nameColor);
				}
			}
		}
		if (Settings.isToggleNametagsEnabled()) {
			if (!pvpOnPrefix.isEmpty()) {
				pvpOnTeam = registerTeam(PVPON);
				pvpOnTeam.setCanSeeFriendlyInvisibles(false);
				pvpOnTeam.setPrefix(pvpOnPrefix);
				if (MCVersion.isAtLeast(MCVersion.V1_13)) {
					final ChatColor nameColor = getLastColor(pvpOnPrefix);
					if (nameColor != null) {
						pvpOnTeam.setColor(nameColor);
					}
				}
			}
			if (!pvpOffPrefix.isEmpty()) {
				pvpOffTeam = registerTeam(PVPOFF);
				pvpOffTeam.setCanSeeFriendlyInvisibles(false);
				pvpOffTeam.setPrefix(pvpOffPrefix);
				if (MCVersion.isAtLeast(MCVersion.V1_13)) {
					final ChatColor nameColor = getLastColor(pvpOffPrefix);
					if (nameColor != null) {
						pvpOffTeam.setColor(nameColor);
					}
				}
			}
			// set pvp tag if player has pvp nametags on
			setPvP(pvPlayer.hasPvPEnabled());
		}
		if (Settings.isHealthBelowName() && health == null) {
			if (scoreboard.getObjective(HEALTHOBJ) != null) {
				health = scoreboard.getObjective(HEALTHOBJ);
			} else {
				health = scoreboard.registerNewObjective(HEALTHOBJ, "health", Settings.getHealthBelowNameSymbol());
				health.setDisplaySlot(DisplaySlot.BELOW_NAME);
			}
		}
	}

	private Team registerTeam(final String teamID) {
		synchronized (scoreboard) {
			if (scoreboard.getTeam(teamID) != null)
				return scoreboard.getTeam(teamID);
			return scoreboard.registerNewTeam(teamID);
		}
	}

	private void addToTeam(final Team team, final String entry) {
		synchronized (scoreboard) {
			team.addEntry(entry);
		}
	}

	private void removeFromTeam(final Team team, final String entry) {
		synchronized (scoreboard) {
			team.removeEntry(entry);
		}
	}

	private String processPlayerID(final UUID uuid) {
		final String idResult = uuid.toString().replace("-", "");
		if (idResult.startsWith("000000000000"))
			return idResult.substring(17, 29);
		else
			return idResult.substring(0, 12);
	}

	private ChatColor getLastColor(final String string) {
		final String lastColors = ChatColor.getLastColors(string);
		if (lastColors.isEmpty())
			return null;
		return ChatColor.getByChar(lastColors.replace("§", ""));
	}

	@Override
	public final synchronized void setInCombat() {
		storePreviousTeam();
		try {
			if (inCombat != null) { // combat nametags off and toggle nametags on
				addToTeam(inCombat, pvPlayer.getName());
			}
		} catch (final IllegalStateException e) {
			Log.severe("Failed to add player to combat team");
			Log.info(
					"This warning can be ignored but if it happens often it means one of your plugins is removing PvPManager teams and causing a conflict");
		}
	}

	private void storePreviousTeam() {
		final Team team = scoreboard.getEntryTeam(pvPlayer.getName());
		if (team != null && !team.equals(inCombat)) {
			previousTeam = team;
			previousTeamName = team.getName();
		}
	}

	@Override
	public final synchronized void restoreNametag() {
		try {
			if (previousTeamName != null && scoreboard.getTeam(previousTeamName) != null) {
				addToTeam(previousTeam, pvPlayer.getName());
			} else if (inCombat != null) { // combat nametags off and toggle nametags on
				removeFromTeam(inCombat, pvPlayer.getName());
			}
		} catch (final IllegalStateException e) {
			// Some plugin is unregistering teams when it shouldn't
			Log.warning("Error restoring nametag for: " + pvPlayer.getName(), e);
		} finally {
			previousTeamName = null;
		}
	}

	@Override
	public final synchronized void setPvP(final boolean state) {
		if (state) {
			if (pvpOnTeam == null) {
				restoreNametag();
			} else {
				addToTeam(pvpOn, pvPlayer.getName());
			}
		} else if (pvpOffTeam == null) {
			restoreNametag();
		} else {
			addToTeam(pvpOff, pvPlayer.getName());
		}
	}

	@Override
	public synchronized void cleanup() {
		if (inCombat == null) // combat nametags off and toggle nametags on
			return;

		try {
			synchronized (scoreboard) {
				if (teamExists(combatTeamID)) {
					Log.debug("Unregistering team: " + inCombat.getName());
					inCombat.unregister();
					inCombat = null;
				}
			}
		} catch (final IllegalStateException e) {
			Log.severe("Team was already unregistered for player: " + pvPlayer.getName(), e);
		}
	}

	private boolean teamExists(final String teamID) {
		return scoreboard.getTeam(teamID) != null;
	}

}
