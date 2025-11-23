package me.chancesd.pvpmanager.player;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.event.NewbieProtectionEvent;
import me.chancesd.pvpmanager.event.PlayerTagEvent;
import me.chancesd.pvpmanager.event.PlayerTogglePvPEvent;
import me.chancesd.pvpmanager.event.PlayerUntagEvent;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.hook.TABHook;
import me.chancesd.pvpmanager.player.nametag.BukkitNameTag;
import me.chancesd.pvpmanager.player.nametag.NameTag;
import me.chancesd.pvpmanager.player.nametag.TABNameTag;
import me.chancesd.pvpmanager.player.world.CombatWorld;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.ItemCooldown;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.tasks.NewbieTask;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.sdutils.scheduler.SDTask;
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
	private boolean lastDeathWasPvP;
	private long toggleTime;
	private long respawnTime;
	private volatile long taggedTime;
	private volatile long totalTagTime;
	private long lastKillCommandTime;
	private NewbieTask newbieTask;
	private SDTask pendingUntagTask;
	private CombatPlayer enemy;
	private final Set<CombatPlayer> lastHitters = new HashSet<>();
	private final Map<String, Integer> victim = new HashMap<>();
	private final Map<Material, Long> itemCooldown = new EnumMap<>(Material.class);
	private final PvPManager plugin;
	private NameTag nametag;

	public CombatPlayer(@NotNull final Player player, final PvPManager plugin) {
		super(player, plugin.getDependencyManager().getEconomy());
		this.plugin = plugin;
		this.pvpState = Conf.DEFAULT_PVP.asBool() || CombatUtils.isNPC(player);
		setCombatWorld(plugin.getWorldManager().getWorld(getPlayer().getWorld()));
		initializeNameTag();
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
		this.enemy = enemyPlayer;
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
		if (!isInCombat()) {
			setWasAllowedFlight(getPlayer().getAllowFlight());
		}
		getPlayer().setFlying(false);
		getPlayer().setAllowFlight(false);
	}

	public final void setNewbie(final boolean newbie) {
		setNewbie(newbie, 0);
	}

	public final void setNewbie(final boolean newbie, final long time) {
		if (newbie) {
			this.newbieTask = new NewbieTask(this, time);
		} else if (this.newbie && newbieTask != null) {
			newbieTask.cancel();
			this.newbieTask = null;
		}

		// Fire event if the newbie state actually changed
		if (this.newbie != newbie) {
			final NewbieProtectionEvent event = new NewbieProtectionEvent(getPlayer(), this, newbie);
			ScheduleUtils.ensureMainThread(() -> Bukkit.getPluginManager().callEvent(event));
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
	public final synchronized void tag(final boolean isAttacker, final CombatPlayer other, final long timeMiliseconds) {
		if (hasPerm(Permissions.EXEMPT_COMBAT_TAG)) {
			Log.debug("Not tagging " + getName() + " because player has permission: " + Permissions.EXEMPT_COMBAT_TAG);
			return;
		}

		this.taggedTime = System.currentTimeMillis();
		addEnemy(other);

		if (tagged) {
			return;
		}

		// Cancel any pending untag task
		if (pendingUntagTask != null && !pendingUntagTask.isCancelled()) {
			pendingUntagTask.cancel();
			Log.debug("Cancelled stale untag task for " + getName());
			pendingUntagTask = null;
		}

		this.totalTagTime = timeMiliseconds;

		final PlayerTagEvent event = new PlayerTagEvent(getPlayer(), this, isAttacker, other.getPlayer());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			this.totalTagTime = 0;
			return;
		}

		if (Conf.GLOWING_IN_COMBAT.asBool() && MCVersion.isAtLeast(MCVersion.V1_9)) {
			getPlayer().setGlowing(true);
		}

		getPlayer().closeInventory();

		if (nametag != null) {
			ScheduleUtils.runAsync(nametag::setInCombat);
		}

		if (isAttacker) {
			message(Lang.TAGGED_ATTACKER, other.getName());
		} else {
			message(Lang.TAGGED_DEFENDER, other.getName());
		}

		this.tagged = true;
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
	public final synchronized void untag(final UntagReason reason) {
		if (!isInCombat()) {
			return;
		}
		final PlayerUntagEvent event = new PlayerUntagEvent(getPlayer(), this, reason);
		pendingUntagTask = ScheduleUtils.ensureMainThread(() -> {
			Bukkit.getPluginManager().callEvent(event);
			if (Conf.DISABLE_FLY.asBool() && Conf.RESTORE_FLY.asBool() && getWasAllowedFlight()) {
				getPlayer().setAllowFlight(getWasAllowedFlight()); // Sync because there's an async catcher on MC 1.8
			}
			pendingUntagTask = null;
		}, getPlayer());

		if (Conf.GLOWING_IN_COMBAT.asBool() && MCVersion.isAtLeast(MCVersion.V1_9)) {
			getPlayer().setGlowing(false);
		}

		if (nametag != null) {
			ScheduleUtils.runAsync(nametag::restoreNametag);
		}

		message(Lang.OUT_OF_COMBAT);
		sendActionBar(Lang.OUT_OF_COMBAT_ACTIONBAR.msg());

		this.lastHitters.clear();
		this.tagged = false;
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
				message(Lang.KILL_ABUSE_WARNING);
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

	/**
	 * Returns the time when the cooldown for the specified item will expire
	 *
	 * @param material The item material
	 * @return The time in milliseconds when the cooldown will expire, or 0 if there is no cooldown
	 */
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

	public NewbieTask getNewbieTask() {
		return newbieTask;
	}

	public final long getNewbieTimeLeft() {
		return newbieTask != null ? newbieTask.getTimeleft() : 0;
	}

	public long getTagTimeLeft() {
		if (!tagged) {
			return 0;
		}
		return Math.max(getUntagTime() - System.currentTimeMillis(), 0);
	}

	public boolean canExecuteKillCommand() {
		final int cooldown = Conf.COMMANDS_ON_KILL_COOLDOWN.asInt();
		if (cooldown == -1)
			return true;
		if (!CombatUtils.hasTimePassed(lastKillCommandTime, cooldown)) {
			return false;
		}
		lastKillCommandTime = System.currentTimeMillis();
		return true;
	}

	public boolean wasLastDeathPvP() {
		return lastDeathWasPvP;
	}

	public void setLastDeathWasPvP(final boolean lastDeathWasPvP) {
		this.lastDeathWasPvP = lastDeathWasPvP;
	}

	private void initializeNameTag() {
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
	}

	/**
	 * Apply loaded player data to this CombatPlayer
	 */
	public synchronized void applyPlayerData(final PlayerData data) {
		// Apply loaded data (overriding defaults)
		this.pvpState = data.isPvpEnabled();
		this.toggleTime = data.getToggleTime();
		this.newbie = data.isNewbie();

		if (Conf.NEWBIE_ENABLED.asBool() && (this.newbie && data.getNewbieTimeLeft() > 0 || data.isDefault() && !getPlayer().hasPlayedBefore())) {
			setNewbie(true, data.getNewbieTimeLeft());
		} else if (!Conf.NEWBIE_ENABLED.asBool() && this.newbie) {
			// If newbie protection is disabled but player has newbie flag, clear it
			setNewbie(false);
		}

		// Apply world-specific PvP rules (these override database values)
		if (getCombatWorld().isPvPForced() != CombatWorld.WorldOptionState.NONE) {
			this.pvpState = getCombatWorld().isPvPForced() == CombatWorld.WorldOptionState.ON;
		}

		this.loaded = true;
		// Wake up any threads waiting for data to load
		notifyAll();
		Log.debug("Finished loading data for " + this + (nametag != null ? " with " + nametag.getClass().getSimpleName() : ""));
	}

	public PlayerData exportPlayerData() {
		return PlayerData.builder()
				.uuid(getUUID())
				.name(getName())
				.displayName(getPlayer().getDisplayName())
				.pvpEnabled(hasPvPEnabled())
				.toggleTime(getToggleTime())
				.newbie(isNewbie())
				.newbieTimeLeft(getNewbieTimeLeft())
				.lastSeen(System.currentTimeMillis())
				.build(false);
	}

	public final void cleanForRemoval() {
		if (newbieTask != null) {
			newbieTask.cancel();
		}
		if (nametag != null) {
			nametag.cleanup();
		}
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

	/**
	 * @param player the player instance
	 * @return CombatPlayer instance for the provided player, or null if not found
	 */
	@Nullable
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
