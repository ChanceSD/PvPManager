package me.chancesd.pvpmanager.manager;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.MCVersion;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.event.PlayerCombatLogEvent;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.PlayerData;
import me.chancesd.pvpmanager.player.ProtectionResult;
import me.chancesd.pvpmanager.player.ProtectionType;
import me.chancesd.pvpmanager.player.UntagReason;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.tasks.CleanKillersTask;
import me.chancesd.pvpmanager.tasks.PvPToggleFeeTask;
import me.chancesd.pvpmanager.tasks.TagTask;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.sdutils.scheduler.ScheduleUtils;

public class PlayerManager {

	private final Map<UUID, CombatPlayer> players = new ConcurrentHashMap<>();
	private final ConfigManager configManager;
	private final DependencyManager dependencyManager;
	private final PvPManager plugin;
	private final TagTask tagTask;
	private boolean globalStatus = true;

	public PlayerManager(@NotNull final PvPManager plugin) {
		this.plugin = plugin;
		this.configManager = plugin.getConfigM();
		this.dependencyManager = plugin.getDependencyManager();
		this.tagTask = new TagTask(plugin.getDisplayManager());
		Bukkit.getPluginManager().registerEvents(tagTask, plugin);

		if (Conf.KILL_ABUSE_ENABLED.asBool()) {
			ScheduleUtils.runAsyncTimer(new CleanKillersTask(this), Conf.KILL_ABUSE_TIME.asInt(), Conf.KILL_ABUSE_TIME.asInt(), TimeUnit.SECONDS);
		}
		if (Conf.PVP_DISABLED_FEE.asInt() != 0) {
			ScheduleUtils.runAsyncTimer(new PvPToggleFeeTask(this), 0, 1, TimeUnit.HOURS);
		}

		// Create CombatPlayer instances for online players (in case of reload)
		removeTeams();
		Bukkit.getOnlinePlayers().forEach(player -> createPlayer(player, true));
	}

	public final ProtectionResult checkProtection(@NotNull final Player damager, @NotNull final Player defender) {
		final CombatPlayer attacker = get(damager);
		final CombatPlayer attacked = get(defender);

		if (attacker.hasOverride() || Conf.VULNERABLE_ENABLED.asBool() && canAttackVulnerable(attacker, attacked))
			return new ProtectionResult(ProtectionType.FAIL_OVERRIDE);
		if (!globalStatus)
			return new ProtectionResult(ProtectionType.GLOBAL_PROTECTION);
		if (!attacked.getCombatWorld().isCombatAllowed())
			return new ProtectionResult(ProtectionType.WORLD_PROTECTION);
		if (attacked.hasRespawnProtection() || attacker.hasRespawnProtection())
			return new ProtectionResult(ProtectionType.RESPAWN_PROTECTION, attacker.hasRespawnProtection());
		if (attacked.isNewbie() || attacker.isNewbie()) {
			if (dependencyManager.shouldDisableProtection(damager, defender, ProtectionType.NEWBIE))
				return new ProtectionResult(ProtectionType.FAIL_PLUGIN_HOOK);
			return new ProtectionResult(ProtectionType.NEWBIE, attacker.isNewbie());
		}
		if (!attacker.hasPvPEnabled() || !attacked.hasPvPEnabled()) {
			if (dependencyManager.shouldDisableProtection(damager, defender, ProtectionType.PVPDISABLED))
				return new ProtectionResult(ProtectionType.FAIL_PLUGIN_HOOK);
			return new ProtectionResult(ProtectionType.PVPDISABLED, !attacker.hasPvPEnabled());
		}
		if (dependencyManager.shouldProtectAFK(defender))
			return new ProtectionResult(ProtectionType.AFK_PROTECTION);

		return new ProtectionResult(ProtectionType.FAIL);
	}

	/**
	 * Use this method to check PvP instead of using {@link PlayerManager#checkProtection(Player, Player)}
	 * This method will not be changed while the previous might change at any time.
	 *
	 * @param attacker
	 *                 The attacking player
	 * @param defender
	 *                 The player being attacked
	 *
	 * @return true if the attack didn't get blocked or if it got override, otherwise false
	 */
	public final boolean canAttack(@NotNull final Player attacker, @NotNull final Player defender) {
		return checkProtection(attacker, defender).isVulnerable();
	}

	private final boolean canAttackVulnerable(final CombatPlayer attacker, final CombatPlayer defender) {
		return attacker.isInCombat() && defender.isInCombat()
			&& attacker.isEnemyOf(defender) && defender.isEnemyOf(attacker);
	}

	/**
	 * Gets an existing CombatPlayer instance for the provided player.
	 * Returns null if the player hasn't joined yet or has already left.
	 *
	 * @param player the player instance
	 * @return CombatPlayer instance for the provided player, or null if not found
	 */
	public final CombatPlayer get(final Player player) {
		final CombatPlayer combatPlayer = players.get(player.getUniqueId());
		if (combatPlayer != null) {
			return combatPlayer;
		}

		final boolean save = CombatUtils.isReal(player.getUniqueId()) && !CombatUtils.isNPC(player);
		if (!save) {
			return createPlayer(player, save);
		}

		return null;
	}

