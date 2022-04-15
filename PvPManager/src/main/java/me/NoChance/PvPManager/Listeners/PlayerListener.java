package me.NoChance.PvPManager.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
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
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;

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
		if (Settings.isBlockPlaceBlocks() && ph.get(event.getPlayer()).isInCombat()) {
			event.setCancelled(true);
			ph.get(event.getPlayer()).sendActionBar(Messages.getBlockPlaceBlockedInCombat());
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
			ph.get(event.getPlayer()).sendActionBar(Messages.getEatBlockedInCombat());
		}
	}

	@EventHandler // normal priority to avoid conflict with griefprevention
	public final void onPlayerLogout(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		final PvPlayer pvPlayer = ph.get(player);
		if (pvPlayer.isInCombat()) {
			if (Settings.isLogToFile()) {
				ph.getConfigManager().getLog().log(player.getName() + " tried to escape combat!");
			}
			for (final String s : Settings.getCommandsOnPvPLog()) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', s.replace("%p", player.getName())));
			}
			ph.applyPunishments(pvPlayer);
		}
		ph.removeUser(pvPlayer);
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
			if (Settings.isKillAbuseEnabled() && !killer.hasPermission("pvpmanager.nokillabuse")) {
				pKiller.addVictim(player.getName());
			}
			if (wg == null || !wg.containsRegionsAt(killer.getLocation(), Settings.getKillsWGExclusions())) {
				if (Settings.getMoneyReward() > 0) {
					pKiller.giveReward(pvPlayer);
				}
				if (Settings.getMoneyPenalty() > 0) {
					pvPlayer.applyPenalty();
				}
				for (final String command : Settings.getCommandsOnKill()) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("<player>", killer.getName()).replace("<victim>", player.getName()));
				}
			}
		}

		if (pvPlayer.isInCombat()) {
			ph.untag(pvPlayer);
			final PvPlayer enemy = pvPlayer.getEnemy();
			if (Settings.isUntagEnemy() && enemy != null && pvPlayer.equals(enemy.getEnemy())) {
				ph.untag(enemy);
			}
		}

		// Let's process player's inventory/exp according to config file
		if (pvPlayer.hasPvPLogged()) {
			ph.handleCombatLogDrops(event, player);
			return;
		}

		ph.handlePlayerDrops(event, player, killer);
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
			        player.getHealth() + Settings.getSoupHealth() > player.getMaxHealth() ? player.getMaxHealth() : player.getHealth() + Settings.getSoupHealth());
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

	@EventHandler(ignoreCancelled = true)
	public final void onPlayerInteract(final PlayerInteractEvent e) {
		final Player player = e.getPlayer();
		if (CombatUtils.isWorldExcluded(player.getWorld().getName()) || e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		final ItemStack i = player.getItemInHand();
		final PvPlayer pvplayer = ph.get(player);
		final Block clickedBlock = e.getClickedBlock();

		if ((i.getType() == Material.FLINT_AND_STEEL || i.getType() == Material.LAVA_BUCKET) && clickedBlock != null) {
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
			e.setCancelled(true);
			pvplayer.sendActionBar(Messages.getInteractBlockedInCombat());
		}
	}

	@EventHandler(ignoreCancelled = true)
	public final void onPlayerPickup(final PlayerPickupItemEvent e) {
		if (Settings.isNewbieProtectionEnabled() && Settings.isBlockPickNewbies()) {
			final PvPlayer player = ph.get(e.getPlayer());
			if (player.isNewbie()) {
				e.setCancelled(true);
				player.sendActionBar(Messages.getNewbiePickupItemBlocked());
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		ph.get(player).updatePlayer(player);
		if (player.isOp() || player.hasPermission("pvpmanager.admin"))
			if (!Messages.getMessageQueue().isEmpty()) {
				for (final String s : Messages.getMessageQueue()) {
					player.sendMessage(s);
				}
			}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public final void onPlayerKick(final PlayerKickEvent event) {
		final PvPlayer pvPlayer = ph.get(event.getPlayer());
		if (pvPlayer.isInCombat() && !Settings.punishOnKick()) {
			ph.untag(pvPlayer);
		}
	}

	@EventHandler
	public final void onPlayerTeleport(final PlayerTeleportEvent event) {
		final PvPlayer player = ph.get(event.getPlayer());
		if (player != null && Settings.isInCombatEnabled() && player.isInCombat())
			if (event.getCause().equals(TeleportCause.ENDER_PEARL) && Settings.isBlockEnderPearl()) {
				event.setCancelled(true);
				player.message(Messages.getEnderpearlBlockedIncombat());
			} else if (event.getCause().equals(TeleportCause.CHORUS_FRUIT) && Settings.isBlockChorusFruit()) {
				event.setCancelled(true);
				player.message(Messages.getChorusBlockedInCombat());
			} else if (event.getCause().equals(TeleportCause.COMMAND) && Settings.isBlockTeleport()) {
				event.setCancelled(true);
				player.message(Messages.getTeleportBlockedInCombat());
			}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public final void onCommand(final PlayerCommandPreprocessEvent event) {
		if (Settings.isInCombatEnabled() && Settings.isStopCommands() || Settings.isNewbieProtectionEnabled()) {
			final PvPlayer player = ph.get(event.getPlayer());
			final String[] givenCommand = event.getMessage().substring(1).split(" ", 3);

			if (player.isInCombat() && !event.getPlayer().hasPermission("pvpmanager.exempt.block.commands")) {
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
	public void onChangeWorld(final PlayerChangedWorldEvent event) {
		if (CombatUtils.isWorldExcluded(event.getPlayer().getWorld().getName()))
			return;
		if (Settings.isForcePvPOnWorldChange()) {
			ph.get(event.getPlayer()).setPvP(Settings.isDefaultPvp());
		}
	}

}
