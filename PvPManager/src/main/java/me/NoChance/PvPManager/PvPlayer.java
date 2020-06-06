package me.NoChance.PvPManager;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.NoChance.PvPManager.Events.PlayerTagEvent;
import me.NoChance.PvPManager.Events.PlayerUntagEvent;
import me.NoChance.PvPManager.Player.EcoPlayer;
import me.NoChance.PvPManager.Player.TeamProfile;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Settings.UserDataFields;
import me.NoChance.PvPManager.Tasks.NewbieTask;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.NoChance.PvPManager.Utils.Log;

public class PvPlayer extends EcoPlayer {

	private boolean newbie;
	private boolean tagged;
	private boolean pvpState;
	private boolean pvpLogged;
	private boolean override;
	private long toggleTime;
	private long respawnTime;
	private long taggedTime;
	private NewbieTask newbieTask;
	private PvPlayer enemy;
	private final HashMap<String, Integer> victim = new HashMap<>();
	private final PvPManager plugin;
	private TeamProfile teamProfile;

	public PvPlayer(final Player player, final PvPManager plugin) {
		super(player, plugin.getDependencyManager().getEconomy());
		this.pvpState = Settings.isDefaultPvp();
		this.plugin = plugin;
		if (plugin.getConfigM().getUserStorage().contains(player.getUniqueId().toString())) {
			loadUserData(plugin.getConfigM().getUserData(player.getUniqueId()));
		} else {
			loadState();
		}
		if (Settings.isUseNameTag() || Settings.isToggleNametagsEnabled() || !Settings.getTeamColor().isEmpty()) {
			try {
				this.teamProfile = new TeamProfile(this);
			} catch (final NoSuchMethodError e) {
				Settings.setUseNameTag(false);
				Settings.setToggleNametagsEnabled(false);
				this.teamProfile = null;
				Log.warning("Colored nametags disabled. You need to update your Spigot version.");
			}
		}
	}

	public final long getToggleTime() {
		return this.toggleTime;
	}

	public final void togglePvP() {
		if (!hasToggleCooldownPassed())
			return;

		setPvP(!pvpState);
	}

	public final boolean hasToggleCooldownPassed() {
		if (!CombatUtils.hasTimePassed(toggleTime, Settings.getToggleCooldown()) && !getPlayer().hasPermission("pvpmanager.pvpstatus.nocooldown")) {
			final long secondsLeft = CombatUtils.getTimeLeft(toggleTime, Settings.getToggleCooldown());
			message(Messages.getErrorPvpCooldown().replace("%m", Long.toString(secondsLeft <= 60 ? secondsLeft : secondsLeft / 60)));
			return false;
		}
		return true;
	}

	public final boolean isNewbie() {
		return this.newbie;
	}

	public final boolean isInCombat() {
		return this.tagged;
	}

	public final boolean hasPvPEnabled() {
		return this.pvpState;
	}

	public final boolean hasPvPLogged() {
		return this.pvpLogged;
	}

	public final boolean hasOverride() {
		return this.override;
	}

	public PvPlayer getEnemy() {
		return this.enemy;
	}

	public final void disableFly() {
		getPlayer().setFlying(false);
		getPlayer().setAllowFlight(false);
	}

	public final void setNewbie(final boolean newbie) {
		if (newbie) {
			message(Messages.getNewbieProtection().replace("%", Integer.toString(Settings.getNewbieProtectionTime())));
			this.newbieTask = new NewbieTask(this, plugin, 0);
		} else if (this.newbie && newbieTask != null) {
			if (Bukkit.getScheduler().isCurrentlyRunning(newbieTask.getTaskId())) {
				message(Messages.getNewbieProtectionEnd());
			} else {
				message(Messages.getNewbieProtectionRemoved());
				newbieTask.cancel();
			}
		} else {
			message(Messages.getErrorNotNewbie());
		}
		this.newbie = newbie;
	}

	public final void setTagged(final boolean attacker, final PvPlayer tagger) {
		if (getPlayer().hasPermission("pvpmanager.nocombat"))
			return;

		this.taggedTime = System.currentTimeMillis();
		this.enemy = tagger;

		if (tagged)
			return;

		final PlayerTagEvent event = new PlayerTagEvent(getPlayer(), this, attacker, tagger.getPlayer());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;

		if (Settings.isUseNameTag() || CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.13") && !Settings.getTeamColor().isEmpty()) {
			teamProfile.setInCombat();
		}
		if (Settings.isGlowingInCombat() && CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.9")) {
			getPlayer().setGlowing(true);
		}

		if (attacker) {
			final String message = Messages.getTaggedAttacker().replace("%p", tagger.getName());
			message(message);
			if (!Settings.getActionBarMessage().isEmpty()) {
				sendActionBar(message);
			}
		} else {
			final String message = Messages.getTaggedDefender().replace("%p", tagger.getName());
			message(message);
			if (!Settings.getActionBarMessage().isEmpty()) {
				sendActionBar(message);
			}
		}

		this.tagged = true;
		plugin.getPlayerHandler().tag(this);
	}

