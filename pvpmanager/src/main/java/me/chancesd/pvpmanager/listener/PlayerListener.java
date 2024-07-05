package me.chancesd.pvpmanager.listener;

import me.chancesd.sdutils.utils.Log;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import java.util.Arrays;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.WorldGuardDependency;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.ProtectionResult;
import me.chancesd.pvpmanager.player.UntagReason;
import me.chancesd.pvpmanager.player.world.CombatWorld;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.sdutils.scheduler.ScheduleUtils;

public class PlayerListener implements Listener {

	private final PlayerManager playerHandler;
	private final WorldGuardDependency wg;
	private final Cache<UUID, String> msgCooldown = CacheBuilder.newBuilder().weakValues().expireAfterWrite(800, TimeUnit.MILLISECONDS).build();

	public PlayerListener(final PlayerManager ph) {
		this.playerHandler = ph;
		this.wg = (WorldGuardDependency) ph.getPlugin().getDependencyManager().getDependency(Hook.WORLDGUARD);
	}

	@EventHandler(ignoreCancelled = true)
	public final void onPlayerEat(final PlayerItemConsumeEvent event) {
		final Material type = event.getItem().getType();
		final CombatPlayer player = playerHandler.get(event.getPlayer());
		if (Conf.BLOCK_EAT.asBool() && player.isInCombat() && type.isEdible()) {
			event.setCancelled(true);
			playerHandler.get(event.getPlayer()).sendActionBar(Lang.EAT_BLOCKED_IN_COMBAT.msg(), 1000);
		}
		if (Conf.ITEM_COOLDOWNS.asMap().containsKey(type)) {
			if (player.hasItemCooldown(type)) {
				event.setCancelled(true);
				player.message(Lang.ITEM_COOLDOWN.msgTime(player.getItemCooldown(type)));
				return;
			}
			player.setItemCooldown(type, Conf.ITEM_COOLDOWNS.asMap().get(type));
		}
	}

	@EventHandler
	public final void onFireworkUseWhileGliding(final PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		if (MCVersion.isLowerThan(MCVersion.V1_9) || !player.isGliding())
			return;

		final Action action = event.getAction();
		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK)
			return;

		final PvPlayer pvPlayer = ph.get(player);
		if (!pvPlayer.isInCombat())
			return;

		final ItemStack item = player.getInventory().getItemInHand();
		Material fireworkMaterial = null;
		if (CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.13")) {
			fireworkMaterial = Material.FIREWORK_ROCKET;
		} else {
			// For versions before 1.13, try to get the legacy material
			try {
				fireworkMaterial = Material.getMaterial("FIREWORK");
			} catch (final Exception e) {
				return;
			}
		}

		if (item.getType() != fireworkMaterial)
			return;

		// Check if fireworks are completely blocked
		if (Settings.isBlockFireworks()) {
			event.setCancelled(true);
			pvPlayer.sendActionBar(Messages.getFireworkBlockedInCombat(), 1000);
			return;
		}

