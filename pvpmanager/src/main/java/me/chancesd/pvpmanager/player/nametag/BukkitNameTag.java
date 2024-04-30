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
import me.chancesd.pvpmanager.setting.Settings;
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
	private static Objective health;

	public BukkitNameTag(final CombatPlayer p) {
		super(p);
		this.combatTeamID = "PVP-" + processPlayerID(pvPlayer.getUUID());
		this.scoreboard = pvPlayer.getPlayer().getScoreboard();
		setup();
	}

	@SuppressWarnings({ "deprecation", "null" })
	private void setup() {
		if (!combatPrefix.isEmpty() || !combatSuffix.isEmpty()) {
			if (scoreboard.getTeam(combatTeamID) != null) {
				inCombat = scoreboard.getTeam(combatTeamID);
			} else {
				inCombat = scoreboard.registerNewTeam(combatTeamID);
				Log.debug("Creating combat team with name " + combatTeamID);
				inCombat.setPrefix(combatPrefix);
				if (MCVersion.isAtLeast(MCVersion.V1_13)) {
					final ChatColor nameColor = getLastColor(combatPrefix);
					if (nameColor != null) {
						inCombat.setColor(nameColor);
					}
				}
			}
		}
		if (Settings.isToggleNametagsEnabled()) {
			if (!pvpOnPrefix.isEmpty()) {
				if (scoreboard.getTeam(PVPON) != null) {
					pvpOnTeam = scoreboard.getTeam(PVPON);
				} else {
					pvpOnTeam = scoreboard.registerNewTeam(PVPON);
					pvpOnTeam.setCanSeeFriendlyInvisibles(false);
					pvpOnTeam.setPrefix(pvpOnPrefix);
					if (MCVersion.isAtLeast(MCVersion.V1_13)) {
						final ChatColor nameColor = getLastColor(pvpOnPrefix);
						if (nameColor != null) {
							pvpOnTeam.setColor(nameColor);
						}
					}
				}
			}
			if (!pvpOffPrefix.isEmpty()) {
				if (scoreboard.getTeam(PVPOFF) != null) {
					pvpOffTeam = scoreboard.getTeam(PVPOFF);
				} else {
					pvpOffTeam = scoreboard.registerNewTeam(PVPOFF);
					pvpOffTeam.setCanSeeFriendlyInvisibles(false);
					pvpOffTeam.setPrefix(pvpOffPrefix);
					if (MCVersion.isAtLeast(MCVersion.V1_13)) {
						final ChatColor nameColor = getLastColor(pvpOffPrefix);
						if (nameColor != null) {
							pvpOffTeam.setColor(nameColor);
						}
					}
				}
			}
			// set pvp tag if player has pvp nametags on
			setPvP(pvPlayer.hasPvPEnabled());
		}
		if (Settings.isHealthBelowName() && (health == null || scoreboard.getObjective(HEALTHOBJ) == null)) {
			if (scoreboard.getObjective(HEALTHOBJ) != null) {
				health = scoreboard.getObjective(HEALTHOBJ);
			} else if (MCVersion.isAtLeast(MCVersion.V1_19)) {
				health = scoreboard.registerNewObjective(HEALTHOBJ, Criteria.HEALTH, Settings.getHealthBelowNameSymbol(), RenderType.HEARTS);
				health.setDisplaySlot(DisplaySlot.BELOW_NAME);
			} else {
				health = scoreboard.registerNewObjective(HEALTHOBJ, "health");
				health.setDisplayName(Settings.getHealthBelowNameSymbol());
				health.setDisplaySlot(DisplaySlot.BELOW_NAME);
			}
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
	public final void setInCombat() {
		storePreviousTeam();
		try {
			inCombat.addEntry(pvPlayer.getName());
		} catch (final IllegalStateException e) {
			Log.info("Failed to add player to combat team");
			Log.info(
					"This warning can be ignored but if it happens often it means one of your plugins is removing PvPManager teams and causing a conflict");
			setup();
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

	@Override
	public final void restoreNametag() {
		try {
			if (previousTeamName != null && scoreboard.getTeam(previousTeamName) != null) {
				previousTeam.addEntry(pvPlayer.getName());
			} else {
				inCombat.removeEntry(pvPlayer.getName());
			}
		} catch (final IllegalStateException e) {
			// Some plugin is unregistering teams when it shouldn't
			Log.warning("Error restoring nametag for: " + pvPlayer.getName());
		} finally {
			previousTeamName = null;
		}
	}

	@Override
	public final void setPvP(final boolean state) {
		if (state) {
			if (pvpOnTeam == null) {
				restoreNametag();
			} else {
				pvpOnTeam.addEntry(pvPlayer.getName());
			}
		} else if (pvpOffTeam == null) {
			restoreNametag();
		} else {
			pvpOffTeam.addEntry(pvPlayer.getName());
		}
	}

	@Override
	public void cleanup() {
		try {
			Log.debug("Unregistering team: " + inCombat.getName());
			inCombat.unregister();
		} catch (final IllegalStateException e) {
			Log.warning("Team was already unregistered for player: " + pvPlayer.getName());
		}
	}

}
