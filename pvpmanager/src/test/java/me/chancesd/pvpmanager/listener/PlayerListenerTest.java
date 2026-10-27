package me.chancesd.pvpmanager.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import me.chancesd.pvpmanager.InstanceCreator;
import me.chancesd.pvpmanager.PluginSetup;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.UntagReason;
import me.chancesd.pvpmanager.player.world.CombatWorld.WorldOptionState;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.ItemCooldown;
import me.chancesd.pvpmanager.setting.ItemKey;
import me.chancesd.sdutils.scheduler.ScheduleUtils;

@ExtendWith(InstanceCreator.class)
class PlayerListenerTest {

	private static PlayerListener listener;
	private static PlayerManager ph;
	private static PluginSetup pt;
	private Player attacker;
	private Player defender;
	private CombatPlayer combatAttacker;
	private CombatPlayer combatDefender;

	@BeforeAll
	static void setupClass() {
		pt = InstanceCreator.getPt();
		final PvPManager plugin = pt.getPlugin();
		ph = plugin.getPlayerManager();
		listener = new PlayerListener(plugin.getPlayerManager());
	}

	@BeforeEach
	final void setup() {
		ph.getPlayers().clear();
		// Create fresh players for each test
		attacker = pt.createPlayer("Attacker");
		defender = pt.createPlayer("Defender");
		combatAttacker = ph.createPlayer(attacker, true);
		combatDefender = ph.createPlayer(defender, true);
		combatAttacker.waitForPlayerToLoad();
		combatDefender.waitForPlayerToLoad();
	}

	private void tagPlayer(final CombatPlayer player, final CombatPlayer enemy) {
		player.tag(true, enemy);
		assertTrue(player.isInCombat());
	}

	private void tagPlayer(final CombatPlayer player) {
		tagPlayer(player, combatAttacker);
	}

	@Test
	void onPlayerJoinTest() {
		ph.getPlayers().clear();
		final Player testPlayer = pt.createPlayer("TestJoinPlayer");

		assertEquals(0, ph.getPlayers().size());
		listener.onPlayerJoin(new PlayerJoinEvent(testPlayer, ""));
		assertEquals(1, ph.getPlayers().size());
		assertEquals(testPlayer, ph.getPlayers().values().stream().findFirst().get().getPlayer());
	}

	@Test
	void onChangeWorldTest() {
		combatAttacker.getCombatWorld().setForcePVP(WorldOptionState.OFF);
		listener.onChangeWorld(new PlayerChangedWorldEvent(attacker, attacker.getWorld()));
		assertFalse(combatAttacker.hasPvPEnabled());

		combatAttacker.getCombatWorld().setForcePVP(WorldOptionState.ON);
		listener.onChangeWorld(new PlayerChangedWorldEvent(attacker, attacker.getWorld()));
		assertTrue(combatAttacker.hasPvPEnabled());
	}

	@Test
	void onPlayerCooldownTest() {
		final ItemStack item = new ItemStack(Material.STONE);
		final ItemKey key = ItemKey.wildcard(Material.STONE);
		final ItemCooldown oldCooldown = Conf.ITEM_COOLDOWNS.asMap().put(key, new ItemCooldown(30, 30));
		try {
			combatAttacker.setItemCooldown(key, new ItemCooldown(30, 30));

			final PlayerInteractEvent interact = new PlayerInteractEvent(attacker, Action.RIGHT_CLICK_AIR, item, null, BlockFace.SELF);
			listener.onPlayerCooldown(interact);
			assertEquals(Event.Result.DENY, interact.useInteractedBlock());
			assertEquals(Event.Result.DENY, interact.useItemInHand());
		} finally {
			if (oldCooldown == null) {
				Conf.ITEM_COOLDOWNS.asMap().remove(key);
			} else {
				Conf.ITEM_COOLDOWNS.asMap().put(key, oldCooldown);
			}
		}
	}

	@Test
	void onPlayerInteractAndBucketEmptyProtectionTest() {
		final org.bukkit.World world = attacker.getWorld();
		when(attacker.getLocation()).thenReturn(new Location(world, 0, 0, 0));
		when(defender.getLocation()).thenReturn(new Location(world, 1, 0, 0));
		when(world.getPlayers()).thenReturn(List.of(attacker, defender));
		combatDefender.setPvP(false);

		final Block block = mock(Block.class);
		when(block.getWorld()).thenReturn(world);
		when(block.getLocation()).thenReturn(new Location(world, 0, 0, 0));
		final PlayerInteractEvent flintAndSteel = new PlayerInteractEvent(attacker, Action.RIGHT_CLICK_BLOCK,
				new ItemStack(Material.FLINT_AND_STEEL), block, BlockFace.UP);
		listener.onPlayerInteract(flintAndSteel);
		assertEquals(Event.Result.DENY, flintAndSteel.useInteractedBlock());
		assertEquals(Event.Result.DENY, flintAndSteel.useItemInHand());

		final PlayerBucketEmptyEvent lavaBucket = new PlayerBucketEmptyEvent(attacker, block, block, BlockFace.UP,
				Material.LAVA_BUCKET, new ItemStack(Material.LAVA_BUCKET), EquipmentSlot.HAND);
		listener.onBucketEmpty(lavaBucket);
		assertTrue(lavaBucket.isCancelled());
	}

