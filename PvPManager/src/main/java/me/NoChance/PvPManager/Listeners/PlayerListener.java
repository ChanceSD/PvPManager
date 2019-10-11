package me.NoChance.PvPManager.Listeners;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Settings.Settings.DropMode;
import me.NoChance.PvPManager.Utils.CombatUtils;

@SuppressWarnings("deprecation")
public class PlayerListener implements Listener {

	private final PlayerHandler ph;
	private Material mushroomSoup;

	public PlayerListener(final PlayerHandler ph) {
		this.ph = ph;
		if (CombatUtils.isVersionSuperior(Settings.getMinecraftVersion(), "1.12.2")) {
			mushroomSoup = Material.getMaterial("MUSHROOM_STEW");
		} else {
			mushroomSoup = Material.getMaterial("MUSHROOM_SOUP");
		}
	}

	@EventHandler(ignoreCancelled = true)
	public final void onBlockPlace(final BlockPlaceEvent event) {
		if (Settings.isBlockPlaceBlocks() && ph.get(event.getPlayer()).isInCombat()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public final void onToggleFlight(final PlayerToggleFlightEvent event) {
		if (Settings.isDisableFly() && event.isFlying() && ph.get(event.getPlayer()).isInCombat()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public final void onPlayerLogout(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		final PvPlayer pvPlayer = ph.get(player);
		if (pvPlayer.isInCombat()) {
			if (Settings.isLogToFile()) {
				ph.getPlugin().getLog().log(player.getName() + " tried to escape combat!");
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
		if (!CombatUtils.isWorldAllowed(player.getWorld().getName()))
			return;
		final PvPlayer pvPlayer = ph.get(player);

		// Let's process player's inventory/exp according to config file
		if (pvPlayer.hasPvPLogged()) {
			if (!Settings.isDropExp()) {
				event.setKeepLevel(true);
				event.setDroppedExp(0);
			}
			if (!Settings.isDropInventory() && Settings.isDropArmor()) {
				CombatUtils.fakeItemStackDrop(player, player.getInventory().getArmorContents());
				player.getInventory().setArmorContents(null);
			} else if (Settings.isDropInventory() && !Settings.isDropArmor()) {
				CombatUtils.fakeItemStackDrop(player, player.getInventory().getContents());
				player.getInventory().clear();
			}
			if (!Settings.isDropInventory() || !Settings.isDropArmor()) {
				event.setKeepInventory(true);
				event.getDrops().clear();
			}
		}

		final Player killer = player.getKiller();
		final boolean pvpDeath = killer != null;
		// Player died in combat, process that
		if (pvpDeath && !killer.equals(player)) {
			final PvPlayer pKiller = ph.get(killer);
			if (Settings.isKillAbuseEnabled()) {
				pKiller.addVictim(player.getName());
			}
			if (Settings.getMoneyReward() > 0) {
				pKiller.giveReward(player);
			}
			if (Settings.getMoneyPenalty() > 0) {
				pvPlayer.applyPenalty();
			}
			for (final String command : Settings.getCommandsOnKill()) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("<player>", killer.getName()).replace("<victim>", player.getName()));
			}
		}
		if (!pvPlayer.hasPvPLogged()) {
			final DropMode mode = Settings.getDropMode();
			switch (mode) {
			case DROP:
				if (!pvpDeath && !pvPlayer.isInCombat()) {
					event.setKeepInventory(true);
					event.getDrops().clear();
				}
				break;
			case KEEP:
				if (pvpDeath || pvPlayer.isInCombat()) {
					event.setKeepInventory(true);
					event.getDrops().clear();
				}
				break;
			case TRANSFER:
				if (pvpDeath) {
					final ItemStack[] drops = event.getDrops().toArray(new ItemStack[event.getDrops().size()]);
					final HashMap<Integer, ItemStack> returned = killer.getInventory().addItem(drops);
					CombatUtils.fakeItemStackDrop(player, returned.values().toArray(new ItemStack[returned.values().size()]));
					event.getDrops().clear();
				}
				break;
			default:
				break;
			}
		}
		if (pvPlayer.isInCombat()) {
			ph.untag(pvPlayer);
			final PvPlayer enemy = pvPlayer.getEnemy();
			if (enemy != null && enemy.getEnemy().equals(pvPlayer)) {
				ph.untag(enemy);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public final void onPlayerInteract(final PlayerInteractEvent e) {
		final Player player = e.getPlayer();
		if (CombatUtils.isWorldAllowed(player.getWorld().getName())) {
			final ItemStack i = player.getItemInHand();
			final PvPlayer pvplayer = ph.get(player);
			if (Settings.isAutoSoupEnabled() && i.getType() == mushroomSoup) {
				if (player.getHealth() == player.getMaxHealth())
					return;
				player.setHealth(player.getHealth() + Settings.getSoupHealth() > player.getMaxHealth() ? player.getMaxHealth() : player.getHealth() + Settings.getSoupHealth());
				i.setType(Material.BOWL);
				return;
			}
			if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				if (i.getType() == Material.FLINT_AND_STEEL || i.getType() == Material.LAVA_BUCKET) {
					for (final Player p : e.getClickedBlock().getWorld().getPlayers()) {
						if (player.equals(p) || !e.getClickedBlock().getWorld().equals(p.getWorld()) || !player.canSee(p)) {
							continue;
						}
						final PvPlayer target = ph.get(p);
						if ((!target.hasPvPEnabled() || !pvplayer.hasPvPEnabled()) && e.getClickedBlock().getLocation().distanceSquared(p.getLocation()) < 9) {
							pvplayer.message(Messages.pvpDisabledOther(target.getName()));
							e.setCancelled(true);
							return;
						}
					}
				}
				if (Settings.blockInteract() && pvplayer.isInCombat()) {
					e.setCancelled(true);
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

	@EventHandler
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
			} else if (event.getCause().equals(TeleportCause.COMMAND) && Settings.isBlockTeleport()) {
				event.setCancelled(true);
				player.message("§cYou can't teleport while in combat!");
			}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public final void onCommand(final PlayerCommandPreprocessEvent event) {
		if (Settings.isInCombatEnabled() && Settings.isStopCommands() || Settings.isNewbieProtectionEnabled()) {
			final PvPlayer player = ph.get(event.getPlayer());
			final String[] givenCommand = event.getMessage().substring(1).split(" ", 3);

			if (player.isInCombat()) {
				final boolean contains = CombatUtils.recursiveContainsCommand(givenCommand, Settings.getCommandsAllowed());
				if (Settings.isCommandsWhitelist() ? !contains : contains) {
					event.setCancelled(true);
					player.message(Messages.getCommandDeniedIncombat());
				}
			}
			if (player.isNewbie() && CombatUtils.recursiveContainsCommand(givenCommand, Settings.getNewbieBlacklist())) {
				event.setCancelled(true);
				// TODO Make configurable
				player.message("§cYou cannot use this command while you have PvP protection!");
			}
		}

	}

	@EventHandler
	public final void onPlayerRespawn(final PlayerRespawnEvent event) {
		if (CombatUtils.isWorldAllowed(event.getPlayer().getWorld().getName()))
			if (Settings.isKillAbuseEnabled() && Settings.getRespawnProtection() != 0) {
				final PvPlayer player = ph.get(event.getPlayer());
				player.setRespawnTime(System.currentTimeMillis());
			}
	}

	@EventHandler
	public void onChangeWorld(final PlayerChangedWorldEvent event) {
		if (!CombatUtils.isWorldAllowed(event.getPlayer().getWorld().getName()))
			return;
		if (Settings.isForcePvPOnWorldChange()) {
			ph.get(event.getPlayer()).setPvP(Settings.isDefaultPvp());
		}
	}

}
