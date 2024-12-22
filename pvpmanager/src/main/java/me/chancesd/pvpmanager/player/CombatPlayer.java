package me.chancesd.pvpmanager.player;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

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
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.hook.TABHook;
import me.chancesd.pvpmanager.library.rollbar.PMRUncaughExceptionHandler;
import me.chancesd.pvpmanager.player.nametag.BukkitNameTag;
import me.chancesd.pvpmanager.player.nametag.NameTag;
import me.chancesd.pvpmanager.player.nametag.TABNameTag;
import me.chancesd.pvpmanager.player.world.CombatWorld;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.ItemCooldown;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.storage.fields.UserDataFields;
import me.chancesd.pvpmanager.tasks.NewbieTask;
import me.chancesd.pvpmanager.tasks.TagTask;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.sdutils.scheduler.ScheduleUtils;
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
	private final TagTask tagTask;
	private NameTag nametag;
	private static ExecutorService executor;

	public CombatPlayer(@NotNull final Player player, final PvPManager plugin, final TagTask tagTask) {
		super(player, plugin.getDependencyManager().getEconomy());
		this.pvpState = Conf.DEFAULT_PVP.asBool();
		this.plugin = plugin;
		this.tagTask = tagTask;
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
			untag(UntagReason.KILLED_ENEMY);
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
			message(Lang.NEWBIE_PROTECTION.msgTimeUntil(newbieTask.getFinishTime()));
		} else if (this.newbie && newbieTask != null) {
			if (newbieTask.isExpired()) {
				message(Lang.NEWBIE_PROTECTION_END);
			} else {
				message(Lang.NEWBIE_PROTECTION_REMOVED);
				newbieTask.cancel();
			}
		} else {
			message(Lang.ERROR_NOT_NEWBIE);
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
	public final void tag(final boolean isAttacker, final CombatPlayer other, final long timeMiliseconds) {
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

		if (Conf.GLOWING_IN_COMBAT.asBool() && MCVersion.isAtLeast(MCVersion.V1_9)) {
			getPlayer().setGlowing(true);
		}

		if (nametag != null) {
			executor.execute(nametag::setInCombat);
		}

		final String message;
		final String actionBarMessage;
		if (isAttacker) {
			message = Lang.TAGGED_ATTACKER.msg(other.getName());
			actionBarMessage = Lang.TAGGED_ATTACKER_ACTIONBAR.msg(other.getName());
		} else {
			message = Lang.TAGGED_DEFENDER.msg(other.getName());
			actionBarMessage = Lang.TAGGED_DEFENDER_ACTIONBAR.msg(other.getName());
		}
		message(message);
		sendActionBar(actionBarMessage, 400);

		this.tagged = true;
		this.totalTagTime = timeMiliseconds;
		tagTask.startTracking(this);
	}

	/**
	 * Places this CombatPlayer in combat or renews their current tag
	 *
	 * @param isAttacker Whether this player initiated the attack or not
	 * @param other      The other player involved in the attack
	 */
	public final void tag(final boolean isAttacker, final CombatPlayer other) {
		tag(isAttacker, other, Conf.TIME_IN_COMBAT.asInt() * 1000L);
	}

	/**
	 * Takes the player out of combat
	 */
	public final void untag(final UntagReason reason) {
		final PlayerUntagEvent event = new PlayerUntagEvent(getPlayer(), this, reason);
		ScheduleUtils.ensureMainThread(() -> Bukkit.getPluginManager().callEvent(event), getPlayer());

		if (Conf.GLOWING_IN_COMBAT.asBool() && MCVersion.isAtLeast(MCVersion.V1_9)) {
			getPlayer().setGlowing(false);
		}

		if (nametag != null) {
			executor.execute(nametag::restoreNametag);
		}

		message(Lang.OUT_OF_COMBAT);
		sendActionBar(Lang.OUT_OF_COMBAT_ACTIONBAR.msg());

		this.lastHitters.clear();
		this.tagged = false;
		tagTask.stopTracking(this);
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

		if (nametag != null && Conf.TOGGLE_NAMETAG_ENABLED.asBool()) {
			nametag.setPvP(pvpState);
		}
		if (!pvpState) {
			message(Lang.PVPDISABLED);
			CombatUtils.executeCommands(Conf.COMMANDS_PVP_OFF.asList(), getPlayer(), getName());
		} else {
			message(Lang.PVPENABLED);
			CombatUtils.executeCommands(Conf.COMMANDS_PVP_ON.asList(), getPlayer(), getName());
		}
	}

	public final void addVictim(final Player victimPlayer) {
		final String victimName = victimPlayer.getName();
		if (!victim.containsKey(victimName)) {
			victim.put(victimName, 1);
		} else {
			int totalKills = victim.get(victimName);
			if (totalKills < Conf.KILL_ABUSE_MAX.asInt()) {
				totalKills++;
				victim.put(victimName, totalKills);
			}
			if (Conf.KILL_ABUSE_WARN.asBool() && totalKills + 1 == Conf.KILL_ABUSE_MAX.asInt()) {
				message(Lang.KILL_ABUSE_WARNING.msg());
			}
			if (totalKills >= Conf.KILL_ABUSE_MAX.asInt()) {
				untag(UntagReason.KICKED);
				CombatUtils.executeCommands(Conf.KILL_ABUSE_COMMANDS.asList(), getPlayer(), getName(), victimName);
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

	public final void setItemCooldown(@NotNull final Material material, final ItemCooldown cooldown) {
		final int time = isInCombat() ? cooldown.getCombatCooldown() : cooldown.getGlobalCooldown();
		if (time < 0)
			return;
		itemCooldown.put(material, System.currentTimeMillis() + time * 1000);
		if (MCVersion.isAtLeast(MCVersion.V1_11_2)) {
			getPlayer().setCooldown(material, time * 20);
		}
	}

	public final Long getItemCooldown(final Material material) {
		return itemCooldown.getOrDefault(material, 0L);
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
		if (CombatUtils.hasTimePassed(respawnTime, Conf.RESPAWN_PROTECTION.asInt())) {
			respawnTime = 0;
			return false;
		}
		return !hasPerm(Permissions.EXEMPT_PROTECTION_RESPAWN);
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
		} else if (Conf.NEWBIE_ENABLED.asBool() && !getPlayer().hasPlayedBefore()) {
			setNewbie(true);
		}
		if (getCombatWorld().isPvPForced() != CombatWorld.WorldOptionState.NONE) {
			this.pvpState = getCombatWorld().isPvPForced() == CombatWorld.WorldOptionState.ON;
		}
		if (Conf.NAMETAG_COMBAT_ENABLED.asBool() || Conf.TOGGLE_NAMETAG_ENABLED.asBool()) {
			try {
				final TABHook tab = (TABHook) plugin.getDependencyManager().getDependency(Hook.TAB);
				this.nametag = tab != null && (tab.showAboveHead() || tab.showInPlayerlist()) ? new TABNameTag(tab, this) : new BukkitNameTag(this);
			} catch (final NoSuchMethodError e) {
				Conf.NAMETAG_COMBAT_ENABLED.disable();
				Conf.TOGGLE_NAMETAG_ENABLED.disable();
				this.nametag = null;
				Log.warning("Colored nametags disabled. You need to update your Spigot version.");
			} catch (final UnsupportedOperationException e) {
				Conf.NAMETAG_COMBAT_ENABLED.disable();
				Conf.TOGGLE_NAMETAG_ENABLED.disable();
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
		if (nametag != null) {
			nametag.cleanup();
		}
		executor.execute(() -> plugin.getStorageManager().getStorage().saveUserData(this));
	}

	@Nullable
	public NameTag getNameTag() {
		return nametag;
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
		return PvPManager.getInstance().getPlayerManager().get(player);
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
