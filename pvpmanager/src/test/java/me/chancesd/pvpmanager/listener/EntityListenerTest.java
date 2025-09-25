package me.chancesd.pvpmanager.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.mockStatic;

import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import me.chancesd.pvpmanager.InstanceCreator;
import me.chancesd.pvpmanager.PluginSetup;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.ProtectionType;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.utils.CombatUtils;

@ExtendWith(InstanceCreator.class)
public class EntityListenerTest {

	private static final PluginSetup PT = InstanceCreator.getPt();
	private static EntityListener damageListener;
	private EntityDamageByEntityEvent mockEvent;
	private EntityDamageByEntityEvent projMockEvent;
	private static PlayerManager ph;
	private Player attacker;
	private Player defender;
	private CombatPlayer combatAttacker;
	private CombatPlayer combatDefender;

	@BeforeAll
	public static void setupClass() {
		final PvPManager plugin = PT.getPlugin();
		ph = plugin.getPlayerManager();
		damageListener = new EntityListener(ph);
		Conf.PVP_BLOOD.disable(); // avoid loading Material class while testing
	}

	@BeforeEach
	public final void setup() {
		ph.getPlayers().clear();
		ph.setGlobalStatus(true);
		// Create fresh players for each test
		attacker = PT.createPlayer("Attacker");
		defender = PT.createPlayer("Defender");
		combatAttacker = ph.get(attacker);
		combatDefender = ph.get(defender);
	}

	private void createAttack(final boolean cancelled, final Player attackerPlayer) {
		createAttack(cancelled, attackerPlayer, defender);
	}

	private void createAttack(final boolean cancelled, final Player attackerPlayer, final Player defenderPlayer) {
		mockEvent = createDamageEvent(attackerPlayer, defenderPlayer, cancelled);

		final Projectile proj = mock(Projectile.class);
		when(proj.getShooter()).thenReturn(attackerPlayer);
		projMockEvent = createDamageEvent(proj, defenderPlayer, cancelled);

		callEvent(mockEvent);
		callEvent(projMockEvent);
	}

	private void createAttack(final boolean cancelled) {
		createAttack(cancelled, attacker);
	}

	private EntityDamageByEntityEvent createDamageEvent(final Entity attackerEntity, final Entity defenderEntity, final boolean cancelled) {
		final EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
		when(event.getDamager()).thenReturn(attackerEntity);
		when(event.getEntity()).thenReturn(defenderEntity);
		when(event.getDamage()).thenReturn(5.0);
		Mockito.doCallRealMethod().when(event).setCancelled(ArgumentMatchers.anyBoolean());
		when(event.isCancelled()).thenCallRealMethod();
		event.setCancelled(cancelled);
		return event;
	}

	private void createMobAttack(final boolean mobAttacker, final boolean cancelled) {
		final Zombie zombie = mock(Zombie.class, RETURNS_MOCKS);
		if (mobAttacker) {
			mockEvent = createDamageEvent(zombie, defender, cancelled);
		} else {
			mockEvent = createDamageEvent(attacker, zombie, cancelled);
		}

		final Projectile proj = mock(Projectile.class);
		when(proj.getShooter()).thenReturn(attacker);
		if (mobAttacker) {
			projMockEvent = createDamageEvent(proj, defender, cancelled);
		} else {
			projMockEvent = createDamageEvent(proj, zombie, cancelled);
		}

		callEvent(mockEvent);
		callEvent(projMockEvent);
	}

	private void callEvent(final EntityDamageByEntityEvent event) {
		if (!event.isCancelled()) { // ignore cancelled true
			damageListener.onPlayerDamage(event);
		}
		damageListener.onPlayerDamageOverride(event);
		if (!event.isCancelled()) { // ignore cancelled true
			damageListener.onPlayerDamageMonitor(event);
		}
	}

	@Test
	final void testMobAttack() {
		createMobAttack(false, false);
		assertFalse(mockEvent.isCancelled());
		assertFalse(projMockEvent.isCancelled());

		createMobAttack(true, false);
		assertFalse(mockEvent.isCancelled());
		assertFalse(projMockEvent.isCancelled());
	}

	@Test
	final void testSelfTag() {
		final Projectile proj = mock(Projectile.class);
		projMockEvent = createDamageEvent(proj, defender, false);

		assertFalse(Conf.SELF_TAG.asBool());
		// attacker different from defender
		createAttack(false, attacker);
		assertTrue(CombatUtils.isPvP(mockEvent));
		assertTrue(CombatUtils.isPvP(projMockEvent));

		// attacker equals defender
		createAttack(false, defender);
		assertFalse(CombatUtils.isPvP(mockEvent));
		assertFalse(CombatUtils.isPvP(projMockEvent));

		// now allow self tagging
		Conf.SELF_TAG.set(true);
		assertTrue(Conf.SELF_TAG.asBool());
		// attacker different from defender
		createAttack(false, attacker);
		assertTrue(CombatUtils.isPvP(mockEvent));
		assertTrue(CombatUtils.isPvP(projMockEvent));

		// attacker equals defender
		createAttack(false, defender);
		assertTrue(CombatUtils.isPvP(mockEvent));
		assertTrue(CombatUtils.isPvP(projMockEvent));
	}

