package me.chancesd.pvpmanager.listener;

import me.chancesd.pvpmanager.world.CombatWorld;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.MCVersion;

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
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.WorldGuardDependency;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.ProtectionResult;
import me.chancesd.pvpmanager.setting.Messages;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.Settings;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.pvpmanager.utils.ScheduleUtils;

@SuppressWarnings("deprecation")
public class PlayerListener implements Listener {

	private final PlayerManager playerHandler;
	private final WorldGuardDependency wg;
	private Material mushroomSoup;
	private final Cache<UUID, String> msgCooldown = CacheBuilder.newBuilder().weakValues().expireAfterWrite(800, TimeUnit.MILLISECONDS).build();

	public PlayerListener(final PlayerManager ph) {
		this.playerHandler = ph;
		this.wg = (WorldGuardDependency) ph.getPlugin().getDependencyManager().getDependency(Hook.WORLDGUARD);
		if (MCVersion.isAtLeast(MCVersion.V1_13)) {
			mushroomSoup = Material.MUSHROOM_STEW;
		} else if (MCVersion.isAtLeast(MCVersion.V1_8)) { // avoid loading Material class on unit tests
			mushroomSoup = Material.getMaterial("MUSHROOM_SOUP");
		}
	}

	@EventHandler(ignoreCancelled = true)
	public final void onBlockPlace(final BlockPlaceEvent event) {
		if (!Settings.isBlockPlaceBlocks() && !Settings.isBlockPlaceBlocksNewbie())
			return;

		final CombatPlayer combatPlayer = playerHandler.get(event.getPlayer());
		if (Settings.isBlockPlaceBlocks() && combatPlayer.isInCombat() || Settings.isBlockPlaceBlocksNewbie() && combatPlayer.isNewbie()) {
			event.setCancelled(true);
			combatPlayer.sendActionBar(Messages.getBlockPlaceBlockedInCombat(), 1000);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public final void onBlockBreak(final BlockBreakEvent event) {
		if (!Settings.isBlockBreakBlocks() && !Settings.isBlockBreakBlocksNewbie())
			return;

		final PvPlayer combatPlayer = ph.get(event.getPlayer());
		if (Settings.isBlockBreakBlocks() && combatPlayer.isInCombat() || Settings.isBlockBreakBlocksNewbie() && combatPlayer.isNewbie()) {
			event.setCancelled(true);
			combatPlayer.sendActionBar(Messages.getBlockBreakBlockedInCombat(), 1000);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public final void onToggleFlight(final PlayerToggleFlightEvent event) {
		if (Settings.isDisableFly() && event.isFlying() && playerHandler.get(event.getPlayer()).isInCombat()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public final void onPlayerEat(final PlayerItemConsumeEvent event) {
		final Material type = event.getItem().getType();
		final CombatPlayer player = playerHandler.get(event.getPlayer());
		if (Settings.isBlockEat() && player.isInCombat() && type.isEdible()) {
			event.setCancelled(true);
			playerHandler.get(event.getPlayer()).sendActionBar(Messages.getEatBlockedInCombat(), 1000);
		}
		if (Settings.getItemCooldowns().containsKey(type)) {
			if (player.hasItemCooldown(type)) {
				event.setCancelled(true);
				player.message(Messages.getItemCooldown(player.getItemCooldown(type)));
				return;
			}
			player.setItemCooldown(type, Settings.getItemCooldowns().get(type));
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
		if (Settings.punishOnKick() && (!Settings.matchKickReason() || Settings.getPunishKickReasons().stream()
				.anyMatch(reason -> event.getReason().toLowerCase().contains(reason.toLowerCase()))))
			return;

		final CombatPlayer pvPlayer = playerHandler.get(player);
		if (pvPlayer.isInCombat()) {
			pvPlayer.unTag();
		}
	}

	@EventHandler // normal priority to avoid conflict with griefprevention
	public final void onPlayerLogout(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		final CombatPlayer pvPlayer = playerHandler.get(player);
		Log.debug(player.getName() + " quit with message: " + event.getQuitMessage() + " - In combat: " + pvPlayer.isInCombat());
		if (pvPlayer.isInCombat() && !pvPlayer.hasPerm(Permissions.EXEMPT_COMBAT_LOG)) {
			if (Settings.isLogToFile()) {
				playerHandler.getConfigManager().getLog().logCombatLog(pvPlayer);
			}
			CombatUtils.executeCommands(Settings.getCommandsOnPvPLog(), player, player.getName());
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
			final Set<PvPlayer> enemies = pvPlayer.getEnemies();
			if (Settings.isUntagEnemy()) {
				enemies.forEach(enemy -> enemy.removeEnemy(pvPlayer));
			}
			pvPlayer.unTag();
		}

		// Let's process player's inventory/exp according to config file
		if (pvPlayer.hasPvPLogged()) {
			playerHandler.handleCombatLogDrops(event, player);
			return;
		}

		playerHandler.handlePlayerDrops(event, player, killer);
	}

	private void handlePvPDeath(final Player player, final CombatPlayer pvPlayer, final Player killer, final CombatPlayer pKiller, final PlayerDeathEvent event) {
		if (Settings.isKillAbuseEnabled() && !pKiller.hasPerm(Permissions.EXEMPT_KILL_ABUSE)) {
			pKiller.addVictim(player);
		}
		if (wg == null || !wg.containsRegionsAt(killer.getLocation(), Settings.getKillsWGExclusions())) {
			if (Settings.getMoneyReward() > 0) {
				pKiller.giveReward(pvPlayer);
			}
			if (Settings.getMoneyPenalty() > 0) {
				pvPlayer.applyPenalty();
			}
			if (pKiller.canExecuteKillCommand()) {
				CombatUtils.executeCommands(Settings.getCommandsOnKill(), killer, killer.getName(), player.getName());
			}
			pvPlayer.setLastDeathWasPvP(true);
			if (Settings.getExpSteal() > 0) {
				final int expWon = pKiller.giveExp(pvPlayer);
				event.setDroppedExp(0);
				event.setNewExp(player.getTotalExperience() - expWon);
				pvPlayer.message(Messages.getExpStolen(pKiller.getName(), String.valueOf(expWon)));
			}
		}
	}

	@EventHandler
	public final void onPlayerUseSoup(final PlayerInteractEvent e) {
		final Player player = e.getPlayer();
		if (CombatUtils.isWorldExcluded(player.getWorld().getName()))
			return;

		final ItemStack i = player.getItemInHand();
		final Material type = i.getType();
		if (Settings.isAutoSoupEnabled() && type == mushroomSoup) {
			if (player.getHealth() == player.getMaxHealth())
				return;
			player.setHealth(
					player.getHealth() + Settings.getSoupHealth() > player.getMaxHealth() ? player.getMaxHealth()
							: player.getHealth() + Settings.getSoupHealth());
			if (Settings.isSoupBowlDisappear()) {
				if (MCVersion.isAtLeast(MCVersion.V1_9)) {
					player.getInventory().getItemInMainHand().setAmount(0);
				} else {
					player.getInventory().setItemInHand(null);
					e.setCancelled(true);
				}
			} else {
				i.setType(Material.BOWL);
			}
		}
		final CombatPlayer pvplayer = playerHandler.get(player);
		if ((e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) && Settings.getItemCooldowns().containsKey(type)) {
			if (pvplayer.hasItemCooldown(type)) {
				final String msg = Messages.getItemCooldown(pvplayer.getItemCooldown(type));
				if (!msg.equals(msgCooldown.getIfPresent(player.getUniqueId()))) {
					pvplayer.message(msg);
					msgCooldown.put(player.getUniqueId(), msg);
				}
				e.setCancelled(true);
			} else if (!type.isEdible()) {
				Bukkit.getScheduler().runTask(playerHandler.getPlugin(), () -> pvplayer.setItemCooldown(type, Settings.getItemCooldowns().get(type)));
			}
		}
	}

	@SuppressWarnings("null") // p.getLocation() is not null
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true) // cancel on low since some plugins check cancels on normal instead of monitor
	public final void onPlayerInteract(final PlayerInteractEvent e) {
		final Player player = e.getPlayer();
		if (CombatUtils.isWorldExcluded(player.getWorld().getName()) || e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		final ItemStack i = player.getItemInHand();
		final CombatPlayer pvplayer = playerHandler.get(player);
		final Block clickedBlock = e.getClickedBlock();
		if (clickedBlock == null)
			return;

		if (i.getType() == Material.FLINT_AND_STEEL) {
			for (final Player p : clickedBlock.getWorld().getPlayers()) {
				if (player.equals(p) || !clickedBlock.getWorld().equals(p.getWorld()) || !player.canSee(p)) {
					continue;
				}
				final CombatPlayer target = playerHandler.get(p);
				if ((!target.hasPvPEnabled() || !pvplayer.hasPvPEnabled()) && clickedBlock.getLocation().distanceSquared(p.getLocation()) < 9) {
					pvplayer.message(Messages.pvpDisabledOther(target.getName()));
					e.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true) // cancel on low since some plugins check cancels on normal instead of monitor
	public final void onPlayerBlockInteract(final PlayerInteractEvent e) {
		final Player player = e.getPlayer();
		if (!Settings.blockInteract() || e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.PHYSICAL
				|| CombatUtils.isWorldExcluded(player.getWorld().getName()))
			return;

		final PvPlayer combatPlayer = ph.get(player);
		final Block clickedBlock = e.getClickedBlock();
		if (clickedBlock == null)
			return;
		if (combatPlayer.isInCombat()) {
			final Material type = clickedBlock.getType();
			for (final String material : Settings.getBlockInteractItemList()) {
				if (type.name().endsWith(material)) {
					e.setCancelled(true);
					combatPlayer.sendActionBar(Messages.getInteractBlockedInCombat(), 1000);
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
				player.sendActionBar(Messages.getNewbiePickupItemBlocked(), 1000);
			}
		}
	}

	@EventHandler
	public final void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final CombatPlayer pvPlayer = playerHandler.get(player);
		ScheduleUtils.runAsync(() -> {
			if (player.isOp() || pvPlayer.hasPerm(Permissions.ADMIN)) {
				Messages.sendQueuedMsgs(pvPlayer);
			}
		});
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public final void onPlayerTeleport(final PlayerTeleportEvent event) {
		final Player player = event.getPlayer();
		if (CombatUtils.isNPC(player)) // Citizens seems to teleport NPCs very often so let's avoid creating new PvPlayer instances
			return;

		final CombatPlayer pvplayer = playerHandler.get(player);
		if (!Settings.isInCombatEnabled() || !pvplayer.isInCombat())
			return;

		final TeleportCause cause = event.getCause();
		if (cause.equals(TeleportCause.ENDER_PEARL) && Settings.isBlockEnderPearl()) {
			event.setCancelled(true);
			pvplayer.message(Messages.getEnderpearlBlockedIncombat());
		} else if (MCVersion.isAtLeast(MCVersion.V1_9) && cause == TeleportCause.CHORUS_FRUIT
				&& Settings.isBlockChorusFruit()) {
			event.setCancelled(true);
			pvplayer.message(Messages.getChorusBlockedInCombat());
		} else if (cause.equals(TeleportCause.COMMAND) && Settings.isBlockTeleport()) {
			event.setCancelled(true);
			pvplayer.message(Messages.getTeleportBlockedInCombat());
		} else if ((cause.equals(TeleportCause.PLUGIN) || cause.equals(TeleportCause.UNKNOWN))
				&& Settings.isBlockUnsafeTeleports()) { // Some plugins use PLUGIN or UNKNOWN as the cause.
			event.setCancelled(true);
			pvplayer.message(Messages.getTeleportBlockedInCombat());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public final void onCommand(final PlayerCommandPreprocessEvent event) {
		if (Settings.isInCombatEnabled() && Settings.isStopCommands() || Settings.isNewbieProtectionEnabled()) {
			final CombatPlayer player = playerHandler.get(event.getPlayer());
			final String[] givenCommand = event.getMessage().substring(1).split(" ", 3);

			if (player.isInCombat() && !player.hasPerm(Permissions.EXEMPT_BLOCK_COMMANDS)) {
				final boolean contains = CombatUtils.recursiveContainsCommand(givenCommand, Settings.getCommandsAllowed());
				Log.debug("Command: " + Arrays.asList(givenCommand) + " | Whitelist: " + Settings.isCommandsWhitelist() + " | In list: " + contains
						+ " | Blocked: " + (Settings.isCommandsWhitelist() != contains));
				if (Settings.isCommandsWhitelist() != contains) {
					event.setCancelled(true);
					player.message(Messages.getCommandDeniedIncombat());
				}
			}
			if (player.isNewbie() && CombatUtils.recursiveContainsCommand(givenCommand, Settings.getNewbieBlacklist())) {
				event.setCancelled(true);
				player.message(Messages.getNewbieCommandBlocked());
			}
		}
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
				Messages.messageProtection(result, player, caught);
			} // TODO trigger damage actions
		}
	}

	@EventHandler
	public final void onPlayerRespawn(final PlayerRespawnEvent event) {
		if (CombatUtils.isWorldExcluded(event.getPlayer().getWorld().getName()))
			return;
		final CombatPlayer combatPlayer = ph.get(event.getPlayer());
		if (Settings.isKillAbuseEnabled() && Settings.getRespawnProtection() != 0) {
			combatPlayer.setRespawnTime(System.currentTimeMillis());
		}
		if (combatPlayer.wasLastDeathPvP()) {
			CombatUtils.executeCommands(Settings.getCommandsOnRespawn(), event.getPlayer(), event.getPlayer().getName());
			combatPlayer.setLastDeathWasPvP(false);
		}
	}

	@EventHandler
	public final void onInventoryOpen(final InventoryOpenEvent event) {
		if (!Settings.isBlockInventoryOpen())
			return;
		final CombatPlayer combatPlayer = playerHandler.get((Player) event.getPlayer());
		if (combatPlayer.isInCombat()) {
			event.setCancelled(true);
			combatPlayer.sendActionBar(Messages.getInventoryBlockedInCombat(), 1000);
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
			pvPlayer.message(Messages.getErrorPvPToggleNoPvP());
			return;
		}
		if (!pvPlayer.hasPvPEnabled() && optionState == CombatWorld.WorldOptionState.ON) {
			pvPlayer.setPvP(true);
			pvPlayer.message(Messages.getErrorPvPToggleForcePvP());
		}
	}

}