	@Test
	void onPlayerEatTest() {
		final ItemStack goldenApple = new ItemStack(Material.GOLDEN_APPLE);
		final PlayerItemConsumeEvent eat = new PlayerItemConsumeEvent(attacker, goldenApple, EquipmentSlot.HAND);
		listener.onPlayerEat(eat);
		assertFalse(eat.isCancelled());
		verify(attacker).setCooldown(Material.GOLDEN_APPLE, 600);
	}

	@Test
	void onProjectileLaunchEventTest() {
		final Projectile pearl = mock(Projectile.class);
		when(pearl.getType()).thenReturn(org.bukkit.entity.EntityType.ENDER_PEARL);
		when(pearl.getShooter()).thenReturn(attacker);
		tagPlayer(combatAttacker, combatDefender);
		final AtomicReference<Runnable> scheduledTask = new AtomicReference<>();
		try (MockedStatic<ScheduleUtils> scheduleUtils = Mockito.mockStatic(ScheduleUtils.class)) {
			scheduleUtils.when(() -> ScheduleUtils.runPlatformTask(any(Runnable.class), eq(attacker)))
					.thenAnswer(invocation -> {
						scheduledTask.set(invocation.getArgument(0));
						return null;
					});
			listener.onProjectileLaunchEvent(new ProjectileLaunchEvent(pearl));
			assertNotNull(scheduledTask.get());
			scheduledTask.get().run();
			verify(attacker).setCooldown(Material.ENDER_PEARL, 60);
		}
	}

	@Test
	void onPlayerLogoutTest() {
		tagPlayer(combatDefender, combatAttacker);

		listener.onPlayerLogout(new PlayerQuitEvent(defender, ""));
		listener.onPlayerLogoutMonitor(new PlayerQuitEvent(defender, ""));
		verify(defender, times(1)).setHealth(0);
	}

	@Test
	void onPlayerKickTest() {
		tagPlayer(combatAttacker);
		listener.onPlayerKick(new PlayerKickEvent(attacker, "", ""));
		assertTrue(combatAttacker.isInCombat());

		Conf.MATCH_KICK_REASON.set(true);
		tagPlayer(combatAttacker);
		listener.onPlayerKick(new PlayerKickEvent(attacker, "", ""));
		assertFalse(combatAttacker.isInCombat());

		tagPlayer(combatAttacker);
		listener.onPlayerKick(new PlayerKickEvent(attacker, "Kicked for spamming", ""));
		assertTrue(combatAttacker.isInCombat());

		combatAttacker.untag(UntagReason.PLUGIN_API);
		assertFalse(combatAttacker.isInCombat());
		tagPlayer(combatAttacker);
		listener.onPlayerKick(new PlayerKickEvent(attacker, "Random text - Kicked for spamming", ""));
		assertTrue(combatAttacker.isInCombat());
	}

	private PlayerDeathEvent createDeathEvent(final Player player) {
		final PlayerDeathEvent event = mock(PlayerDeathEvent.class);
		when(event.getEntity()).thenReturn(player);
		when(player.getKiller()).thenReturn(attacker);
		return event;
	}

	@Test
	final void regularDeath() {
		assertFalse(combatDefender.isInCombat());
		listener.onPlayerDeath(createDeathEvent(defender));
	}

	@Test
	final void inCombatDeath() {
		// Test 1: Default config (UNTAG_ON_KILL=false, SELF_TAG=false)
		tagPlayer(combatDefender, combatAttacker);
		tagPlayer(combatAttacker, combatDefender);
		listener.onPlayerDeath(createDeathEvent(defender));
		assertFalse(combatDefender.isInCombat());
		assertTrue(combatAttacker.isInCombat());

		// Test 2: UNTAG_ON_KILL=true
		Conf.UNTAG_ON_KILL.set(true);
		tagPlayer(combatDefender, combatAttacker);
		tagPlayer(combatAttacker, combatDefender);
		listener.onPlayerDeath(createDeathEvent(defender));
		assertFalse(combatDefender.isInCombat());
		assertFalse(combatAttacker.isInCombat());

		// Test 3: SELF_TAG=true
		Conf.SELF_TAG.set(true);
		tagPlayer(combatAttacker, combatAttacker);
		tagPlayer(combatDefender, combatAttacker);
		tagPlayer(combatAttacker, combatDefender);
		listener.onPlayerDeath(createDeathEvent(defender));
		assertFalse(combatDefender.isInCombat());
		assertFalse(combatAttacker.isInCombat());
	}

	@Test
	final void deathEdgeCases() {
		Conf.SELF_TAG.set(false);
		tagPlayer(combatDefender, combatAttacker);
		PlayerDeathEvent event = mock(PlayerDeathEvent.class);
		when(event.getEntity()).thenReturn(defender);
		when(defender.getKiller()).thenReturn(defender);
		
		listener.onPlayerDeath(event);
		assertFalse(combatDefender.isInCombat(), "Player should be untagged after self-kill");
		
		// Test 2: Environmental death (killer = null)
		tagPlayer(combatDefender, combatAttacker);
		event = mock(PlayerDeathEvent.class);
		when(event.getEntity()).thenReturn(defender);
		when(defender.getKiller()).thenReturn(null);
		
		listener.onPlayerDeath(event);
		assertFalse(combatDefender.isInCombat(), "Player should be untagged after environmental death");
	}

}