	@Test
	final void testNoDamageHits() {
		final Projectile proj = mock(Projectile.class);
		when(proj.getShooter()).thenReturn(attacker);

		// ignore no damage hits
		assertTrue(Conf.IGNORE_NO_DMG_HITS.asBool());

		projMockEvent = createDamageEvent(proj, defender, false);
		when(projMockEvent.getDamage()).thenReturn(5.0);
		assertTrue(CombatUtils.isPvP(projMockEvent));

		projMockEvent = createDamageEvent(proj, defender, false);
		when(projMockEvent.getDamage()).thenReturn(0.0);
		assertFalse(CombatUtils.isPvP(projMockEvent));

		// don't ignore any hits
		Conf.IGNORE_NO_DMG_HITS.set(false);
		assertFalse(Conf.IGNORE_NO_DMG_HITS.asBool());

		projMockEvent = createDamageEvent(proj, defender, false);
		when(projMockEvent.getDamage()).thenReturn(5.0);
		assertTrue(CombatUtils.isPvP(projMockEvent));

		projMockEvent = createDamageEvent(proj, defender, false);
		when(projMockEvent.getDamage()).thenReturn(0.0);
		assertTrue(CombatUtils.isPvP(projMockEvent));
	}

	@Test
	final void cancelNewbie() {
		combatAttacker.setNewbie(true);
		createAttack(false);

		assertEquals(ProtectionType.NEWBIE, ph.checkProtection(attacker, defender).type());
		verify(attacker, times(2)).sendMessage(Lang.NEWBIE_PROTECTION_ON_HIT.msg());

		verify(mockEvent).setCancelled(true);
		verify(projMockEvent).setCancelled(true);
	}

	@Test
	final void cancelPvPDisabled() {
		combatDefender.setPvP(false);
		createAttack(false);

		assertEquals(ProtectionType.PVPDISABLED, ph.checkProtection(attacker, defender).type());
		verify(attacker, times(2)).sendMessage(Lang.ATTACK_DENIED_OTHER.msg(defender.getName()));

		verify(mockEvent).setCancelled(true);
		verify(projMockEvent).setCancelled(true);
	}

	@Test
	final void failCancel() {
		when(attacker.getAllowFlight()).thenReturn(true);
		when(defender.getAllowFlight()).thenReturn(true);
		when(attacker.isFlying()).thenReturn(true);
		when(defender.isFlying()).thenReturn(true);
		assertEquals(ProtectionType.FAIL, ph.checkProtection(attacker, defender).type());
		createAttack(false);
		assertTrue(combatAttacker.isInCombat());
		assertTrue(combatDefender.isInCombat());
		verify(attacker, times(2)).setFlying(false);
		verify(defender, times(2)).setFlying(false);

		verify(mockEvent, never()).setCancelled(true);
		verify(projMockEvent, never()).setCancelled(true);
	}

	@Test
	final void overrideCancel() {
		combatAttacker.toggleOverride();
		createAttack(false);

		assertEquals(ProtectionType.FAIL_OVERRIDE, ph.checkProtection(attacker, defender).type());
		assertTrue(combatAttacker.isInCombat());
		assertTrue(combatDefender.isInCombat());

		verify(mockEvent, times(1)).setCancelled(false); // only when creating the attack
		verify(projMockEvent, times(1)).setCancelled(false); // only when creating the attack
	}

	@Test
	final void overrideCancelled() {
		combatAttacker.toggleOverride();
		createAttack(true);

		assertEquals(ProtectionType.FAIL_OVERRIDE, ph.checkProtection(attacker, defender).type());
		assertTrue(combatAttacker.isInCombat());
		assertTrue(combatDefender.isInCombat());

		verify(mockEvent).setCancelled(false);
		verify(projMockEvent).setCancelled(false);
	}

	@Test
	final void respawnProtection() {
		combatDefender.setRespawnTime(System.currentTimeMillis());
		createAttack(false);

		assertEquals(ProtectionType.RESPAWN_PROTECTION, ph.checkProtection(attacker, defender).type());
		verify(mockEvent).setCancelled(true);
		verify(projMockEvent).setCancelled(true);
	}

