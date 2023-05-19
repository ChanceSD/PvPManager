package me.NoChance.PvPManager.Player.nametag;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.ChatUtils;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.NoChance.PvPManager.Utils.Log;

public class NameTag {

	private Team inCombat;
	private Team pvpOn;
	private Team pvpOff;
	private Team previousTeam;
	private String previousTeamName;
	private final PvPlayer pvPlayer;
	private final String id;
	private final String prefix;
	private final String pvpOnPrefix;
	private final String pvpOffPrefix;
	private Scoreboard scoreboard;

	public NameTag(final PvPlayer p) {
		this.pvPlayer = p;
		this.id = "PVP-" + processPlayerID(pvPlayer.getUUID());
		this.prefix = ChatUtils.colorize(Settings.getNameTagPrefix());
		this.pvpOnPrefix = Settings.getToggleColorOn().equalsIgnoreCase("none") ? "" : ChatUtils.colorize(Settings.getToggleColorOn());
		this.pvpOffPrefix = Settings.getToggleColorOff().equalsIgnoreCase("none") ? "" : ChatUtils.colorize(Settings.getToggleColorOff());
		setupScoreboard();
		setupTeams();
	}

	private void setupScoreboard() {
		scoreboard = pvPlayer.getPlayer().getScoreboard();
	}

	private void setupTeams() {
		if (Settings.isUseCombatTeam()) {
			if (scoreboard.getTeam(id) != null) {
				inCombat = scoreboard.getTeam(id);
				Log.debug("Combat team with name " + id + " already existed");
			} else {
				inCombat = scoreboard.registerNewTeam(id);
				Log.debug("Creating combat team with name " + id);
				if (Settings.isUseNameTag()) {
					inCombat.setPrefix(prefix);
				}
				if (CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.13")) {
					final ChatColor nameColor = getLastColor(prefix);
					if (nameColor != null) {
						inCombat.setColor(nameColor);
					}
				}
			}
		}
		if (Settings.isToggleNametagsEnabled()) {
			if (!pvpOnPrefix.isEmpty()) {
				if (scoreboard.getTeam("PvPOn") != null) {
					pvpOn = scoreboard.getTeam("PvPOn");
				} else {
					pvpOn = scoreboard.registerNewTeam("PvPOn");
					pvpOn.setCanSeeFriendlyInvisibles(false);
					pvpOn.setPrefix(pvpOnPrefix);
					if (CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.13")) {
						final ChatColor nameColor = getLastColor(pvpOnPrefix);
						if (nameColor != null) {
							pvpOn.setColor(nameColor);
						}
					}
				}
			}
			if (!pvpOffPrefix.isEmpty()) {
				if (scoreboard.getTeam("PvPOff") != null) {
					pvpOff = scoreboard.getTeam("PvPOff");
				} else {
					pvpOff = scoreboard.registerNewTeam("PvPOff");
					pvpOff.setCanSeeFriendlyInvisibles(false);
					pvpOff.setPrefix(pvpOffPrefix);
					if (CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.13")) {
						final ChatColor nameColor = getLastColor(pvpOffPrefix);
						if (nameColor != null) {
							pvpOff.setColor(nameColor);
						}
					}
				}
			}
			// set pvp tag if player has pvp nametags on
			setPvP(pvPlayer.hasPvPEnabled());
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

	public final void setInCombat() {
		storePreviousTeam();
		try {
			inCombat.addEntry(pvPlayer.getName());
		} catch (final IllegalStateException e) {
			Log.info("Failed to add player to combat team");
			Log.info("This warning can be ignored but if it happens often it means one of your plugins is removing PvPManager teams and causing a conflict");
			setupTeams();
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
			Log.debug("Unregistering team: " + inCombat.getName());
			inCombat.unregister();
		} catch (final IllegalStateException e) {
			if (unregisteredSent)
				return;
			unregisteredSent = true;
			Log.severe("Team was already unregistered for player: " + pvPlayer.getName());
		}
	}
}
