package me.NoChance.PvPManager.Listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import me.NoChance.PvPManager.InstanceCreator;
import me.NoChance.PvPManager.PluginTest;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Player.ProtectionResult;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;

@ExtendWith(InstanceCreator.class)
public class EntityListenerTest {

	private static final PluginTest PT = InstanceCreator.getPt();
	private static EntityListener damageListener;
	private EntityDamageByEntityEvent mockEvent;
	private EntityDamageByEntityEvent projMockEvent;
	private static PlayerHandler ph;
	private static Player attacker;
	private static Player defender;

	@BeforeAll
	public static void setupClass() {
		final PvPManager plugin = PT.getPlugin();
		ph = plugin.getPlayerHandler();
		damageListener = new EntityListener(ph);
		Settings.setPvpBlood(false); // avoid loading Material class while testing
		attacker = PT.getAttacker();
		defender = PT.getDefender();
	}

	@BeforeEach
	public final void setup() {
		ph.getPlayers().clear();
	}

	private void createAttack(final boolean cancelled) {
		mockEvent = createDamageEvent(attacker, defender, cancelled);

		final Projectile proj = mock(Projectile.class);
		when(proj.getShooter()).thenReturn(attacker);
		projMockEvent = createDamageEvent(proj, defender, cancelled);

		callEvent(mockEvent);
		callEvent(projMockEvent);
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

		assertFalse(Settings.isSelfTag());
		// attacker different from defender
		when(proj.getShooter()).thenReturn(attacker);
		assertEquals(proj.getShooter(), attacker);
		assertTrue(CombatUtils.isPvP(projMockEvent));

		// attacker equals defender
		when(proj.getShooter()).thenReturn(defender);
		assertEquals(proj.getShooter(), projMockEvent.getEntity());
		assertFalse(CombatUtils.isPvP(projMockEvent));

		// now allow self tagging
		Settings.setSelfTag(true);
		assertTrue(Settings.isSelfTag());
		// attacker different from defender
		when(proj.getShooter()).thenReturn(attacker);
		assertEquals(proj.getShooter(), attacker);
		assertTrue(CombatUtils.isPvP(projMockEvent));

		// attacker equals defender
		when(proj.getShooter()).thenReturn(defender);
		assertEquals(proj.getShooter(), projMockEvent.getEntity());
		assertTrue(CombatUtils.isPvP(projMockEvent));
	}

	@Test
	final void testNoDamageHits() {
		final Projectile proj = mock(Projectile.class);
		when(proj.getShooter()).thenReturn(attacker);

		// ignore no damage hits
		assertTrue(Settings.isIgnoreNoDamageHits());

		projMockEvent = createDamageEvent(proj, defender, false);
		when(projMockEvent.getDamage()).thenReturn(5.0);
		assertTrue(CombatUtils.isPvP(projMockEvent));

		projMockEvent = createDamageEvent(proj, defender, false);
		when(projMockEvent.getDamage()).thenReturn(0.0);
		assertFalse(CombatUtils.isPvP(projMockEvent));

		// don't ignore any hits
		Settings.setIgnoreNoDamageHits(false);
		assertFalse(Settings.isIgnoreNoDamageHits());

		projMockEvent = createDamageEvent(proj, defender, false);
		when(projMockEvent.getDamage()).thenReturn(5.0);
		assertTrue(CombatUtils.isPvP(projMockEvent));

		projMockEvent = createDamageEvent(proj, defender, false);
		when(projMockEvent.getDamage()).thenReturn(0.0);
		assertTrue(CombatUtils.isPvP(projMockEvent));
	}

	@Test
	final void cancelNewbie() {
		ph.get(attacker).setNewbie(true);
		createAttack(false);

		assertEquals(ProtectionResult.NEWBIE, ph.tryCancel(attacker, defender));
		verify(attacker, times(2)).sendMessage(Messages.newbieBlocked());

		verify(mockEvent).setCancelled(true);
		verify(projMockEvent).setCancelled(true);
	}

	@Test
	final void cancelPvPDisabled() {
		ph.get(defender).setPvP(false);
		createAttack(false);

		assertEquals(ProtectionResult.PVPDISABLED, ph.tryCancel(attacker, defender));
		verify(attacker, times(2)).sendMessage(Messages.pvpDisabledOther(defender.getName()));

		verify(mockEvent).setCancelled(true);
		verify(projMockEvent).setCancelled(true);
	}

	@Test
	final void failCancel() {
		ph.get(defender).setPvP(true);
		ph.get(attacker).setPvP(true);

		when(attacker.isFlying()).thenReturn(true);
		when(defender.isFlying()).thenReturn(true);
		assertEquals(ProtectionResult.FAIL, ph.tryCancel(attacker, defender));
		createAttack(false);
		assertTrue(ph.get(attacker).isInCombat());
		assertTrue(ph.get(defender).isInCombat());
		verify(attacker, times(2)).setFlying(false);
		verify(defender, times(2)).setFlying(false);

		verify(mockEvent, never()).setCancelled(true);
		verify(projMockEvent, never()).setCancelled(true);
	}

	@Test
	final void overrideCancel() {
		ph.get(attacker).toggleOverride();
		createAttack(false);

		assertEquals(ProtectionResult.FAIL_OVERRIDE, ph.tryCancel(attacker, defender));
		assertTrue(ph.get(attacker).isInCombat());
		assertTrue(ph.get(defender).isInCombat());

		verify(mockEvent, times(1)).setCancelled(false); // only when creating the attack
		verify(projMockEvent, times(1)).setCancelled(false); // only when creating the attack
	}

	@Test
	final void overrideCancelled() {
		ph.get(attacker).toggleOverride();
		createAttack(true);

		assertEquals(ProtectionResult.FAIL_OVERRIDE, ph.tryCancel(attacker, defender));
		assertTrue(ph.get(attacker).isInCombat());
		assertTrue(ph.get(defender).isInCombat());

		verify(mockEvent).setCancelled(false);
		verify(projMockEvent).setCancelled(false);
	}

}
