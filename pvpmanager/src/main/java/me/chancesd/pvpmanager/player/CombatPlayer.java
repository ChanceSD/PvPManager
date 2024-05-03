package me.chancesd.pvpmanager.player;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import me.chancesd.pvpmanager.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.event.PlayerTagEvent;
import me.chancesd.pvpmanager.event.PlayerTogglePvPEvent;
import me.chancesd.pvpmanager.event.PlayerUntagEvent;
import me.chancesd.pvpmanager.library.rollbar.PMRUncaughExceptionHandler;
import me.chancesd.pvpmanager.player.nametag.BukkitNameTag;
import me.chancesd.pvpmanager.player.nametag.NameTag;
import me.chancesd.pvpmanager.player.world.CombatWorld;
import me.chancesd.pvpmanager.setting.Messages;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.Settings;
import me.chancesd.pvpmanager.storage.fields.UserDataFields;
import me.chancesd.pvpmanager.tasks.NewbieTask;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.pvpmanager.utils.ScheduleUtils;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.MCVersion;

public class CombatPlayer extends EcoPlayer {

	private boolean newbie;
	private boolean tagged;
	private boolean pvpState;
	private boolean pvpLogged;
	private boolean override;
	private boolean loaded;
	private long toggleTime;
	private long respawnTime;
	private long taggedTime;
	private long totalTagTime;
	private NewbieTask newbieTask;
	private CombatPlayer enemy;
	private final Set<CombatPlayer> lastHitters = new HashSet<>();
	private final Map<String, Integer> victim = new HashMap<>();
	private final Map<Material, Long> itemCooldown = new EnumMap<>(Material.class);
	private final PvPManager plugin;
	private NameTag nametag;
	private static ExecutorService executor;

	public CombatPlayer(@NotNull final Player player, final PvPManager plugin) {
		super(player, plugin.getDependencyManager().getEconomy());
		this.pvpState = Settings.isDefaultPvp();
		this.plugin = plugin;
		setCombatWorld(plugin.getWorldManager().getWorld(getPlayer().getWorld()));
		if (!CombatUtils.isNPC(player)) {
			executor.execute(this::loadData);
		}
	}

