package me.NoChance.PvPManager.Managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.WorldGuardHook;
import me.NoChance.PvPManager.Events.PlayerCombatLogEvent;
import me.NoChance.PvPManager.Player.CancelResult;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Tasks.CleanKillersTask;
import me.NoChance.PvPManager.Tasks.TagTask;
import me.NoChance.PvPManager.Utils.CombatUtils;

public class PlayerHandler {

	private final HashMap<UUID, PvPlayer> players = new HashMap<>();
	private static final HashSet<UUID> newbiesDisabled = new HashSet<>();
	private final ConfigManager configManager;
	private final DependencyManager dependencyManager;
	private final PvPManager plugin;
	private final TagTask tagTask;
	private final WorldGuardHook worldguard;

	public PlayerHandler(final PvPManager plugin) {
		this.plugin = plugin;
		this.configManager = plugin.getConfigM();
		this.dependencyManager = plugin.getDependencyManager();
		this.tagTask = new TagTask(plugin.getDisplayManager());
		this.worldguard = (WorldGuardHook) dependencyManager.getDependency(Hook.WORLDGUARD);
		if (Settings.isKillAbuseEnabled()) {
			new CleanKillersTask(this).runTaskTimer(plugin, 0, Settings.getKillAbuseTime() * 20);
		}

		addOnlinePlayers();
	}

	public final CancelResult tryCancel(final Player damager, final Player defender) {
		final PvPlayer attacker = get(damager);
		final PvPlayer attacked = get(defender);
		if (attacker.hasOverride() || Settings.borderHoppingVulnerable() && canAttackHooks(attacker, attacked))
			return CancelResult.FAIL_OVERRIDE;
		if (attacked.hasRespawnProtection() || attacker.hasRespawnProtection())
			return CancelResult.RESPAWN_PROTECTION.setAttackerCaused(attacker.hasRespawnProtection());
		if (attacked.isNewbie() || attacker.isNewbie())
			return CancelResult.NEWBIE.setAttackerCaused(attacker.isNewbie());
		if (!attacker.hasPvPEnabled() || !attacked.hasPvPEnabled()) {
			if (Settings.isWorldguardOverrides() && worldguard != null && worldguard.hasAllowPvPFlag(defender)) {
				attacker.setPvP(true);
				attacker.message(Messages.getPvpForceEnabledWG());
				attacked.setPvP(true);
				attacked.message(Messages.getPvpForceEnabledWG()));
			} else if (dependencyManager.shouldDisableProtection(damager, defender))
				return CancelResult.FAIL;
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
		for (final Team team : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
			if (team.getName().startsWith("PVP-") && team.getName().length() == 16) {
				team.unregister();
			}
		}
		for (final Player p : plugin.getServer().getOnlinePlayers()) {
			get(p);
		}
		newbiesDisabled.clear();
	}

	/**
	 * @param player
	 * @return PvPlayer instance for the provided player
	 */
	public final PvPlayer get(final Player player) {
		final PvPlayer pvPlayer = players.get(player.getUniqueId());
		return pvPlayer != null ? pvPlayer : addUser(new PvPlayer(player, plugin));
	}

	private PvPlayer addUser(final PvPlayer p) {
		// Save only if player actually exists
		if (Bukkit.getPlayer(p.getUUID()) != null) {
			players.put(p.getUUID(), p);
		}
		return p;
	}

	public final void removeUser(final PvPlayer player) {
		player.cleanForRemoval();
		players.remove(player.getUUID());
		if (player.hasPvPLogged()) {
			player.setPvpLogged(false);
			untag(player);
		}
		configManager.markForSave(player);
	}

	public final void applyPunishments(final PvPlayer player) {
		final Player p = player.getPlayer();
		final PlayerCombatLogEvent event = new PlayerCombatLogEvent(p, player);
		Bukkit.getPluginManager().callEvent(event);
		if (Settings.isKillOnLogout()) {
			player.setPvpLogged(true);
			p.setHealth(0);
		}
		if (Settings.getFineAmount() != 0) {
			player.applyFine();
		}
	}

	public void handleCombatLogDrops(final PlayerDeathEvent event, final Player player) {
		if (!Settings.isDropExp()) {
			keepExp(event);
		}
		if (!Settings.isDropInventory() && Settings.isDropArmor()) {
			CombatUtils.fakeItemStackDrop(player, player.getInventory().getArmorContents());
			player.getInventory().setArmorContents(null);
		} else if (Settings.isDropInventory() && !Settings.isDropArmor()) {
			CombatUtils.fakeItemStackDrop(player, player.getInventory().getContents());
			player.getInventory().clear();
		}
		if (!Settings.isDropInventory() || !Settings.isDropArmor()) {
			keepInv(event);
		}
	}

	public void handlePlayerDrops(final PlayerDeathEvent event, final Player player, final Player killer) {
		switch (Settings.getDropMode()) {
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

	public void handlePluginDisable() {
		tagTask.cancel();
		for (final PvPlayer p : players.values()) {
			final Player player = p.getPlayer();
			if (player != null && !player.hasPlayedBefore() && !p.isNewbie()) {
				newbiesDisabled.add(p.getUUID());
			}
			configManager.markForSave(p);
			p.cleanForRemoval();
		}
		removeTeams();
		configManager.awaitSave();
	}

	private final void removeTeams() {
		final Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		final Team pvpon = scoreboard.getTeam("PvPOn");
		if (pvpon != null) {
			pvpon.unregister();
		}
		final Team pvpoff = scoreboard.getTeam("PvPOff");
		if (pvpoff != null) {
			pvpoff.unregister();
		}
	}

	public final void untag(final PvPlayer p) {
		tagTask.untag(p);
	}

	public final void tag(final PvPlayer p) {
		tagTask.addTagged(p);
	}

	public final Map<UUID, PvPlayer> getPlayers() {
		return players;
	}

	public final Set<PvPlayer> getPlayersInCombat() {
		return tagTask.getTaggedPlayers();
	}

	public static boolean isRemovedNewbie(final PvPlayer p) {
		return newbiesDisabled.contains(p.getUUID());
	}

	public final PvPManager getPlugin() {
		return plugin;
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

}
