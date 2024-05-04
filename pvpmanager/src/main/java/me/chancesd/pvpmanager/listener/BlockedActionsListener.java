package me.chancesd.pvpmanager.listener;

import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.MCVersion;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.utils.CombatUtils;

public class BlockedActionsListener implements Listener {

	private final PlayerManager playerHandler;

	public BlockedActionsListener(final PlayerManager ph) {
		this.playerHandler = ph;
	}

	@EventHandler(ignoreCancelled = true)
	public final void onBlockPlace(final BlockPlaceEvent event) {
		if (!Conf.BLOCK_PLACE_BLOCKS.asBool() && !Conf.NEWBIE_BLOCK_PLACE.asBool())
			return;

		final CombatPlayer combatPlayer = playerHandler.get(event.getPlayer());
		if (Conf.BLOCK_PLACE_BLOCKS.asBool() && combatPlayer.isInCombat() || Conf.NEWBIE_BLOCK_PLACE.asBool() && combatPlayer.isNewbie()) {
			event.setCancelled(true);
			combatPlayer.sendActionBar(Lang.BLOCK_PLACE_BLOCKED_IN_COMBAT.msg(), 1000);
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
		if (Conf.DISABLE_FLY.asBool() && event.isFlying() && playerHandler.get(event.getPlayer()).isInCombat()) {
			event.setCancelled(true);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true)
	public final void onPlayerPickup(final PlayerPickupItemEvent e) {
		if (Conf.NEWBIE_ENABLED.asBool() && Conf.NEWBIE_BLOCK_PICK.asBool()) {
			final CombatPlayer player = playerHandler.get(e.getPlayer());
			if (player.isNewbie()) {
				e.setCancelled(true);
				player.sendActionBar(Lang.NEWBIE_PICKUP_ITEM_BLOCKED.msg(), 1000);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public final void onPlayerTeleport(final PlayerTeleportEvent event) {
		final Player player = event.getPlayer();
		if (CombatUtils.isNPC(player)) // Citizens seems to teleport NPCs very often so let's avoid creating new PvPlayer instances
			return;

		final CombatPlayer pvplayer = playerHandler.get(player);
		if (!Conf.COMBAT_TAG_ENABLED.asBool() || !pvplayer.isInCombat())
			return;

		if (event.getCause().equals(TeleportCause.ENDER_PEARL) && Conf.BLOCK_ENDERPEARL.asBool()) {
			event.setCancelled(true);
			pvplayer.message(Lang.ENDERPEARL_BLOCKED_INCOMBAT);
		} else if (MCVersion.isAtLeast(MCVersion.V1_9) && event.getCause() == TeleportCause.CHORUS_FRUIT
				&& Conf.BLOCK_CHORUSFRUIT.asBool()) {
			event.setCancelled(true);
			pvplayer.message(Lang.CHORUS_BLOCKED_IN_COMBAT);
		} else if (event.getCause().equals(TeleportCause.COMMAND) && Conf.BLOCK_TELEPORT.asBool()) {
			event.setCancelled(true);
			pvplayer.message(Lang.TELEPORT_BLOCKED_IN_COMBAT);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public final void onCommand(final PlayerCommandPreprocessEvent event) {
		if (Conf.COMBAT_TAG_ENABLED.asBool() && Conf.BLOCK_COMMANDS.asBool() || Conf.NEWBIE_ENABLED.asBool()) {
			final CombatPlayer player = playerHandler.get(event.getPlayer());
			final String[] givenCommand = event.getMessage().substring(1).split(" ", 3);

			if (player.isInCombat() && !player.hasPerm(Permissions.EXEMPT_BLOCK_COMMANDS)) {
				final boolean contains = CombatUtils.recursiveContainsCommand(givenCommand, Conf.COMMANDS_ALLOWED.asList());
				Log.debug("Command: " + Arrays.asList(givenCommand) + " | Whitelist: " + Conf.BLOCK_COMMANDS_WHITELIST.asBool() + " | In list: " + contains
						+ " | Blocked: " + (Conf.BLOCK_COMMANDS_WHITELIST.asBool() != contains));
				if (Conf.BLOCK_COMMANDS_WHITELIST.asBool() != contains) {
					event.setCancelled(true);
					player.message(Lang.COMMAND_DENIED_INCOMBAT);
				}
			}
			if (player.isNewbie() && CombatUtils.recursiveContainsCommand(givenCommand, Conf.NEWBIE_BLACKLIST.asList())) {
				event.setCancelled(true);
				player.message(Lang.NEWBIE_COMMAND_BLOCKED.msg());
			}
		}
	}

	@EventHandler
	public final void onInventoryOpen(final InventoryOpenEvent event) {
		if (!Conf.BLOCK_INVENTORY_OPEN.asBool())
			return;
		final CombatPlayer combatPlayer = playerHandler.get((Player) event.getPlayer());
		if (combatPlayer.isInCombat()) {
			event.setCancelled(true);
			combatPlayer.sendActionBar(Lang.INVENTORY_BLOCKED_IN_COMBAT.msg(), 1000);
		}
	}

}
