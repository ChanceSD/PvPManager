package me.NoChance.PvPManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

import me.NoChance.PvPManager.Events.PlayerTagEvent;
import me.NoChance.PvPManager.Events.PlayerTogglePvPEvent;
import me.NoChance.PvPManager.Events.PlayerUntagEvent;
import me.NoChance.PvPManager.Managers.PlayerHandler;
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
	private boolean loaded;
	private long toggleTime;
	private long respawnTime;
	private long taggedTime;
	private NewbieTask newbieTask;
	private PvPlayer enemy;
	private final HashMap<String, Integer> victim = new HashMap<>();
	private final PvPManager plugin;
	private TeamProfile teamProfile;
	private static final ExecutorService executor = Executors.newCachedThreadPool();

	public PvPlayer(final Player player, final PvPManager plugin) {
		super(player, plugin.getDependencyManager().getEconomy());
		this.pvpState = Settings.isDefaultPvp();
		this.plugin = plugin;
		executor.execute(this::loadData);
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
			message(Messages.getErrorPvpCooldown().replace("%m", Long.toString(secondsLeft <= 60 ? secondsLeft : secondsLeft - secondsLeft / 60 * 60)).replace("%t",
			        Long.toString(secondsLeft <= 60 ? 0 : secondsLeft / 60)));
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

	@Nullable
	public PvPlayer getEnemy() {
		return this.enemy;
	}

	public final void disableFly() {
		getPlayer().setFlying(false);
		getPlayer().setAllowFlight(false);
	}

	public final void setNewbie(final boolean newbie) {
		if (newbie) {
			if (PlayerHandler.isRemovedNewbie(this))
				return;
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

		if (teamProfile != null && Settings.isUseCombatTeam()) {
			teamProfile.setInCombat();
		}
		if (Settings.isGlowingInCombat() && CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.9")) {
			getPlayer().setGlowing(true);
		}

		if (attacker) {
			message(Messages.getTaggedAttacker().replace("%p", tagger.getName()));
			sendActionBar(Messages.getTaggedAttackerABar().replace("%p", tagger.getName()));
		} else {
			message(Messages.getTaggedDefender().replace("%p", tagger.getName()));
			sendActionBar(Messages.getTaggedDefenderABar().replace("%p", tagger.getName()));
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
			if (teamProfile != null && Settings.isUseCombatTeam()) {
				teamProfile.restoreTeam();
			}
			if (Settings.isGlowingInCombat() && CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.9")) {
				getPlayer().setGlowing(false); // effect should pass by itself but now players can get untagged before tag expires
			}

			message(Messages.getOutOfCombat());
			sendActionBar(Messages.getOutOfCombatABar());
		}

		this.tagged = false;
	}

	public final void setPvP(final boolean pvpState) {
		if (pvpState == this.pvpState)
			return;

		final PlayerTogglePvPEvent event = new PlayerTogglePvPEvent(getPlayer(), this, pvpState);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;

		this.pvpState = pvpState;
		this.toggleTime = System.currentTimeMillis();

		if (teamProfile != null && Settings.isToggleNametagsEnabled()) {
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
		return newbieTask != null ? newbieTask.getTimeleft() : 0;
	}

	public long getTagTimeLeft() {
		return tagged ? taggedTime + Settings.getTimeInCombat() * 1000 - System.currentTimeMillis() : 0;
	}

	private synchronized void loadData() {
		if (plugin.getConfigM().getUserStorage().contains(getUUID().toString())) {
			loadUserData(plugin.getConfigM().getUserData(getUUID()));
		} else if (CombatUtils.isReal(getUUID()) && Settings.isNewbieProtectionEnabled() && !getPlayer().hasPlayedBefore()) {
			setNewbie(true);
		}
		if (getPlayer().hasPermission("pvpmanager.nopvp")) {
			this.pvpState = false;
		}
		if (Settings.isUseCombatTeam() || Settings.isToggleNametagsEnabled()) {
			try {
				this.teamProfile = new TeamProfile(this);
			} catch (final NoSuchMethodError e) {
				Settings.setUseCombatTeam(false);
				Settings.setToggleNametagsEnabled(false);
				this.teamProfile = null;
				Log.warning("Colored nametags disabled. You need to update your Spigot version.");
			}
		}
		this.loaded = true;
		notifyAll();
		Log.debug("Finished loading data for " + this);
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

	public final void cleanForRemoval() {
		if (newbieTask != null) {
			newbieTask.cancel();
		}
		if (teamProfile != null && Settings.isUseCombatTeam()) {
			teamProfile.removeCombatTeam();
		}
	}

	public boolean isLoaded() {
		return loaded;
	}

	public synchronized void waitForPlayerToLoad() {
		try {
			while (!isLoaded()) {
				Log.debug("Waiting on data loading for " + this);
				wait(100);
			}
		} catch (final InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
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
