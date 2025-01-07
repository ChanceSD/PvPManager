package me.chancesd.pvpmanager.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.MCVersion;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.event.PlayerCombatLogEvent;
import me.chancesd.pvpmanager.player.CombatPlayer;
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

	private final HashMap<UUID, CombatPlayer> players = new HashMap<>();
	private final ConfigManager configManager;
	private final DependencyManager dependencyManager;
	@NotNull
	private final PvPManager plugin;
	private final TagTask tagTask;
	private boolean globalStatus = true;

	public PlayerManager(@NotNull final PvPManager plugin) {
		this.plugin = plugin;
		this.configManager = plugin.getConfigM();
		this.dependencyManager = plugin.getDependencyManager();
		this.tagTask = new TagTask(plugin.getDisplayManager());
		if (Conf.KILL_ABUSE_ENABLED.asBool()) {
			ScheduleUtils.runAsyncTimer(new CleanKillersTask(this), Conf.KILL_ABUSE_TIME.asInt(), Conf.KILL_ABUSE_TIME.asInt(), TimeUnit.SECONDS);
		}
		if (Conf.PVP_DISABLED_FEE.asInt() != 0) {
			ScheduleUtils.runAsyncTimer(new PvPToggleFeeTask(this), 0, 1, TimeUnit.HOURS);
		}

		addOnlinePlayers();
	}

	public final ProtectionResult checkProtection(@NotNull final Player damager, @NotNull final Player defender) {
		final CombatPlayer attacker = get(damager);
		final CombatPlayer attacked = get(defender);

		if (attacker.hasOverride() || Conf.VULNERABLE_ENABLED.asBool() && canAttackHooks(attacker, attacked))
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

	private boolean canAttackHooks(final CombatPlayer attacker, final CombatPlayer defender) {
		if (attacker.isInCombat() && defender.isInCombat())
			return defender.isEnemyOf(attacker);
		return false;
	}

	/**
	 * @param player the player instance
	 * @return PvPlayer instance for the provided player
	 */
	@NotNull
	public final CombatPlayer get(final Player player) {
		final CombatPlayer pvPlayer = players.get(player.getUniqueId());
		if (pvPlayer != null)
			return pvPlayer;
		return addUser(new CombatPlayer(player, plugin, tagTask));
	}

	@NotNull
	private CombatPlayer addUser(final CombatPlayer p) {
		final boolean save = CombatUtils.isReal(p.getUUID());
		// Save only if player actually exists
		if (save) {
			players.put(p.getUUID(), p);
		}
		Log.debug("Creating " + p + " Saved: " + save);
		return p;
	}

	public final void removeUser(final CombatPlayer player) {
		if (player.isInCombat()) {
			player.untag(UntagReason.LOGOUT);
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
				p.damage(Float.MAX_VALUE, DamageSource.builder(DamageType.GENERIC_KILL).build());
			} else {
				p.setHealth(0);
			}
			player.setPvpLogged(false);
		}
		if (Conf.FINE_AMOUNT.asDouble() != 0) {
			player.applyFine();
		}
	}

	public void handleCombatLogDrops(final PlayerDeathEvent event, final Player player) {
		if (!Conf.DROP_EXP.asBool()) {
			keepExp(event);
		}
		if (!Conf.DROP_INVENTORY.asBool() && Conf.DROP_ARMOR.asBool()) {
			CombatUtils.fakeItemStackDrop(player, player.getInventory().getArmorContents());
			player.getInventory().setArmorContents(null);
		} else if (Conf.DROP_INVENTORY.asBool() && !Conf.DROP_ARMOR.asBool()) {
			CombatUtils.fakeItemStackDrop(player, player.getInventory().getContents());
			player.getInventory().clear();
		}
		if (!Conf.DROP_INVENTORY.asBool() || !Conf.DROP_ARMOR.asBool()) {
			keepInv(event);
		}
	}

	public void handlePlayerDrops(final PlayerDeathEvent event, final Player player, final Player killer) {
		if (player.equals(killer))
			return;
		switch (Conf.PLAYER_DROP_MODE.asEnum(Conf.DropMode.class)) {
		case DROP:
			if (killer == null) {
				keepInv(event);
				keepExp(event);
			}
			break;
		case KEEP:
			if (killer != null) {
				keepInv(event);
				keepExp(event);
			}
			break;
		case TRANSFER:
			if (killer != null) {
				final ItemStack[] drops = event.getDrops().toArray(new ItemStack[event.getDrops().size()]);
				final HashMap<Integer, ItemStack> returned = killer.getInventory().addItem(drops);
				CombatUtils.fakeItemStackDrop(player, returned.values().toArray(new ItemStack[returned.values().size()]));
				event.getDrops().clear();
			}
			break;
		case CLEAR:
			event.getDrops().clear();
			event.setDroppedExp(0);
			break;
		default:
			break;
		}
	}

	private void keepInv(final PlayerDeathEvent event) {
		event.setKeepInventory(true);
		event.getDrops().clear();
	}

	private void keepExp(final PlayerDeathEvent event) {
		event.setKeepLevel(true);
		event.setDroppedExp(0);
	}

	private void addOnlinePlayers() {
		CombatPlayer.startExecutor();
		final ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
		if (scoreboardManager != null) {
			scoreboardManager.getMainScoreboard().getTeams().forEach(team -> {
				if (team.getName().startsWith("PVP-") && team.getName().length() == 16) {
					Log.debug("Unregistered leftover team: " + team.getName() + " Entries: " + team.getEntries());
					team.unregister();
				}
			});
		}
		for (final Player p : plugin.getServer().getOnlinePlayers()) {
			get(p);
		}
	}

	public void handlePluginDisable() {
		tagTask.cancel();
		for (final CombatPlayer p : new HashSet<>(players.values())) {
			removeUser(p);
		}
		removeTeams();
		Log.infoColor(ChatColor.RED + "Saving player data to storage...");
		CombatPlayer.shutdownExecutorAndWait();
	}

	private void removeTeams() {
		final ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
		if (scoreboardManager == null)
			return;
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