	public final void unTag() {
		final PlayerUntagEvent event = new PlayerUntagEvent(getPlayer(), this);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;

		if (isOnline()) {
			if (Settings.isUseNameTag() || CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.13") && !Settings.getTeamColor().isEmpty()) {
				teamProfile.restoreTeam();
			}
			if (Settings.isGlowingInCombat() && CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.9")) {
				getPlayer().setGlowing(false); // effect should pass by itself but now players can get untagged before tag expires
			}

			message(Messages.getOutOfCombat());
			if (!Settings.getActionBarMessage().isEmpty()) {
				sendActionBar(Messages.getOutOfCombat());
			}
		}

		this.tagged = false;
	}

	public final void setPvP(final boolean pvpState) {
		this.pvpState = pvpState;
		this.toggleTime = System.currentTimeMillis();
		if (Settings.isToggleNametagsEnabled()) {
			teamProfile.setPvP(pvpState);
		}
		if (!pvpState) {
			message(Messages.getPvpDisabled());
			for (final String s : Settings.getCommandsPvPOff()) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', s.replace("%p", getName())));
			}
		} else {
			message(Messages.getPvpEnabled());
			for (final String s : Settings.getCommandsPvPOn()) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', s.replace("%p", getName())));
			}
		}
	}

	public final void addVictim(final String victimName) {
		if (!victim.containsKey(victimName)) {
			victim.put(victimName, 1);
		} else if (victim.containsKey(victimName)) {
			int totalKills = victim.get(victimName);
			if (totalKills < Settings.getKillAbuseMaxKills()) {
				totalKills++;
				victim.put(victimName, totalKills);
			}
			if (totalKills >= Settings.getKillAbuseMaxKills()) {
				unTag();
				for (final String command : Settings.getKillAbuseCommands()) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("<player>", getName()));
				}
			}
		}
	}

	public final void clearVictims() {
		victim.clear();
	}

	public final void setPvpLogged(final boolean pvpLogged) {
		this.pvpLogged = pvpLogged;
	}

	public final boolean hasRespawnProtection() {
		if (respawnTime == 0)
			return false;
		if (CombatUtils.hasTimePassed(respawnTime, Settings.getRespawnProtection())) {
			respawnTime = 0;
			return false;
		}
		return true;
	}

	public final void setRespawnTime(final long respawnTime) {
		this.respawnTime = respawnTime;
	}

	public final boolean toggleOverride() {
		this.override = !override;
		return this.override;
	}

	public final long getTaggedTime() {
		return taggedTime;
	}

	public final long getNewbieTimeLeft() {
		return newbieTask.getTimeleft();
	}

	public long getTagTimeLeft() {
		return taggedTime + Settings.getTimeInCombat() * 1000 - System.currentTimeMillis();
	}

	private final void loadState() {
		if (!getPlayer().isOp() && getPlayer().hasPermission("pvpmanager.nopvp")) {
			this.pvpState = false;
		} else if (!getPlayer().hasPlayedBefore() && !plugin.getPlayerHandler().isNewbieDisabled(this)) {
			if (Settings.isNewbieProtectionEnabled()) {
				setNewbie(true);
			}
		}
	}

	private void loadUserData(final Map<String, Object> userData) {
		if (userData.get(UserDataFields.PVP_STATUS) instanceof Boolean) {
			this.pvpState = (boolean) userData.get(UserDataFields.PVP_STATUS);
		}
		final Object toggle_time = userData.get(UserDataFields.TOGGLE_TIME);
		if (toggle_time instanceof Integer || toggle_time instanceof Long) {
			this.toggleTime = ((Number) toggle_time).longValue();
		}
		if (userData.get(UserDataFields.NEWBIE) instanceof Boolean) {
			this.newbie = (boolean) userData.get(UserDataFields.NEWBIE);
			if (this.newbie) {
				final Object newbie_time = userData.get(UserDataFields.NEWBIE_TIMELEFT);
				if (newbie_time instanceof Integer || newbie_time instanceof Long) {
					final long timeleft = ((Number) newbie_time).longValue();
					this.newbieTask = new NewbieTask(this, plugin, timeleft);
					message(String.format(Messages.getNewbieTimeCheck(), timeleft / 1000));
				}
			}
		}
	}

	public final Map<String, Object> getUserData() {
		final Map<String, Object> userData = new HashMap<>();
		userData.put(UserDataFields.PVP_STATUS, hasPvPEnabled());
		userData.put(UserDataFields.TOGGLE_TIME, getToggleTime());
		userData.put(UserDataFields.NEWBIE, isNewbie());
		userData.put(UserDataFields.NEWBIE_TIMELEFT, newbieTask != null ? newbieTask.getTimeleft() : 0);
		return userData;
	}

	public final void updatePlayer(final Player p) {
		if (!p.equals(getPlayer())) {
			setPlayer(p);
			if (teamProfile != null) {
				teamProfile = new TeamProfile(this);
			}
		}
	}

	public final void removeCombatTeam() {
		if (teamProfile != null) {
			teamProfile.removeCombatTeam();
		}
	}

	/**
	 * @param player
	 * @return PvPlayer instance for the provided player
	 */
	public static PvPlayer get(final Player player) {
		return PvPManager.getInstance().getPlayerHandler().get(player);
	}

}
