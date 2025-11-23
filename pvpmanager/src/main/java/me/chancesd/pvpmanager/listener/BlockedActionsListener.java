package me.chancesd.pvpmanager.listener;

import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.MCVersion;
import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.utils.CombatUtils;

public class BlockedActionsListener implements Listener {

	private final PlayerManager playerHandler;
	Material fireworkMaterial;

	public BlockedActionsListener(final PlayerManager ph) {
		this.playerHandler = ph;
		if (MCVersion.isAtLeast(MCVersion.V1_13)) {
			fireworkMaterial = Material.FIREWORK_ROCKET;
		} else {
			fireworkMaterial = Material.getMaterial("FIREWORK");
		}
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
		if (!Conf.BLOCK_BREAK_BLOCKS.asBool() && !Conf.NEWBIE_BLOCK_BREAK.asBool())
			return;

		final CombatPlayer combatPlayer = playerHandler.get(event.getPlayer());
		if (Conf.BLOCK_BREAK_BLOCKS.asBool() && combatPlayer.isInCombat() || Conf.NEWBIE_BLOCK_BREAK.asBool() && combatPlayer.isNewbie()) {
			event.setCancelled(true);
			combatPlayer.sendActionBar(Lang.BLOCK_BREAK_BLOCKED_IN_COMBAT.msg(), 1000);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public final void onToggleFlight(final PlayerToggleFlightEvent event) {
		if (Conf.DISABLE_FLY.asBool() && event.isFlying() && playerHandler.get(event.getPlayer()).isInCombat()) {
			event.setCancelled(true);
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

		final CombatPlayer pvPlayer = playerHandler.get(player);
		if (!pvPlayer.isInCombat())
			return;

		final ItemStack item = event.getItem();
		if (item == null || item.getType() != fireworkMaterial)
			return;

		if (Conf.BLOCK_FIREWORKS_IN_COMBAT.asBool()) {
			event.setCancelled(true);
			pvPlayer.sendActionBar(Lang.FIREWORK_BLOCKED_IN_COMBAT.msg(), 1000);
			return;
		}

		// Check power limit
		final int powerLimit = Conf.FIREWORK_POWER_LIMIT.asInt();
		if (powerLimit >= 0 && item.hasItemMeta()) {
			final FireworkMeta meta = (FireworkMeta) item.getItemMeta();
			if (meta != null && meta.getPower() > powerLimit) {
				event.setCancelled(true);
				pvPlayer.sendActionBar(Lang.FIREWORK_POWER_LIMITED_IN_COMBAT.msg(meta.getPower()), 1000);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true) // cancel on low since some plugins check cancels on normal instead of monitor
	public final void onPlayerBlockInteract(final PlayerInteractEvent e) {
		final Player player = e.getPlayer();
		if (!Conf.BLOCK_INTERACT_IN_COMBAT.asBool() || e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.PHYSICAL
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
		final CombatPlayer combatPlayer = playerHandler.getUnchecked(player);
		if (combatPlayer == null)
			return;

		if (!Conf.COMBAT_TAG_ENABLED.asBool() || !combatPlayer.isInCombat())
			return;

		final TeleportCause cause = event.getCause();
		if (cause.equals(TeleportCause.ENDER_PEARL) && Conf.BLOCK_ENDERPEARL.asBool()) {
			event.setCancelled(true);
			combatPlayer.message(Lang.ENDERPEARL_BLOCKED_INCOMBAT);
		} else if (MCVersion.isAtLeast(MCVersion.V1_9) && cause == TeleportCause.CHORUS_FRUIT
				&& Conf.BLOCK_CHORUSFRUIT.asBool()) {
			event.setCancelled(true);
			combatPlayer.message(Lang.CHORUS_BLOCKED_IN_COMBAT);
		} else if (shouldBlockTeleport(cause) && !combatPlayer.getExemptions().canBypassTeleportBlock()) {
			event.setCancelled(true);
			combatPlayer.message(Lang.TELEPORT_BLOCKED_IN_COMBAT);
		}
	}

	private boolean shouldBlockTeleport(final TeleportCause cause) {
		return cause.equals(TeleportCause.COMMAND) && Conf.BLOCK_TELEPORT.asBool() ||
		// Some plugins use PLUGIN or UNKNOWN as the cause.
				(cause.equals(TeleportCause.PLUGIN) || cause.equals(TeleportCause.UNKNOWN)) && Conf.BLOCK_UNSAFE_TELEPORTS.asBool();
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