		// Check power limit
		final int powerLimit = Settings.getFireworkPowerLimit();
		if (powerLimit >= 0 && item.hasItemMeta()) {
			final FireworkMeta meta = (FireworkMeta) item.getItemMeta();
			if (meta != null && meta.getPower() > powerLimit) {
				event.setCancelled(true);
				final String message = Messages.getFireworkPowerLimitedInCombat().replace("%power", String.valueOf(meta.getPower()));
				pvPlayer.sendActionBar(message, 1000);
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public final void onPlayerKick(final PlayerKickEvent event) {
		final Player player = event.getPlayer();
		Log.debugLazy(() -> player.getName() + " was kicked with reason: " + event.getReason() + " | Leave message: " + event.getLeaveMessage()
				+ " - In combat: " + playerHandler.get(player).isInCombat());
		if (Conf.PUNISH_ON_KICK.asBool() && (!Conf.MATCH_KICK_REASON.asBool() || Conf.PUNISH_KICK_REASONS.asList().stream()
				.anyMatch(reason -> event.getReason().toLowerCase().contains(reason.toLowerCase()))))
			return;

		final CombatPlayer pvPlayer = playerHandler.get(player);
		if (pvPlayer.isInCombat()) {
			pvPlayer.untag(UntagReason.KICKED);
		}
	}

	@EventHandler // normal priority to avoid conflict with griefprevention
	public final void onPlayerLogout(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		final CombatPlayer pvPlayer = playerHandler.get(player);
		Log.debug(player.getName() + " quit with message: " + event.getQuitMessage() + " - In combat: " + pvPlayer.isInCombat());
		if (pvPlayer.isInCombat() && !pvPlayer.hasPerm(Permissions.EXEMPT_COMBAT_LOG)) {
			playerHandler.getConfigManager().getLog().logCombatLog(pvPlayer);
			CombatUtils.executeCommands(Conf.COMMANDS_ON_COMBATLOG.asList(), player, player.getName());
			playerHandler.applyPunishments(pvPlayer);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public final void onPlayerLogoutMonitor(final PlayerQuitEvent event) {
		playerHandler.removeUser(playerHandler.get(event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public final void onPlayerDeath(final PlayerDeathEvent event) {
		final Player player = event.getEntity();
		if (CombatUtils.isWorldExcluded(player.getWorld().getName()))
			return;

		final CombatPlayer pvPlayer = playerHandler.get(player);
		final Player killer = player.getKiller();

		// Player died in combat, process that
		if (killer != null && !killer.equals(player)) {
			final CombatPlayer pKiller = playerHandler.get(killer);
			handlePvPDeath(player, pvPlayer, killer, pKiller, event);
		}

		if (pvPlayer.isInCombat()) {
			final Set<CombatPlayer> enemies = pvPlayer.getEnemies();
			if (Conf.UNTAG_ON_KILL.asBool()) {
				enemies.forEach(enemy -> enemy.removeEnemy(pvPlayer));
			}
			pvPlayer.untag(UntagReason.DEATH);
		}

		// Let's process player's inventory/exp according to config file
		if (pvPlayer.hasPvPLogged()) {
			playerHandler.handleCombatLogDrops(event, player);
			return;
		}

		playerHandler.handlePlayerDrops(event, player, killer);
	}

	private void handlePvPDeath(final Player player, final CombatPlayer pvPlayer, final Player killer, final CombatPlayer pKiller, final PlayerDeathEvent event) {
		if (Conf.KILL_ABUSE_ENABLED.asBool() && !pKiller.hasPerm(Permissions.EXEMPT_KILL_ABUSE)) {
			pKiller.addVictim(player);
		}
		if (wg == null || !wg.containsRegionsAt(killer.getLocation(), Conf.KILLS_WG_EXCLUSIONS.asSet())) {
			if (Conf.MONEY_REWARD.asDouble() > 0) {
				pKiller.giveReward(pvPlayer);
			}
			if (Conf.MONEY_PENALTY.asDouble() > 0) {
				pvPlayer.applyPenalty();
			}
			if (pKiller.canExecuteKillCommand()) {
				CombatUtils.executeCommands(Conf.COMMANDS_ON_KILL.asList(), killer, killer.getName(), player.getName());
			}
			pvPlayer.setLastDeathWasPvP(true);
			if (Conf.EXP_STEAL.asDouble() > 0) {
				final int expWon = pKiller.giveExp(pvPlayer);
				event.setDroppedExp(0);
				event.setNewExp(player.getTotalExperience() - expWon);
				pvPlayer.message(Lang.EXP_STOLEN.msg(pKiller.getName(), String.valueOf(expWon)));
			}
		}
	}

	@EventHandler
	public final void onPlayerCooldown(final PlayerInteractEvent e) {
		final Player player = e.getPlayer();
		if (CombatUtils.isWorldExcluded(player.getWorld().getName()))
			return;

		final Material type = e.getMaterial();
		if ((e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) && Conf.ITEM_COOLDOWNS.asMap().containsKey(type)) {
			final CombatPlayer pvplayer = playerHandler.get(player);
			if (pvplayer.hasItemCooldown(type)) {
				final String msg = Lang.ITEM_COOLDOWN.msgTime(pvplayer.getItemCooldown(type));
				if (!msg.equals(msgCooldown.getIfPresent(player.getUniqueId()))) {
					pvplayer.message(msg);
					msgCooldown.put(player.getUniqueId(), msg);
				}
				e.setCancelled(true);
			} else if (!type.isEdible()) {
				Bukkit.getScheduler().runTask(playerHandler.getPlugin(), () -> pvplayer.setItemCooldown(type, Conf.ITEM_COOLDOWNS.asMap().get(type)));
			}
		}
	}

	@SuppressWarnings("null") // p.getLocation() is not null
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true) // cancel on low since some plugins check cancels on normal instead of monitor
	public final void onPlayerInteract(final PlayerInteractEvent e) {
		final Player player = e.getPlayer();
		if (CombatUtils.isWorldExcluded(player.getWorld().getName()) || e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		final Block clickedBlock = e.getClickedBlock();
		if (clickedBlock == null)
			return;

		final CombatPlayer pvplayer = playerHandler.get(player);
		final Material type = e.getMaterial();
		if (type == Material.FLINT_AND_STEEL) {
			for (final Player p : clickedBlock.getWorld().getPlayers()) {
				if (player.equals(p) || !clickedBlock.getWorld().equals(p.getWorld()) || !player.canSee(p)) {
					continue;
				}
				final CombatPlayer target = playerHandler.get(p);
				if ((!target.hasPvPEnabled() || !pvplayer.hasPvPEnabled()) && clickedBlock.getLocation().distanceSquared(p.getLocation()) < 9) {
					pvplayer.message(Lang.ATTACK_DENIED_OTHER.msg(target.getName()));
					e.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true) // cancel on low since some plugins check cancels on normal instead of monitor
	public final void onPlayerBlockInteract(final PlayerInteractEvent e) {
		final Player player = e.getPlayer();
		if (!Conf.BLOCK_INTERACT_IN_COMBAT.asBool()  || e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.PHYSICAL
				|| CombatUtils.isWorldExcluded(player.getWorld().getName()))
			return;

		final CombatPlayer combatPlayer = playerHandler.get(player);
		final Block clickedBlock = e.getClickedBlock();
		if (clickedBlock == null)
			return;
		if (combatPlayer.isInCombat()) {
			final Material blockType = clickedBlock.getType();
			for (final String material : Conf.BLOCK_INTERACT_ITEM_LIST.asList()) {
				if (blockType.name().endsWith(material)) {
					e.setCancelled(true);
					combatPlayer.sendActionBar(Lang.INTERACT_BLOCKED_IN_COMBAT.msg(), 1000);
					return;
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public final void onBucketEmpty(final PlayerBucketEmptyEvent event) {
		final Player player = event.getPlayer();
		final PvPlayer combatPlayer = ph.get(player);
		final Block clickedBlock = event.getBlockClicked();
		if (event.getBucket() == Material.LAVA_BUCKET) {
			for (final Player p : clickedBlock.getWorld().getPlayers()) {
				if (player.equals(p) || !clickedBlock.getWorld().equals(p.getWorld()) || !player.canSee(p)) {
					continue;
				}
				final PvPlayer target = ph.get(p);
				if ((!target.hasPvPEnabled() || !combatPlayer.hasPvPEnabled()) && clickedBlock.getLocation().distanceSquared(p.getLocation()) < 25) {
					combatPlayer.message(Messages.pvpDisabledOther(target.getName()));
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public final void onPlayerPickup(final PlayerPickupItemEvent e) {
		if (Settings.isNewbieProtectionEnabled() && Settings.isBlockPickNewbies()) {
			final CombatPlayer player = playerHandler.get(e.getPlayer());
			if (player.isNewbie()) {
				e.setCancelled(true);
				player.sendActionBar(Lang.NEWBIE_PICKUP_ITEM_BLOCKED.msg(), 1000);
			}
		}
	}

	@EventHandler
	public final void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final CombatPlayer pvPlayer = playerHandler.get(player);
		ScheduleUtils.runAsync(() -> {
			if (player.isOp() || pvPlayer.hasPerm(Permissions.ADMIN)) {
				Lang.sendQueuedMsgs(pvPlayer);
			}
		});
	}

	@EventHandler(ignoreCancelled = true)
	public final void onPlayerFish(final PlayerFishEvent event) {
		final Player player = event.getPlayer();
		if (CombatUtils.isWorldExcluded(player.getWorld().getName()))
			return;

		if (event.getState() == State.CAUGHT_ENTITY && event.getCaught() instanceof final Player caught) {
			final ProtectionResult result = playerHandler.checkProtection(player, caught);
			if (result.isProtected()) {
				event.setCancelled(true);
				event.getHook().setHookedEntity(null);
				Lang.messageProtection(result, player, caught);
			} else {
				playerHandler.getPlugin().getEntityListener().processDamage(player, caught);
			}
		}
	}

	@EventHandler
	public final void onPlayerRespawn(final PlayerRespawnEvent event) {
		if (CombatUtils.isWorldExcluded(event.getPlayer().getWorld().getName()))
			return;
		final CombatPlayer combatPlayer = ph.get(event.getPlayer());
		if (Conf.KILL_ABUSE_ENABLED.asBool() && Conf.RESPAWN_PROTECTION.asInt() != 0) {
			combatPlayer.setRespawnTime(System.currentTimeMillis());
		}
		if (combatPlayer.wasLastDeathPvP()) {
			CombatUtils.executeCommands(Settings.getCommandsOnRespawn(), event.getPlayer(), event.getPlayer().getName());
			combatPlayer.setLastDeathWasPvP(false);
		}
	}

	@EventHandler
	public void onChangeWorld(final PlayerChangedWorldEvent event) {
		final Player player = event.getPlayer();
		final CombatPlayer pvPlayer = playerHandler.get(player);
		final CombatWorld combatWorld = playerHandler.getPlugin().getWorldManager().getWorld(player.getWorld());
		pvPlayer.setCombatWorld(combatWorld);

		// Handle newbie protection pause in excluded worlds
		if (pvPlayer.isNewbie() && pvPlayer.getNewbieTask() != null) {
			if (CombatUtils.isWorldExcluded(player.getWorld().getName())) {
				pvPlayer.getNewbieTask().pause();
			} else {
				pvPlayer.getNewbieTask().resume();
			}
		}

		final CombatWorld.WorldOptionState optionState = pvPlayer.getCombatWorld().isPvPForced();
		if (optionState == CombatWorld.WorldOptionState.NONE)
			return;
		if (pvPlayer.hasPvPEnabled() && optionState == CombatWorld.WorldOptionState.OFF) {
			pvPlayer.setPvP(false);
			pvPlayer.message(Lang.ERROR_PVP_TOGGLE_NO_PVP);
			return;
		}
		if (!pvPlayer.hasPvPEnabled() && optionState == CombatWorld.WorldOptionState.ON) {
			pvPlayer.setPvP(true);
			pvPlayer.message(Lang.ERROR_PVP_TOGGLE_FORCE_PVP);
		}
	}

}
