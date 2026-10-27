package me.chancesd.pvpmanager.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.EquipmentSlot;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import me.chancesd.pvpmanager.InstanceCreator;
import me.chancesd.pvpmanager.PluginSetup;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;

@ExtendWith(InstanceCreator.class)
class BlockedActionsListenerTest {

	private static BlockedActionsListener listener;
	private static PlayerManager ph;
	private static PluginSetup pt;

	@BeforeAll
	static void setupClass() {
		pt = InstanceCreator.getPt();
		final PvPManager plugin = pt.getPlugin();
		ph = plugin.getPlayerManager();
		listener = new BlockedActionsListener(plugin.getPlayerManager());
	}

	@BeforeEach
	final void setup() {
		ph.getPlayers().clear();
	}

	private void tagPlayer(final CombatPlayer player, final CombatPlayer enemy) {
		player.tag(true, enemy);
		assertTrue(player.isInCombat());
	}

	private void tagPlayer(final CombatPlayer player) {
		tagPlayer(player, ph.createPlayer(pt.createPlayer("Attacker"), true));
	}

	@Test
	final void combatTeleportRestrictions() {
		final Player player = pt.createPlayer("combatTeleportTest");
		final CombatPlayer combatPlayer = ph.createPlayer(player, true);
		tagPlayer(combatPlayer);
		final Location location = new Location(player.getWorld(), 0, 0, 0);

		Conf.BLOCK_ENDERPEARL.set(true);
		final PlayerTeleportEvent pearlTeleport = new PlayerTeleportEvent(player, location, location, TeleportCause.ENDER_PEARL);
		listener.onPlayerTeleport(pearlTeleport);
		assertTrue(pearlTeleport.isCancelled());
		Conf.BLOCK_ENDERPEARL.set(false);

		Conf.BLOCK_TELEPORT.set(true);
		combatPlayer.getExemptions().setCanBypassTeleportBlock(true);
		final PlayerTeleportEvent commandTeleport = new PlayerTeleportEvent(player, location, location, TeleportCause.COMMAND);
		listener.onPlayerTeleport(commandTeleport);
		assertFalse(commandTeleport.isCancelled());
	}

	@Test
	final void combatFireworkRestriction() {
		final Player player = pt.createPlayer("combatFireworkTest");
		final CombatPlayer combatPlayer = ph.createPlayer(player, true);
		tagPlayer(combatPlayer);
		when(player.isGliding()).thenReturn(true);
		Conf.BLOCK_FIREWORKS_IN_COMBAT.set(true);
		final ItemStack fireworkItem = mock(ItemStack.class);
		when(fireworkItem.getType()).thenReturn(Material.FIREWORK_ROCKET);
		final PlayerInteractEvent fireworkUse = new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR,
				fireworkItem, null, BlockFace.SELF);
		listener.onFireworkUseWhileGliding(fireworkUse);
		assertEquals(Event.Result.DENY, fireworkUse.useInteractedBlock());
		assertEquals(Event.Result.DENY, fireworkUse.useItemInHand());
		Conf.BLOCK_FIREWORKS_IN_COMBAT.set(false);
	}

	@Test
	final void combatBlockPlacementRestriction() {
		final Player player = pt.createPlayer("blockedBlockPlaceTest");
		final CombatPlayer combatPlayer = ph.createPlayer(player, true);
		tagPlayer(combatPlayer);
		final Block block = mock(Block.class);

		Conf.BLOCK_PLACE_BLOCKS.set(true);
		final BlockPlaceEvent place = new BlockPlaceEvent(block, mock(BlockState.class), block,
				new ItemStack(Material.STONE), player, true, EquipmentSlot.HAND);
		listener.onBlockPlace(place);
		assertTrue(place.isCancelled());
	}

	@Test
	final void combatBlockBreakRestriction() {
		final Player player = pt.createPlayer("blockedBlockBreakTest");
		final CombatPlayer combatPlayer = ph.createPlayer(player, true);
		tagPlayer(combatPlayer);
		final Block block = mock(Block.class);

		Conf.BLOCK_BREAK_BLOCKS.set(true);
		final BlockBreakEvent breakEvent = new BlockBreakEvent(block, player);
		listener.onBlockBreak(breakEvent);
		assertTrue(breakEvent.isCancelled());
	}

	@Test
	final void combatBlockInteractionRestriction() {
		final Player player = pt.createPlayer("blockedBlockInteractTest");
		final CombatPlayer combatPlayer = ph.createPlayer(player, true);
		tagPlayer(combatPlayer);
		final Block block = mock(Block.class);
		Conf.BLOCK_INTERACT_IN_COMBAT.set(true);
		Conf.BLOCK_INTERACT_ITEM_LIST.asList().clear();
		Conf.BLOCK_INTERACT_ITEM_LIST.asList().add("CHEST");
		when(block.getType()).thenReturn(Material.CHEST);
		final PlayerInteractEvent interact = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK,
				null, block, BlockFace.UP);
		listener.onPlayerBlockInteract(interact);
		assertEquals(Event.Result.DENY, interact.useInteractedBlock());
		assertEquals(Event.Result.DENY, interact.useItemInHand());
	}

	@Test
	final void combatInventoryOpenRestriction() {
		final Player player = pt.createPlayer("blockedInventoryTest");
		final CombatPlayer combatPlayer = ph.createPlayer(player, true);
		tagPlayer(combatPlayer);
		Conf.BLOCK_INVENTORY_OPEN.set(true);
		final InventoryView view = mock(InventoryView.class);
		when(view.getPlayer()).thenReturn(player);
		final InventoryOpenEvent inventoryOpen = new InventoryOpenEvent(view);
		listener.onInventoryOpen(inventoryOpen);
		assertTrue(inventoryOpen.isCancelled());
	}

	@Test
	final void onCommandTest() {
		final Player player = pt.createPlayer("onCommandTest");
		final CombatPlayer pvPlayer = ph.createPlayer(player, true);
		final PlayerCommandPreprocessEvent commandPreprocessEvent = new PlayerCommandPreprocessEvent(player, "/spawn");

		assertFalse(commandPreprocessEvent.isCancelled());
		listener.onCommand(commandPreprocessEvent);
		assertFalse(commandPreprocessEvent.isCancelled());

		tagPlayer(pvPlayer);
		listener.onCommand(commandPreprocessEvent);
		assertTrue(commandPreprocessEvent.isCancelled());
		verify(player, atMostOnce()).sendMessage(Lang.COMMAND_DENIED_INCOMBAT.msg());

		final PlayerCommandPreprocessEvent commandPreprocessEvent2 = new PlayerCommandPreprocessEvent(player, "/tell");
		tagPlayer(pvPlayer);
		listener.onCommand(commandPreprocessEvent2);
		assertFalse(commandPreprocessEvent2.isCancelled());
	}

}
