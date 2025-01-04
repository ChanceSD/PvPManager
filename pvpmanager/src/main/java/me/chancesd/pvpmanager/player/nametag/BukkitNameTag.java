package me.chancesd.pvpmanager.player.nametag;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.MCVersion;

public class BukkitNameTag extends NameTag {

	private Team inCombat;
	private Team pvpOnTeam;
	private Team pvpOffTeam;
	private Team previousTeam;
	private String previousTeamName;
	@NotNull
	private final String combatTeamID;
	private final Scoreboard scoreboard;
	private static final String PVPOFF = "PvPOff";
	private static final String PVPON = "PvPOn";
	private static final String HEALTHOBJ = "PvP_Health";
	private Objective health;

	public BukkitNameTag(final CombatPlayer p) {
		super(p);
		this.combatTeamID = "PVP-" + processPlayerID(pvPlayer.getUUID());
		this.scoreboard = pvPlayer.getPlayer().getScoreboard();
		setup();
	}

	@SuppressWarnings({ "deprecation" })
	private void setup() {
		setupCombatTeam();
		if (Conf.TOGGLE_NAMETAG_ENABLED.asBool()) {
			pvpOnTeam = setupToggleTeam(pvpOnPrefix, PVPON);
			pvpOffTeam = setupToggleTeam(pvpOffPrefix, PVPOFF);
			// set pvp tag if player has pvp nametags on
			setPvP(pvPlayer.hasPvPEnabled());
		}
		if (Conf.HEALTH_BELOW_NAME.asBool() && (health == null || scoreboard.getObjective(HEALTHOBJ) == null)) {
			if (scoreboard.getObjective(HEALTHOBJ) != null) {
				health = scoreboard.getObjective(HEALTHOBJ);
			} else if (MCVersion.isAtLeast(MCVersion.V1_19)) {
				health = scoreboard.registerNewObjective(HEALTHOBJ, Criteria.HEALTH, Conf.HEALTH_BELOW_NAME_SYMBOL.asString(), RenderType.HEARTS);
				health.setDisplaySlot(DisplaySlot.BELOW_NAME);
			} else {
				health = scoreboard.registerNewObjective(HEALTHOBJ, "health");
				health.setDisplayName(Conf.HEALTH_BELOW_NAME_SYMBOL.asString());
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

	private void setupCombatTeam() {
		if (combatPrefix.isEmpty() && combatSuffix.isEmpty())
			return;
		inCombat = registerTeam(combatTeamID);
		Log.debug("Creating combat team with name " + combatTeamID);
		inCombat.setPrefix(combatPrefix.replace(" &8(&7%pvpmanager_combat_timeleft%s&8)", ""));
		setTeamColor(inCombat, combatPrefix);
	}

	private void setTeamColor(final Team team, final String teamPrefix) {
		if (MCVersion.isAtLeast(MCVersion.V1_13)) {
			final ChatColor nameColor = getLastColor(teamPrefix);
			if (nameColor != null) {
				team.setColor(nameColor);
			}
		}
	}

	private Team setupToggleTeam(final String teamPrefix, @NotNull final String teamName) {
		if (teamPrefix.isEmpty())
			return null;
		final Team toggleTeam = registerTeam(teamName);
		toggleTeam.setCanSeeFriendlyInvisibles(false);
		toggleTeam.setPrefix(teamPrefix);
		setTeamColor(toggleTeam, teamPrefix);
		return toggleTeam;
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
				addToTeam(pvpOnTeam, pvPlayer.getName());
			}
		} else if (pvpOffTeam == null) {
			restoreNametag();
		} else {
			addToTeam(pvpOffTeam, pvPlayer.getName());
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
