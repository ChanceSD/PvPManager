package me.chancesd.pvpmanager.listener;

import me.chancesd.sdutils.utils.Log;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import me.chancesd.pvpmanager.manager.DeathHandler;
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

	private final PlayerManager playerManager;
	private final DeathHandler deathHandler;
	private final Cache<UUID, String> msgCooldown = CacheBuilder.newBuilder().weakValues().expireAfterWrite(800, TimeUnit.MILLISECONDS).build();

	public PlayerListener(final PlayerManager pM) {
		this.playerManager = pM;
		this.deathHandler = new DeathHandler(pM);
	}

	@EventHandler(ignoreCancelled = true)
	public final void onPlayerEat(final PlayerItemConsumeEvent event) {
		final Material type = event.getItem().getType();
		final CombatPlayer player = playerManager.get(event.getPlayer());
		if (Conf.BLOCK_EAT.asBool() && player.isInCombat() && type.isEdible()) {
			event.setCancelled(true);
			playerManager.get(event.getPlayer()).sendActionBar(Lang.EAT_BLOCKED_IN_COMBAT.msg(), 1000);
		}
		if (Conf.ITEM_COOLDOWNS.asMap().containsKey(type)) {
			if (player.hasItemCooldown(type)) {
				event.setCancelled(true);
				player.message(Lang.ITEM_COOLDOWN.msgTimeUntil(player.getItemCooldown(type)));
				return;
			}
			player.setItemCooldown(type, Conf.ITEM_COOLDOWNS.asMap().get(type));
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public final void onPlayerKick(final PlayerKickEvent event) {
		final Player player = event.getPlayer();
		Log.debugLazy(() -> player.getName() + " was kicked with reason: " + event.getReason() + " | Leave message: " + event.getLeaveMessage()
				+ " - In combat: " + playerManager.get(player).isInCombat());
		if (Conf.PUNISH_ON_KICK.asBool() && (!Conf.MATCH_KICK_REASON.asBool() || Conf.PUNISH_KICK_REASONS.asList().stream()
				.anyMatch(reason -> event.getReason().toLowerCase().contains(reason.toLowerCase()))))
			return;

		final CombatPlayer pvPlayer = playerManager.get(player);
		if (pvPlayer.isInCombat()) {
			pvPlayer.untag(UntagReason.KICKED);
		}
	}

	@EventHandler // normal priority to avoid conflict with griefprevention
	public final void onPlayerLogout(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		final CombatPlayer combatPlayer = playerManager.get(player);
		Log.debug(player.getName() + " quit with message: " + event.getQuitMessage() + " - In combat: " + combatPlayer.isInCombat());
		if (combatPlayer.isInCombat() && !combatPlayer.hasPerm(Permissions.EXEMPT_COMBAT_LOG)) {
			playerManager.getConfigManager().getLog().logCombatLog(combatPlayer);
			CombatUtils.executeCommands(Conf.COMMANDS_ON_COMBATLOG.asList(), player, player.getName());
			playerManager.applyPunishments(combatPlayer);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public final void onPlayerLogoutMonitor(final PlayerQuitEvent event) {
		// Paper still calls some events after PlayerQuitEvent, so delay removal to next tick
		ScheduleUtils.runPlatformTask(() -> playerManager.removePlayer(playerManager.get(event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public final void onPlayerDeath(final PlayerDeathEvent event) {
		if (CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName()))
			return;
		deathHandler.processDeath(event);
	}

	@EventHandler
	public final void onPlayerCooldown(final PlayerInteractEvent e) {
		final Player player = e.getPlayer();
		if (CombatUtils.isWorldExcluded(player.getWorld().getName()))
			return;

		final Material type = e.getMaterial();
		if ((e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) && Conf.ITEM_COOLDOWNS.asMap().containsKey(type)) {
			final CombatPlayer pvplayer = playerManager.get(player);
			if (pvplayer.hasItemCooldown(type)) {
				final String msg = Lang.ITEM_COOLDOWN.msgTimeUntil(pvplayer.getItemCooldown(type));
				if (!msg.equals(msgCooldown.getIfPresent(player.getUniqueId()))) {
					pvplayer.message(msg);
					msgCooldown.put(player.getUniqueId(), msg);
				}
				e.setCancelled(true);
			} else if (!type.isEdible()) {
				ScheduleUtils.runPlatformTask(() -> pvplayer.setItemCooldown(type, Conf.ITEM_COOLDOWNS.asMap().get(type)));
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

		final CombatPlayer pvplayer = playerManager.get(player);
		final Material type = e.getMaterial();
		if (type == Material.FLINT_AND_STEEL) {
			for (final Player p : clickedBlock.getWorld().getPlayers()) {
				if (player.equals(p) || !clickedBlock.getWorld().equals(p.getWorld()) || !player.canSee(p)) {
					continue;
				}
				final CombatPlayer target = playerManager.get(p);
				if ((!target.hasPvPEnabled() || !pvplayer.hasPvPEnabled()) && clickedBlock.getLocation().distanceSquared(p.getLocation()) < 9) {
					pvplayer.message(Lang.ATTACK_DENIED_OTHER.msg(target.getName()));
					e.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public final void onBucketEmpty(final PlayerBucketEmptyEvent event) {
		final Player player = event.getPlayer();
		final CombatPlayer combatPlayer = playerManager.get(player);
		final Block clickedBlock = event.getBlockClicked();
		if (event.getBucket() == Material.LAVA_BUCKET) {
			for (final Player p : clickedBlock.getWorld().getPlayers()) {
				if (player.equals(p) || !clickedBlock.getWorld().equals(p.getWorld()) || !player.canSee(p)) {
					continue;
				}
				final CombatPlayer target = playerManager.get(p);
				final Location playerLocation = p.getLocation();
				if ((!target.hasPvPEnabled() || !combatPlayer.hasPvPEnabled())
						&& playerLocation != null && clickedBlock.getLocation().distanceSquared(playerLocation) < 25) {
					combatPlayer.message(Lang.ATTACK_DENIED_OTHER.msg(target.getName()));
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST) // Create player as early as possible
	public final void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final CombatPlayer pvPlayer = playerManager.createPlayer(player, true);
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
			final ProtectionResult result = playerManager.checkProtection(player, caught);
			if (result.isProtected()) {
				event.setCancelled(true);
				event.getHook().setHookedEntity(null);
				Lang.messageProtection(result, player, caught);
			} else if (!Conf.IGNORE_NO_DMG_HITS.asBool()) {
				playerManager.getPlugin().getEntityListener().processDamage(player, caught);
			}
		}
	}

	@EventHandler
	public final void onPlayerRespawn(final PlayerRespawnEvent event) {
		final Player player = event.getPlayer();
		if (CombatUtils.isWorldExcluded(player.getWorld().getName()))
			return;
		final CombatPlayer combatPlayer = playerManager.getUnchecked(player);
		if (combatPlayer == null)
			return;
		if (Conf.KILL_ABUSE_ENABLED.asBool() && Conf.RESPAWN_PROTECTION.asInt() != 0) {
			combatPlayer.setRespawnTime(System.currentTimeMillis());
		}
		if (combatPlayer.wasLastDeathPvP()) {
			CombatUtils.executeCommands(Conf.COMMANDS_ON_RESPAWN.asList(), player, player.getName());
		}
	}

	@EventHandler
	public void onChangeWorld(final PlayerChangedWorldEvent event) {
		final Player player = event.getPlayer();
		final CombatPlayer pvPlayer = playerManager.get(player);
		final CombatWorld combatWorld = playerManager.getPlugin().getWorldManager().getWorld(player.getWorld());
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
