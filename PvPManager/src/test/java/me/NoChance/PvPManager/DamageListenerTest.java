package me.NoChance.PvPManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import me.NoChance.PvPManager.Listeners.EntityListener;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Player.CancelResult;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;

public class DamageListenerTest {

	private static EntityListener damageListener;
	private EntityDamageByEntityEvent mockEvent;
	private EntityDamageByEntityEvent projMockEvent;
	private static PlayerHandler ph;
	private static Player attacker;
	private static Player defender;

	@BeforeClass
	public static void setupClass() {
		final PluginTest pt = AllTests.getPt();
		final PvPManager plugin = pt.getPlugin();
		ph = plugin.getPlayerHandler();
		damageListener = new EntityListener(ph);
		Settings.setPvpBlood(false); // avoid loading Material class while testing
		attacker = pt.getAttacker();
		defender = pt.getDefender();
	}

	@Before
	public final void setup() {
		ph.getPlayers().clear();
		assertTrue(ph.getPlayers().size() == 0);
	}

	private void createAttack(final boolean cancelled) {
		mockEvent = spy(new EntityDamageByEntityEvent(attacker, defender, DamageCause.ENTITY_ATTACK, 5));
		mockEvent.setCancelled(cancelled);

		final Projectile proj = mock(Projectile.class);
		when(proj.getShooter()).thenReturn(attacker);
		projMockEvent = spy(new EntityDamageByEntityEvent(proj, defender, DamageCause.PROJECTILE, 5));
		projMockEvent.setCancelled(cancelled);

		callEvent(mockEvent);
		callEvent(projMockEvent);
	}

	private void createMobAttack(final boolean mobAttacker, final boolean cancelled) {
		final Zombie zombie = mock(Zombie.class, RETURNS_MOCKS);
		if (mobAttacker) {
			mockEvent = spy(new EntityDamageByEntityEvent(zombie, defender, DamageCause.ENTITY_ATTACK, 5));
		} else {
			mockEvent = spy(new EntityDamageByEntityEvent(attacker, zombie, DamageCause.ENTITY_ATTACK, 5));
		}
		mockEvent.setCancelled(cancelled);

		final Projectile proj = mock(Projectile.class);
		when(proj.getShooter()).thenReturn(attacker);
		if (mobAttacker) {
			projMockEvent = spy(new EntityDamageByEntityEvent(proj, defender, DamageCause.PROJECTILE, 5));
		} else {
			projMockEvent = spy(new EntityDamageByEntityEvent(proj, zombie, DamageCause.PROJECTILE, 5));
		}
		projMockEvent.setCancelled(cancelled);

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
	public final void testMobAttack() {
		createMobAttack(false, false);
		assertFalse(mockEvent.isCancelled());
		assertFalse(projMockEvent.isCancelled());

		createMobAttack(true, false);
		assertFalse(mockEvent.isCancelled());
		assertFalse(projMockEvent.isCancelled());
	}

	@Test
	public final void testSelfTag() {
		final Projectile proj = mock(Projectile.class);
		projMockEvent = spy(new EntityDamageByEntityEvent(proj, defender, DamageCause.PROJECTILE, 5));
		projMockEvent.setCancelled(false);

		assertFalse(Settings.isSelfTag());
		// attacker different from defender
		when(proj.getShooter()).thenReturn(attacker);
		assertNotEquals(proj.getShooter(), projMockEvent.getEntity());
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
		assertNotEquals(proj.getShooter(), projMockEvent.getEntity());
		assertTrue(CombatUtils.isPvP(projMockEvent));

		// attacker equals defender
		when(proj.getShooter()).thenReturn(defender);
		assertEquals(proj.getShooter(), projMockEvent.getEntity());
		assertTrue(CombatUtils.isPvP(projMockEvent));
	}

	@Test
	public final void testNoDamageHits() {
		final Projectile proj = mock(Projectile.class);
		when(proj.getShooter()).thenReturn(attacker);

		// don't ignore any hits
		assertFalse(Settings.isIgnoreNoDamageHits());

		projMockEvent = spy(new EntityDamageByEntityEvent(proj, defender, DamageCause.PROJECTILE, 5));
		assertTrue(CombatUtils.isPvP(projMockEvent));

		projMockEvent = spy(new EntityDamageByEntityEvent(proj, defender, DamageCause.PROJECTILE, 0));
		assertTrue(CombatUtils.isPvP(projMockEvent));

		// ignore no damage hits
		Settings.setIgnoreNoDamageHits(true);
		assertTrue(Settings.isIgnoreNoDamageHits());

		projMockEvent = spy(new EntityDamageByEntityEvent(proj, defender, DamageCause.PROJECTILE, 5));
		assertTrue(CombatUtils.isPvP(projMockEvent));

		projMockEvent = spy(new EntityDamageByEntityEvent(proj, defender, DamageCause.PROJECTILE, 0));
		assertFalse(CombatUtils.isPvP(projMockEvent));
	}

	@Test
	public final void cancelNewbie() {
		ph.get(attacker).setNewbie(true);
		createAttack(false);

		assertEquals(CancelResult.NEWBIE, ph.tryCancel(attacker, defender));
		verify(attacker, times(2)).sendMessage(Messages.newbieBlocked());

		verify(mockEvent).setCancelled(true);
		verify(projMockEvent).setCancelled(true);
	}

	@Test
	public final void cancelPvPDisabled() {
		ph.get(defender).setPvP(false);
		createAttack(false);

		assertEquals(CancelResult.PVPDISABLED, ph.tryCancel(attacker, defender));
		verify(attacker, times(2)).sendMessage(Messages.pvpDisabledOther(defender.getName()));

		verify(mockEvent).setCancelled(true);
		verify(projMockEvent).setCancelled(true);
	}

	@Test
	public final void failCancel() {
		ph.get(defender).setPvP(true);
		ph.get(attacker).setPvP(true);

		when(attacker.isFlying()).thenReturn(true);
		when(defender.isFlying()).thenReturn(true);
		assertEquals(CancelResult.FAIL, ph.tryCancel(attacker, defender));
		createAttack(false);
		assertTrue(ph.get(attacker).isInCombat());
		assertTrue(ph.get(defender).isInCombat());
		verify(attacker, times(2)).setFlying(false);
		verify(defender, times(2)).setFlying(false);

		verify(mockEvent, never()).setCancelled(true);
		verify(projMockEvent, never()).setCancelled(true);
	}

	@Test
	public final void overrideCancel() {
		ph.get(attacker).toggleOverride();
		createAttack(false);

		assertEquals(CancelResult.FAIL_OVERRIDE, ph.tryCancel(attacker, defender));
		assertTrue(ph.get(attacker).isInCombat());
		assertTrue(ph.get(defender).isInCombat());

		verify(mockEvent, times(1)).setCancelled(false); // only when creating the attack
		verify(projMockEvent, times(1)).setCancelled(false); // only when creating the attack
	}

	@Test
	public final void overrideCancelled() {
		ph.get(attacker).toggleOverride();
		createAttack(true);

		assertEquals(CancelResult.FAIL_OVERRIDE, ph.tryCancel(attacker, defender));
		assertTrue(ph.get(attacker).isInCombat());
		assertTrue(ph.get(defender).isInCombat());

		verify(mockEvent).setCancelled(false);
		verify(projMockEvent).setCancelled(false);
	}

}