	@Nullable
	public final CombatPlayer getUnchecked(final Player player) {
		return players.get(player.getUniqueId());
	}

	/**
	 * Creates a new CombatPlayer instance for the provided player.
	 * Should only be called from join events or server startup.
	 *
	 * @param player the player instance
	 * @return CombatPlayer instance for the provided player
	 */
	@NotNull
	public final CombatPlayer createPlayer(final Player player, final boolean save) {
		Objects.requireNonNull(player, "Player cannot be null");

		final CombatPlayer combatPlayer = new CombatPlayer(player, plugin);
		Log.debug("Created " + combatPlayer + " Saved: " + save);

		// Only save data for real players
		if (save) {
			players.put(combatPlayer.getUUID(), combatPlayer);
			loadPlayerDataAsync(combatPlayer);
		}

		return combatPlayer;
	}

	/**
	 * Loads player data asynchronously and applies it
	 */
	private void loadPlayerDataAsync(final CombatPlayer combatPlayer) {
		ScheduleUtils.runAsync(() -> {
			try {
				final Map<String, Object> userData = plugin.getStorageManager().getStorage().getUserData(combatPlayer);
				final PlayerData data = PlayerData.fromMap(userData);
				if (combatPlayer.getPlayer().isOnline()) {
					combatPlayer.applyPlayerData(data);
					if (userData.isEmpty()) {
						savePlayer(combatPlayer);
					}
				}
			} catch (final Exception e) {
				Log.severe("Failed to load player data for " + combatPlayer.getName(), e);
			}
		});
	}

	/**
	 * Saves player data to storage
	 */
	public void savePlayer(final CombatPlayer player) {
		try {
			plugin.getStorageManager().getStorage().saveUserData(player);
		} catch (final Exception e) {
			Log.severe("Failed to save player data for " + player.getName(), e);
		}
	}

	public final void removePlayer(final CombatPlayer player) {
		removePlayer(player, true);
	}

	private void removePlayer(final CombatPlayer player, final boolean asyncSave) {
		if (player.isInCombat()) {
			player.untag(UntagReason.LOGOUT);
		}

		if (player.isLoaded()) {
			if (asyncSave) {
				ScheduleUtils.runAsync(() -> savePlayer(player));
			} else {
				savePlayer(player);
			}
		}

		player.cleanForRemoval();
		players.remove(player.getUUID());
	}

	public final void applyPunishments(final CombatPlayer player) {
		final Player p = player.getPlayer();
		final PlayerCombatLogEvent event = new PlayerCombatLogEvent(p, player);
		Bukkit.getPluginManager().callEvent(event);
		if (Conf.KILL_ON_LOGOUT.asBool()) {
			player.setPvpLogged(true);
			if (MCVersion.isAtLeast(MCVersion.V1_20_5)) {
				p.damage(Float.MAX_VALUE, DamageSource.builder(DamageType.GENERIC_KILL).build()); // datapack compatibility
			} else {
				p.setHealth(0);
			}
			if (p.getHealth() > 0) {
				Log.warning("Failed to kill player on logout. It's possible the damage to the player was blocked by another plugin.");
				p.setHealth(0); // Retry with setHealth
			}
			player.setPvpLogged(false);
		}
		if (Conf.FINE_AMOUNT.asDouble() != 0) {
			player.applyFine();
		}
	}

	public void handlePluginDisable() {
		tagTask.cancel();
		// Save players synchronously to ensure saving before storage shutdown
		for (final CombatPlayer p : new HashSet<>(players.values())) {
			removePlayer(p, false);
		}
		removeTeams();
		Log.infoColor(ChatColor.RED + "Saving player data to storage...");
	}

	private void removeTeams() {
		final ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
		if (scoreboardManager == null)
			return;

		scoreboardManager.getMainScoreboard().getTeams().forEach(team -> {
			if (team.getName().startsWith("PVP-") && team.getName().length() == 16) {
				Log.debug("Unregistered leftover team: " + team.getName() + " Entries: " + team.getEntries());
				team.unregister();
			}
		});
		final Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
		final Team pvpon = scoreboard.getTeam("PvPOn");
		if (pvpon != null) {
			pvpon.unregister();
		}
		final Team pvpoff = scoreboard.getTeam("PvPOff");
		if (pvpoff != null) {
			pvpoff.unregister();
		}
		final Objective health = scoreboard.getObjective("PvP_Health");
		if (health != null) {
			health.unregister();
		}
	}

	public boolean getGlobalStatus() {
		return globalStatus;
	}

	public void setGlobalStatus(final boolean globalStatus) {
		this.globalStatus = globalStatus;
	}

	public final Map<UUID, CombatPlayer> getPlayers() {
		return players;
	}

	public final Set<CombatPlayer> getPlayersInCombat() {
		return tagTask.getTaggedPlayers();
	}

	@NotNull
	public final PvPManager getPlugin() {
		return plugin;
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

}
