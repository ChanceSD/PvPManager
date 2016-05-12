package me.NoChance.PvPManager.Managers;

import java.util.HashMap;
import java.util.Timer;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Dependencies.Hooks.WorldGuard;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Tasks.CleanKillersTask;
import me.NoChance.PvPManager.Tasks.TagTask;
import me.NoChance.PvPManager.Utils.CancelResult;

public class PlayerHandler {

	private final HashMap<UUID, PvPlayer> players = new HashMap<>();
	private final ConfigManager configManager;
	private final DependencyManager dependencyManager;
	private final PvPManager plugin;
	private final TagTask tagTask = new TagTask();
	private final WorldGuard worldguard;

	public PlayerHandler(final PvPManager plugin) {
		this.plugin = plugin;
		this.configManager = plugin.getConfigM();
		this.dependencyManager = plugin.getDependencyManager();
		worldguard = (WorldGuard) dependencyManager.getDependency("WorldGuard");
		if (Settings.isKillAbuseEnabled()) {
			new CleanKillersTask(this).runTaskTimer(plugin, 0, Settings.getKillAbuseTime() * 20);
		}

		addOnlinePlayers();
		new Timer(true).scheduleAtFixedRate(tagTask, 1000, 1000);
	}

	public final CancelResult tryCancel(final Player damager, final Player defender) {
		final PvPlayer attacker = get(damager);
		final PvPlayer attacked = get(defender);
		if (attacker.hasOverride() || Settings.isStopBorderHopping() && canAttackHooks(attacker, attacked))
			return CancelResult.FAIL_OVERRIDE;
		if (attacked.hasRespawnProtection() || attacker.hasRespawnProtection())
			return CancelResult.RESPAWN_PROTECTION;
		if (attacked.isNewbie() || attacker.isNewbie())
			return CancelResult.NEWBIE.setAttackerCaused(attacker.isNewbie());
		if (!attacker.hasPvPEnabled() || !attacked.hasPvPEnabled()) {
			if (worldguard != null && worldguard.hasAllowPvPFlag(defender.getLocation())) {
				attacker.setPvP(true);
				attacked.setPvP(true);
			}
			return CancelResult.PVPDISABLED.setAttackerCaused(!attacker.hasPvPEnabled());
		}
		return CancelResult.FAIL;

	}

	/**
	 * Use this method to check PvP instead of using {@link PlayerHandler#tryCancel(Player, Player)}
	 * This method will not be changed while the previous might change at any time.
	 *
	 * @param attacker
	 *            The attacking player
	 * @param defender
	 *            The player being attacked
	 *
	 * @return true if the attack didn't get blocked or if it got override, otherwise false
	 */
	public final boolean canAttack(final Player attacker, final Player defender) {
		final CancelResult cr = tryCancel(attacker, defender);
		return cr.equals(CancelResult.FAIL) || cr.equals(CancelResult.FAIL_OVERRIDE);
	}

	private boolean canAttackHooks(final PvPlayer attacker, final PvPlayer defender) {
		if (attacker.isInCombat() && defender.isInCombat())
			return dependencyManager.canAttack(attacker.getPlayer(), defender.getPlayer());
		return false;
	}

	private void addOnlinePlayers() {
		for (final Player p : plugin.getServer().getOnlinePlayers()) {
			get(p);
		}
	}

	/**
	 * @param player
	 * @return PvPlayer instance for the provided player
	 */
	public final PvPlayer get(final Player player) {
		final UUID uuid = player.getUniqueId();
		return players.containsKey(uuid) ? players.get(uuid) : addUser(new PvPlayer(player, plugin));
	}

	private PvPlayer addUser(final PvPlayer p) {
		// Save only if player actually exists
		if (Bukkit.getPlayer(p.getUUID()) != null) {
			players.put(p.getUUID(), p);
			if (configManager.getUserStorage().contains(p.getUUID().toString())) {
				p.loadUserData(configManager.getUserData(p.getUUID()));
			}
			p.loadState();
		}
		return p;
	}

	public final void removeUser(final PvPlayer player) {
		players.remove(player.getUUID());
		player.removeCombatTeam();
		if (player.hasPvPLogged()) {
			player.setPvpLogged(false);
			untag(player);
		}
		configManager.saveUser(player);
	}

	public final void applyPunishments(final PvPlayer player) {
		final Player p = player.getPlayer();
		if (Settings.isKillOnLogout()) {
			player.setPvpLogged(true);
			p.setHealth(0);
		}
		if (Settings.getFineAmount() != 0) {
			player.applyFine();
		}
	}

	public void handlePluginDisable() {
		tagTask.cancel();
		for (final PvPlayer p : players.values()) {
			configManager.saveUser(p);
			p.removeCombatTeam();
		}
		removeTeams();
	}

	private final void removeTeams() {
		final Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		if (scoreboard.getTeam("PvPOn") != null) {
			scoreboard.getTeam("PvPOn").unregister();
		}

		if (scoreboard.getTeam("PvPOff") != null) {
			scoreboard.getTeam("PvPOff").unregister();
		}
	}

	public final void untag(final PvPlayer p) {
		tagTask.untag(p);
	}

	public final void tag(final PvPlayer p) {
		tagTask.addTagged(p);
	}

	public final HashMap<UUID, PvPlayer> getPlayers() {
		return players;
	}

	public final PvPManager getPlugin() {
		return plugin;
	}

}
