package me.NoChance.PvPManager.Listeners;

import me.chancesd.pvpmanager.world.CombatWorld;
import me.chancesd.sdutils.utils.Log;

import java.util.Set;

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
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.WorldGuardHook;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Player.CancelResult;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.utils.ScheduleUtils;

@SuppressWarnings("deprecation")
public class PlayerListener implements Listener {

	private final PlayerHandler ph;
	private final WorldGuardHook wg;
	private Material mushroomSoup;

	public PlayerListener(final PlayerHandler ph) {
		this.ph = ph;
		this.wg = (WorldGuardHook) ph.getPlugin().getDependencyManager().getDependency(Hook.WORLDGUARD);
		if (CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.13")) {
			mushroomSoup = Material.MUSHROOM_STEW;
		} else if (CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.0")) { // avoid loading Material class on unit tests
			mushroomSoup = Material.getMaterial("MUSHROOM_SOUP");
		}
	}

	@EventHandler(ignoreCancelled = true)
	public final void onBlockPlace(final BlockPlaceEvent event) {
		if (!Settings.isBlockPlaceBlocks() && !Settings.isBlockPlaceBlocksNewbie())
			return;

		final PvPlayer combatPlayer = ph.get(event.getPlayer());
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
		if (Settings.isDisableFly() && event.isFlying() && ph.get(event.getPlayer()).isInCombat()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public final void onPlayerEat(final PlayerItemConsumeEvent event) {
		if (Settings.isBlockEat() && ph.get(event.getPlayer()).isInCombat() && event.getItem().getType().isEdible()) {
			event.setCancelled(true);
			ph.get(event.getPlayer()).sendActionBar(Messages.getEatBlockedInCombat(), 1000);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public final void onPlayerKick(final PlayerKickEvent event) {
		final Player player = event.getPlayer();
		Log.debugLazy(() -> player.getName() + " was kicked with reason: " + event.getReason() + " | Leave message: " + event.getLeaveMessage()
				+ " - In combat: " + ph.get(player).isInCombat());
		if (Settings.punishOnKick() && (!Settings.matchKickReason() || Settings.getPunishKickReasons().contains(event.getReason())))
			return;

		final PvPlayer pvPlayer = ph.get(player);
		if (pvPlayer.isInCombat()) {
			ph.untag(pvPlayer);
		}
	}

	@EventHandler // normal priority to avoid conflict with griefprevention
	public final void onPlayerLogout(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		final PvPlayer pvPlayer = ph.get(player);
		Log.debug(player.getName() + " quit with reason: " + event.getQuitMessage() + " - In combat: " + pvPlayer.isInCombat());
		if (pvPlayer.isInCombat() && !pvPlayer.hasPerm(Permissions.EXEMPT_COMBAT_LOG)) {
			if (Settings.isLogToFile()) {
				ph.getConfigManager().getLog().log(player.getName() + " tried to escape combat!");
			}
			CombatUtils.executeCommands(Settings.getCommandsOnPvPLog(), player, player.getName());
			ph.applyPunishments(pvPlayer);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public final void onPlayerLogoutMonitor(final PlayerQuitEvent event) {
		ph.removeUser(ph.get(event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public final void onPlayerDeath(final PlayerDeathEvent event) {
		final Player player = event.getEntity();
		if (CombatUtils.isWorldExcluded(player.getWorld().getName()))
			return;

		final PvPlayer pvPlayer = ph.get(player);
		final Player killer = player.getKiller();

		// Player died in combat, process that
		if (killer != null && !killer.equals(player)) {
			final PvPlayer pKiller = ph.get(killer);
			handlePvPDeath(player, pvPlayer, killer, pKiller);
		}

		if (pvPlayer.isInCombat()) {
			final Set<PvPlayer> enemies = pvPlayer.getEnemies();
			if (Settings.isUntagEnemy()) {
				enemies.forEach(enemy -> enemy.removeEnemy(pvPlayer));
			}
			ph.untag(pvPlayer);
		}

		// Let's process player's inventory/exp according to config file
		if (pvPlayer.hasPvPLogged()) {
			ph.handleCombatLogDrops(event, player);
			return;
		}

		ph.handlePlayerDrops(event, player, killer);
	}

	private void handlePvPDeath(final Player player, final PvPlayer pvPlayer, final Player killer, final PvPlayer pKiller) {
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
			CombatUtils.executeCommands(Settings.getCommandsOnKill(), killer, killer.getName(), player.getName());
		}
	}

	@EventHandler
	public final void onPlayerUseSoup(final PlayerInteractEvent e) {
		final Player player = e.getPlayer();
		if (CombatUtils.isWorldExcluded(player.getWorld().getName()))
			return;

		final ItemStack i = player.getItemInHand();
		if (Settings.isAutoSoupEnabled() && i.getType() == mushroomSoup) {
			if (player.getHealth() == player.getMaxHealth())
				return;
			player.setHealth(
					player.getHealth() + Settings.getSoupHealth() > player.getMaxHealth() ? player.getMaxHealth()
							: player.getHealth() + Settings.getSoupHealth());
			if (Settings.isSoupBowlDisappear()) {
				if (CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.9")) {
					player.getInventory().getItemInMainHand().setAmount(0);
				} else {
					player.getInventory().setItemInHand(null);
					e.setCancelled(true);
				}
			} else {
				i.setType(Material.BOWL);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true) // cancel on low since some plugins check cancels on normal instead of monitor
	public final void onPlayerInteract(final PlayerInteractEvent e) {
		final Player player = e.getPlayer();
		if (CombatUtils.isWorldExcluded(player.getWorld().getName()) || e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		final ItemStack i = player.getItemInHand();
		final PvPlayer pvplayer = ph.get(player);
		final Block clickedBlock = e.getClickedBlock();
		if (clickedBlock == null)
			return;

		if (i.getType() == Material.FLINT_AND_STEEL || i.getType() == Material.LAVA_BUCKET) {
			for (final Player p : clickedBlock.getWorld().getPlayers()) {
				if (player.equals(p) || !clickedBlock.getWorld().equals(p.getWorld()) || !player.canSee(p)) {
					continue;
				}
				final PvPlayer target = ph.get(p);
				if ((!target.hasPvPEnabled() || !pvplayer.hasPvPEnabled()) && clickedBlock.getLocation().distanceSquared(p.getLocation()) < 9) {
					pvplayer.message(Messages.pvpDisabledOther(target.getName()));
					e.setCancelled(true);
					return;
				}
			}
		}
		if (Settings.blockInteract() && pvplayer.isInCombat()) {
			final Material type = clickedBlock.getType();
			for (final String material : Settings.getBlockInteractItemList()) {
				if (type.name().endsWith(material)) {
					e.setCancelled(true);
					pvplayer.sendActionBar(Messages.getInteractBlockedInCombat(), 1000);
					return;
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public final void onPlayerPickup(final PlayerPickupItemEvent e) {
		if (Settings.isNewbieProtectionEnabled() && Settings.isBlockPickNewbies()) {
			final PvPlayer player = ph.get(e.getPlayer());
			if (player.isNewbie()) {
				e.setCancelled(true);
				player.sendActionBar(Messages.getNewbiePickupItemBlocked(), 1000);
			}
		}
	}

	@EventHandler
	public final void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final PvPlayer pvPlayer = ph.get(player);
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

		final PvPlayer pvplayer = ph.get(player);
		if (!Settings.isInCombatEnabled() || !pvplayer.isInCombat())
			return;

		if (event.getCause().equals(TeleportCause.ENDER_PEARL) && Settings.isBlockEnderPearl()) {
			event.setCancelled(true);
			pvplayer.message(Messages.getEnderpearlBlockedIncombat());
		} else if (CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.9") && event.getCause() == TeleportCause.CHORUS_FRUIT
				&& Settings.isBlockChorusFruit()) {
			event.setCancelled(true);
			pvplayer.message(Messages.getChorusBlockedInCombat());
		} else if (event.getCause().equals(TeleportCause.COMMAND) && Settings.isBlockTeleport()) {
			event.setCancelled(true);
			pvplayer.message(Messages.getTeleportBlockedInCombat());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public final void onCommand(final PlayerCommandPreprocessEvent event) {
		if (Settings.isInCombatEnabled() && Settings.isStopCommands() || Settings.isNewbieProtectionEnabled()) {
			final PvPlayer player = ph.get(event.getPlayer());
			final String[] givenCommand = event.getMessage().substring(1).split(" ", 3);

			if (player.isInCombat() && !player.hasPerm(Permissions.EXEMPT_BLOCK_COMMANDS)) {
				final boolean contains = CombatUtils.recursiveContainsCommand(givenCommand, Settings.getCommandsAllowed());
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

		if (event.getState() == State.CAUGHT_ENTITY && event.getCaught() instanceof Player) {
			final Player caught = (Player) event.getCaught();
			final CancelResult result = ph.tryCancel(player, caught);
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
		if (Settings.isKillAbuseEnabled() && Settings.getRespawnProtection() != 0) {
			final PvPlayer player = ph.get(event.getPlayer());
			player.setRespawnTime(System.currentTimeMillis());
		}
	}

	@EventHandler
	public final void onInventoryOpen(final InventoryOpenEvent event) {
		if (!Settings.isBlockInventoryOpen())
			return;
		final PvPlayer combatPlayer = ph.get((Player) event.getPlayer());
		if (combatPlayer.isInCombat()) {
			event.setCancelled(true);
			combatPlayer.sendActionBar(Messages.getInventoryBlockedInCombat(), 1000);
		}
	}

	@EventHandler
	public void onChangeWorld(final PlayerChangedWorldEvent event) {
		final Player player = event.getPlayer();
		final PvPlayer pvPlayer = ph.get(player);
		final CombatWorld combatWorld = ph.getPlugin().getWorldManager().getWorld(player.getWorld());
		pvPlayer.setCombatWorld(combatWorld);

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
			return;
		}
	}

}
