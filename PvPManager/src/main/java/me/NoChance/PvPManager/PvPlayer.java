package me.NoChance.PvPManager;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.NoChance.PvPManager.Player.EcoPlayer;
import me.NoChance.PvPManager.Player.TeamProfile;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Settings.UserDataFields;
import me.NoChance.PvPManager.Tasks.NewbieTask;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.NoChance.PvPManager.Utils.Log;

public class PvPlayer extends EcoPlayer {

	private WeakReference<Player> player;
	private final UUID uuid;
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
		super(plugin.getDependencyManager().getEconomy());
		this.player = new WeakReference<>(player);
		this.uuid = player.getUniqueId();
		this.pvpState = Settings.isDefaultPvp();
		this.plugin = plugin;
		if (Settings.isUseNameTag() || Settings.isToggleNametagsEnabled() || !Settings.getTeamColor().isEmpty()) {
			try {
				this.teamProfile = new TeamProfile(this);
			} catch (final NoSuchMethodError e) {
				Settings.setUseNameTag(false);
				Settings.setToggleNametagsEnabled(false);
				this.teamProfile = null;
				Log.severe("Colored nametags disabled. You need to update your Spigot version.");
			}
		}
	}

	public final String getName() {
		return getPlayer().getName();
	}

	public final UUID getUUID() {
		return uuid;
	}

	@Override
	public final Player getPlayer() {
		return player.get();
	}

	public final String getWorldName() {
		return getPlayer().getWorld().getName();
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
			this.newbieTask = new NewbieTask(this);
			newbieTask.runTaskLater(plugin, newbieTask.getTimeleft() / 50);
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

		if (Settings.isUseNameTag()) {
			teamProfile.setInCombat();
		}
		if (Settings.isGlowingInCombat() && CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.9")) {
			getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Settings.getTimeInCombat() * 20, 1));
		}

		if (attacker) {
			message(Messages.getTaggedAttacker().replace("%p", tagger.getName()));
		} else {
			message(Messages.getTaggedDefender().replace("%p", tagger.getName()));
		}

		this.tagged = true;
		plugin.getPlayerHandler().tag(this);
	}

	public final void unTag() {
		if (isOnline()) {
			if (Settings.isUseNameTag()) {
				teamProfile.restoreTeam();
			}

			message(Messages.getOutOfCombat());
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

	public final void loadState() {
		if (!getPlayer().isOp() && getPlayer().hasPermission("pvpmanager.nopvp")) {
			this.pvpState = false;
		} else if (!getPlayer().hasPlayedBefore()) {
			if (Settings.isNewbieProtectionEnabled()) {
				setNewbie(true);
			}
		}
		// now that the state is definitely loaded, set player team
		if (teamProfile != null) {
			teamProfile.setPvP(pvpState);
		}
	}

	public void loadUserData(final Map<String, Object> userData) {
		if (userData.get(UserDataFields.PVP_STATUS) instanceof Boolean) {
			this.pvpState = (boolean) userData.get(UserDataFields.PVP_STATUS);
		}
		if (userData.get(UserDataFields.TOGGLE_TIME) instanceof Long) {
			this.toggleTime = (long) userData.get(UserDataFields.TOGGLE_TIME);
		}
		if (userData.get(UserDataFields.NEWBIE) instanceof Boolean) {
			this.newbie = (boolean) userData.get(UserDataFields.NEWBIE);
			if (this.newbie) {
				if (userData.get(UserDataFields.NEWBIE_TIMELEFT) instanceof Long) {
					final long timeleft = (long) userData.get(UserDataFields.NEWBIE_TIMELEFT);
					this.newbieTask = new NewbieTask(this);
					newbieTask.runTaskLater(plugin, timeleft / 50);
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
			player = new WeakReference<>(p);
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