	public final long getToggleTime() {
		return this.toggleTime;
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
	public CombatPlayer getEnemy() {
		return this.enemy;
	}

	public boolean isEnemyOf(final CombatPlayer enemyPlayer) {
		return this.lastHitters.contains(enemyPlayer);
	}

	public void addEnemy(final CombatPlayer enemyPlayer) {
		if (enemyPlayer == this)
			return;
		this.lastHitters.add(enemyPlayer);
	}

	public boolean removeEnemy(final CombatPlayer enemyPlayer) {
		final boolean success = this.lastHitters.remove(enemyPlayer);
		if (isInCombat() && getEnemies().isEmpty())
			plugin.getPlayerHandler().untag(this);
		return success;
	}

	public Set<CombatPlayer> getEnemies() {
		return this.lastHitters;
	}

	public final void disableFly() {
		getPlayer().setFlying(false);
		getPlayer().setAllowFlight(false);
	}

	public final void setNewbie(final boolean newbie) {
		if (newbie) {
			this.newbieTask = new NewbieTask(this, 0);
			message(Messages.newbieProtection.getMsg(TimeUtil.getDiffMsg(newbieTask.getFinishTime())));
		} else if (this.newbie && newbieTask != null) {
			if (newbieTask.isExpired()) {
				message(Messages.newbieProtectionEnd.getMsg());
			} else {
				message(Messages.newbieProtectionRemoved.getMsg());
				newbieTask.cancel();
			}
		} else {
			message(Messages.errorNotNewbie.getMsg());
		}
		this.newbie = newbie;
	}

	/**
	 * Places this CombatPlayer in combat or renews their current tag
	 *
	 * @param isAttacker      Whether this player initiated the attack or not
	 * @param other           The other player involved in the attack
	 * @param timeMiliseconds How long the player should be tagged for
	 */
	public final void setTagged(final boolean isAttacker, final CombatPlayer other, final long timeMiliseconds) {
		if (hasPerm(Permissions.EXEMPT_COMBAT_TAG)) {
			Log.debug("Not tagging " + getName() + " because player has permission: " + Permissions.EXEMPT_COMBAT_TAG);
			return;
		}

		this.taggedTime = System.currentTimeMillis();
		this.enemy = other;
		addEnemy(other);

		if (tagged)
			return;

		final PlayerTagEvent event = new PlayerTagEvent(getPlayer(), this, isAttacker, other.getPlayer());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;

		if (Settings.isGlowingInCombat() && MCVersion.isAtLeast(MCVersion.V1_9)) {
			getPlayer().setGlowing(true);
		}

		if (nametag != null && Settings.useNameTag()) {
			executor.execute(nametag::setInCombat);
		}

		final String message;
		final String actionBarMessage;
		if (isAttacker) {
			message = Messages.taggedAttacker.getMsg(other.getName());
			actionBarMessage = Messages.taggedAttackerActionbar.getMsg(other.getName());
		} else {
			message = Messages.taggedDefender.getMsg(other.getName());
			actionBarMessage = Messages.taggedDefenderActionbar.getMsg(other.getName());
		}
		message(message);
		sendActionBar(actionBarMessage, 400);

		this.tagged = true;
		this.totalTagTime = timeMiliseconds;
		plugin.getPlayerHandler().addToTagTask(this);
	}

	/**
	 * Places this CombatPlayer in combat or renews their current tag
	 *
	 * @param isAttacker Whether this player initiated the attack or not
	 * @param other      The other player involved in the attack
	 */
	public final void setTagged(final boolean isAttacker, final CombatPlayer other) {
		setTagged(isAttacker, other, Settings.getTimeInCombatMs());
	}

	/**
	 * Takes the player out of combat
	 */
	public final void unTag() {
		final PlayerUntagEvent event = new PlayerUntagEvent(getPlayer(), this);
		ScheduleUtils.ensureMainThread(() -> Bukkit.getPluginManager().callEvent(event), getPlayer());

		if (isOnline()) {
			if (nametag != null && Settings.useNameTag()) {
				nametag.restoreNametag();
			}
			if (Settings.isGlowingInCombat() && MCVersion.isAtLeast(MCVersion.V1_9)) {
				getPlayer().setGlowing(false);
			}

			message(Messages.outOfCombat);
			sendActionBar(Messages.outOfCombatActionbar.getMsg());
		}

		this.lastHitters.clear();
		this.tagged = false;
		plugin.getPlayerHandler().removeFromTagTask(this);
	}

	/**
	 * Toggles PvP for this player
	 *
	 * @param pvpState True to enable PvP, false to disable PvP
	 */
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
			message(Messages.pvpDisabled);
			CombatUtils.executeCommands(Settings.getCommandsPvPOff(), getPlayer(), getName());
		} else {
			message(Messages.pvpEnabled);
			CombatUtils.executeCommands(Settings.getCommandsPvPOn(), getPlayer(), getName());
		}
	}

	public final void addVictim(final Player victimPlayer) {
		final String victimName = victimPlayer.getName();
		if (!victim.containsKey(victimName)) {
			victim.put(victimName, 1);
		} else {
			int totalKills = victim.get(victimName);
			if (totalKills < Settings.getKillAbuseMaxKills()) {
				totalKills++;
				victim.put(victimName, totalKills);
			}
			if (Settings.isKillAbuseWarn() && totalKills + 1 == Settings.getKillAbuseMaxKills()) {
				message(Messages.killAbuseWarning.getMsg());
			}
			if (totalKills >= Settings.getKillAbuseMaxKills()) {
				unTag();
				CombatUtils.executeCommands(Settings.getKillAbuseCommands(), getPlayer(), getName(), victimName);
			}
		}
	}

	/**
	 * The number of times this player killed victimPlayer in the last x seconds
	 * <br>
	 * where x is the time limit defined in the config file
	 *
	 * @param victimPlayer
	 * @return The kill count
	 */
	public final int getKillAbuseCount(final Player victimPlayer) {
		return victim.getOrDefault(victimPlayer.getName(), 0);
	}

	public final boolean hasItemCooldown(final Material material) {
		final Long time = itemCooldown.get(material);
		if (time == null)
			return false;
		if (System.currentTimeMillis() > time) {
			itemCooldown.remove(material);
			return false;
		}
		return true;
	}

	public final void setItemCooldown(@NotNull final Material material, final int time) {
		itemCooldown.put(material, System.currentTimeMillis() + time * 1000);
		if (MCVersion.isAtLeast(MCVersion.V1_11_2)) {
			getPlayer().setCooldown(material, time * 20);
		}
	}

	@Nullable
	public final Long getItemCooldown(final Material material) {
		return itemCooldown.get(material);
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
		return !getPlayer().hasPermission("pvpmanager.bypass.protection.respawn");
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

	public final long getUntagTime() {
		return taggedTime + totalTagTime;
	}

	public final long getTotalTagTime() {
		return totalTagTime;
	}

	public final long getNewbieTimeLeft() {
		return newbieTask != null ? newbieTask.getTimeleft() : 0;
	}

	public long getTagTimeLeft() {
		return Math.max(getUntagTime() - System.currentTimeMillis(), 0);
	}

	private synchronized void loadData() {
		if (plugin.getStorageManager().getStorage().userExists(this)) {
			loadUserData(plugin.getStorageManager().getStorage().getUserData(this));
		} else if (Settings.isNewbieProtectionEnabled() && !getPlayer().hasPlayedBefore()) {
			setNewbie(true);
		}
		if (getCombatWorld().isPvPForced() == CombatWorld.WorldOptionState.ON) {
			this.pvpState = true;
		} else if (getCombatWorld().isPvPForced() == CombatWorld.WorldOptionState.OFF) {
			this.pvpState = false;
		}
		if (Settings.useNameTag()) {
			try {
				this.nametag = new BukkitNameTag(this);
			} catch (final NoSuchMethodError e) {
				Settings.setUseNameTag(false);
				this.nametag = null;
				Log.warning("Colored nametags disabled. You need to update your Spigot version.");
			} catch (final UnsupportedOperationException e) {
				Settings.setUseNameTag(false);
				this.nametag = null;
				Log.infoColor(ChatColor.RED
						+ "Nametag support disabled until Folia supports the scoreboard API or use the TAB plugin with PvPManager premium");
			}
		}
		this.loaded = true;
		notifyAll();
		Log.debug("Finished loading data for " + this + (nametag != null ? " with " + nametag.getClass().getSimpleName() : ""));
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
				this.newbieTask = new NewbieTask(this, timeleft);
			}
		}
	}

	public final Map<String, Object> getUserData() {
		final Map<String, Object> userData = new HashMap<>();
		userData.put(UserDataFields.UUID, getUUID().toString());
		userData.put(UserDataFields.NAME, getName());
		userData.put(UserDataFields.DISPLAYNAME, CombatUtils.truncateString(getPlayer().getDisplayName(), 255));
		userData.put(UserDataFields.PVPSTATUS, hasPvPEnabled());
		userData.put(UserDataFields.TOGGLETIME, getToggleTime());
		userData.put(UserDataFields.NEWBIE, isNewbie());
		userData.put(UserDataFields.NEWBIETIMELEFT, getNewbieTimeLeft());
		userData.put(UserDataFields.LASTSEEN, System.currentTimeMillis());
		return userData;
	}

	public final void cleanForRemoval() {
		if (newbieTask != null) {
			newbieTask.cancel();
		}
		if (nametag != null && Settings.useNameTag()) {
			nametag.cleanup();
		}
		executor.execute(() -> plugin.getStorageManager().getStorage().saveUserData(this));
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
			Log.severe(e.getMessage(), e);
			Thread.currentThread().interrupt();
		}
	}

	public static void shutdownExecutorAndWait() {
		try {
			Log.debug(executor.toString());
			executor.shutdown();
			executor.awaitTermination(5, TimeUnit.SECONDS);
			startExecutor();
		} catch (final InterruptedException e) {
			Log.severe(e.getMessage(), e);
			Thread.currentThread().interrupt();
		}
	}

	public static void startExecutor() {
		executor = ScheduleUtils.newBoundedCachedThreadPool(4, Math.max(4, Runtime.getRuntime().availableProcessors()),
				new ThreadFactoryBuilder().setNameFormat("PvPManager Player Thread - %d")
						.setUncaughtExceptionHandler(new PMRUncaughExceptionHandler()).build());
	}

	/**
	 * @param player the player instance
	 * @return PvPlayer instance for the provided player
	 */
	public static CombatPlayer get(final Player player) {
		return PvPManager.getInstance().getPlayerHandler().get(player);
	}

	@Override
	public boolean equals(final Object obj) {
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