	@Test
	final void respawnProtectionAttacker() {
		combatAttacker.setRespawnTime(System.currentTimeMillis());
		createAttack(false);

		assertEquals(ProtectionType.RESPAWN_PROTECTION, ph.checkProtection(attacker, defender).type());
		assertTrue(ph.checkProtection(attacker, defender).isAttacker()); // Attacker has protection
		verify(mockEvent).setCancelled(true);
		verify(projMockEvent).setCancelled(true);
	}

	@Test
	final void worldProtection() {
		// Set up world as non-combat world
		combatDefender.getCombatWorld().setCombatAllowed(false);
		createAttack(false);

		assertEquals(ProtectionType.WORLD_PROTECTION, ph.checkProtection(attacker, defender).type());
		verify(mockEvent).setCancelled(true);
		verify(projMockEvent).setCancelled(true);
	}

	@Test
	final void globalProtection() {
		ph.setGlobalStatus(false);
		createAttack(false);

		assertEquals(ProtectionType.GLOBAL_PROTECTION, ph.checkProtection(attacker, defender).type());
		verify(mockEvent).setCancelled(true);
		verify(projMockEvent).setCancelled(true);
	}

	@Test
	final void tntAttack() {
		final TNTPrimed tnt = mock(TNTPrimed.class);
		when(tnt.getSource()).thenReturn(attacker);

		final EntityDamageByEntityEvent tntEvent = createDamageEvent(tnt, defender, false);
		assertTrue(CombatUtils.isPvP(tntEvent));

		callEvent(tntEvent);
		// Verify combat tagging behavior for TNT attacks
		assertTrue(combatAttacker.isInCombat());
		assertTrue(combatDefender.isInCombat());
	}

	@Test
	final void areaEffectCloudAttack() {
		final AreaEffectCloud cloud = mock(AreaEffectCloud.class);
		when(cloud.getSource()).thenReturn(attacker);

		final EntityDamageByEntityEvent cloudEvent = createDamageEvent(cloud, defender, false);
		assertTrue(CombatUtils.isPvP(cloudEvent));

		callEvent(cloudEvent);
		// Verify combat tagging for area effect clouds
		assertTrue(combatAttacker.isInCombat());
		assertTrue(combatDefender.isInCombat());
	}

	@Test
	final void combatTagging() {
		assertFalse(combatAttacker.isInCombat());
		assertFalse(combatDefender.isInCombat());

		createAttack(false);

		assertTrue(combatAttacker.isInCombat());
		assertTrue(combatDefender.isInCombat());
	}

	@Test
	final void excludedWorld() {
		final String excludedWorldName = "excluded_world";

		// Configure world as excluded
		when(defender.getWorld().getName()).thenReturn(excludedWorldName);
		when(attacker.getWorld().getName()).thenReturn(excludedWorldName);

		// Mock CombatUtils.isWorldExcluded to return true for our test world
		try (var mockedCombatUtils = mockStatic(CombatUtils.class)) {
			mockedCombatUtils.when(() -> CombatUtils.isWorldExcluded(excludedWorldName)).thenReturn(true);

			mockEvent = createDamageEvent(attacker, defender, false);

			// Call the listener directly
			damageListener.onPlayerDamage(mockEvent);

			// Event should remain unchanged (not cancelled, not processed)
			assertFalse(mockEvent.isCancelled());
			// No combat tagging should occur
			assertFalse(combatAttacker.isInCombat());
			assertFalse(combatDefender.isInCombat());
		}
	}

	@Test
	final void multipleProtectionTypes() {
		// Test when player has multiple protections - respawn takes precedence
		combatDefender.setNewbie(true);
		combatDefender.setRespawnTime(System.currentTimeMillis());
		createAttack(false);

		assertEquals(ProtectionType.RESPAWN_PROTECTION, ph.checkProtection(attacker, defender).type());
		verify(mockEvent).setCancelled(true);
		verify(projMockEvent).setCancelled(true);
	}

	@Test
	final void vulnerableState() {
		// Test vulnerable state when both players are in combat and enemies
		combatAttacker.tag(true, combatDefender);
		combatDefender.tag(false, combatAttacker);

		// With vulnerable enabled and override, should allow attack
		combatAttacker.toggleOverride();
		createAttack(false);

		assertEquals(ProtectionType.FAIL_OVERRIDE, ph.checkProtection(attacker, defender).type());
		verify(mockEvent, never()).setCancelled(true);
		verify(projMockEvent, never()).setCancelled(true);
	}

	@Test
	final void preCancelledEvent() {
		// Test that pre-cancelled events are handled appropriately
		mockEvent = createDamageEvent(attacker, defender, true);
		projMockEvent = createDamageEvent(mock(Projectile.class), defender, true);

		// Pre-cancelled events should not trigger normal PvP logic
		callEvent(mockEvent);
		callEvent(projMockEvent);

		// Verify the events remain cancelled and no additional processing occurs
		assertTrue(mockEvent.isCancelled());
		assertTrue(projMockEvent.isCancelled());
	}

}
