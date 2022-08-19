package me.NoChance.PvPManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

import me.NoChance.PvPManager.Events.PlayerTagEvent;
import me.NoChance.PvPManager.Events.PlayerTogglePvPEvent;
import me.NoChance.PvPManager.Events.PlayerUntagEvent;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Player.EcoPlayer;
import me.NoChance.PvPManager.Player.nametag.NameTag;
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
	private NameTag nametag;
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
		if (getPlayer().hasPermission("pvpmanager.nocombattag"))
			return;

		this.taggedTime = System.currentTimeMillis();
		this.enemy = tagger;

		if (tagged)
			return;

		final PlayerTagEvent event = new PlayerTagEvent(getPlayer(), this, attacker, tagger.getPlayer());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;

		if (nametag != null && Settings.isUseCombatTeam()) {
			nametag.setInCombat();
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
			if (nametag != null && Settings.isUseCombatTeam()) {
				nametag.restoreTeam();
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

		if (nametag != null && Settings.isToggleNametagsEnabled()) {
			nametag.setPvP(pvpState);
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
		if (plugin.getDatabaseManager().userExists(getUUID())) {
			loadUserData(plugin.getDatabaseManager().getUserData(getUUID()));
		} else if (CombatUtils.isReal(getUUID()) && Settings.isNewbieProtectionEnabled() && !getPlayer().hasPlayedBefore()) {
			setNewbie(true);
		}
		if (!getPlayer().hasPermission("*")) {
			if (getPlayer().hasPermission("pvpmanager.forcepvp")) {
				this.pvpState = true;
			} else if (getPlayer().hasPermission("pvpmanager.nopvp")) {
				this.pvpState = false;
			}
		}
		if (Settings.isUseCombatTeam() || Settings.isToggleNametagsEnabled()) {
			try {
				this.nametag = new NameTag(this);
			} catch (final NoSuchMethodError e) {
				Settings.setUseCombatTeam(false);
				Settings.setToggleNametagsEnabled(false);
				this.nametag = null;
				Log.warning("Colored nametags disabled. You need to update your Spigot version.");
			}
		}
		this.loaded = true;
		notifyAll();
		Log.debug("Finished loading data for " + this);
	}

	private void loadUserData(final Map<String, Object> userData) {
		final Object pvpstate = userData.get(UserDataFields.PVPSTATUS);
		if (pvpstate instanceof Integer) {
			this.pvpState = (int) pvpstate != 0;
		} else if (pvpstate instanceof Boolean) {
			this.pvpState = (boolean) pvpstate;
		}
		final Object pvpToggleTime = userData.get(UserDataFields.TOGGLETIME);
		if (pvpToggleTime instanceof Integer || pvpToggleTime instanceof Long) {
			this.toggleTime = ((Number) pvpToggleTime).longValue();
		}
		final Object newbieState = userData.get(UserDataFields.NEWBIE);
		if (newbieState instanceof Integer) {
			this.newbie = (int) newbieState != 0;
		} else if (newbieState instanceof Boolean) {
			this.newbie = (boolean) newbieState;
		}
		if (this.newbie) {
			final Object newbieTime = userData.get(UserDataFields.NEWBIETIMELEFT);
			if (newbieTime instanceof Integer || newbieTime instanceof Long) {
				final long timeleft = ((Number) newbieTime).longValue();
				this.newbieTask = new NewbieTask(this, plugin, timeleft);
			}
		}
	}

	public final Map<String, Object> getUserData() {
		final Map<String, Object> userData = new HashMap<>();
		userData.put(UserDataFields.UUID, getUUID());
		userData.put(UserDataFields.NAME, getName());
		userData.put(UserDataFields.DISPLAYNAME, getPlayer().getDisplayName());
		userData.put(UserDataFields.PVPSTATUS, hasPvPEnabled());
		userData.put(UserDataFields.TOGGLETIME, getToggleTime());
		userData.put(UserDataFields.NEWBIE, isNewbie());
		userData.put(UserDataFields.NEWBIETIMELEFT, newbieTask != null ? newbieTask.getTimeleft() : 0);
		return userData;
	}

	public final void updatePlayer(final Player p) {
		if (!p.equals(getPlayer())) {
			setPlayer(p);
			if (nametag != null) {
				nametag = new NameTag(this);
			}
		}
	}

	public final void cleanForRemoval() {
		if (newbieTask != null) {
			newbieTask.cancel();
		}
		if (nametag != null && Settings.isUseCombatTeam()) {
			nametag.removeCombatTeam();
		}
		executor.execute(() -> plugin.getDatabaseManager().saveUser(this));
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

	public static void shutdownExecutorAndWait() {
		try {
			executor.shutdown();
			executor.awaitTermination(3, TimeUnit.SECONDS);
		} catch (final InterruptedException e) {
			Log.severe(e.getMessage(), e);
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
