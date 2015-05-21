package me.NoChance.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Listeners.PlayerListener;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Utils.CancelResult;
import me.NoChance.PvPManager.Utils.CombatUtils;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PvPManager.class, CombatUtils.class })
public class DamageListenerTest {

	private static PluginTest pt;
	private static PvPManager plugin;
	private static PlayerListener damageListener;
	private static EntityDamageByEntityEvent mockEvent;
	private static EntityDamageByEntityEvent projMockEvent;
	private static PlayerHandler ph;
	@Mock
	private Player attacker;
	@Mock(answer = Answers.RETURNS_MOCKS)
	private Player defender;

	@BeforeClass
	public static void setupClass() throws Exception {
		pt = PluginTest.getInstance();
		plugin = pt.getPlugin();
		ph = pt.getPh();
		PowerMockito.mockStatic(CombatUtils.class);
		when(CombatUtils.isWorldAllowed(anyString())).thenReturn(true);
		when(CombatUtils.isPvP((EntityDamageByEntityEvent) Mockito.anyObject())).thenCallRealMethod();
		damageListener = new PlayerListener(plugin);
		final Field playerHandlerfield = PlayerListener.class.getDeclaredField("ph");
		playerHandlerfield.setAccessible(true);
		playerHandlerfield.set(damageListener, ph);
	}

	@SuppressWarnings("deprecation")
	public final void createAttack(final CancelResult cr, final boolean cancelled) {
		MockitoAnnotations.initMocks(this);
		when(ph.tryCancel(attacker, defender)).thenReturn(cr);

		mockEvent = spy(new EntityDamageByEntityEvent(attacker, defender, DamageCause.ENTITY_ATTACK, 5));
		when(mockEvent.isCancelled()).thenReturn(cancelled);

		Projectile proj = mock(Projectile.class);
		when(proj.getShooter()).thenReturn(attacker);
		projMockEvent = spy(new EntityDamageByEntityEvent(proj, defender, DamageCause.PROJECTILE, 5));
		when(projMockEvent.isCancelled()).thenReturn(cancelled);

		damageListener.onPlayerDamage(mockEvent);
		damageListener.onPlayerDamageOverride(mockEvent);
		damageListener.onPlayerDamageMonitor(mockEvent);
		damageListener.onPlayerDamage(projMockEvent);
		damageListener.onPlayerDamageOverride(projMockEvent);
		damageListener.onPlayerDamageMonitor(projMockEvent);
	}

	@Test
	public final void cancelNewbie() {
		final CancelResult cr = CancelResult.NEWBIE;
		createAttack(cr, false);

		assertEquals(cr, ph.tryCancel(attacker, defender));
		verify(attacker, times(2)).sendMessage(Messages.getNewbieProtectionOnHit());

		verify(mockEvent).setCancelled(true);
		verify(projMockEvent).setCancelled(true);
	}

	@Test
	public final void cancelPvPDisabled() {
		final CancelResult cr = CancelResult.PVPDISABLED;
		createAttack(cr, false);

		assertEquals(cr, ph.tryCancel(attacker, defender));
		verify(attacker, times(2)).sendMessage(Messages.getAttackDeniedYou());

		verify(mockEvent).setCancelled(true);
		verify(projMockEvent).setCancelled(true);
	}

	@Test
	public final void failCancel() {
		final CancelResult cr = CancelResult.FAIL;
		createAttack(cr, false);

		assertEquals(cr, ph.tryCancel(attacker, defender));

		verify(mockEvent, never()).setCancelled(true);
		verify(projMockEvent, never()).setCancelled(true);
	}

	@Test
	public final void overrideCancel() {
		final CancelResult cr = CancelResult.FAIL_OVERRIDE;
		createAttack(cr, false);

		assertEquals(cr, ph.tryCancel(attacker, defender));
		verify(attacker, never()).sendMessage(anyString());

		verify(mockEvent, never()).setCancelled(false);
		verify(projMockEvent, never()).setCancelled(false);
	}

	@Test
	public final void overrideCancelled() {
		final CancelResult cr = CancelResult.FAIL_OVERRIDE;
		createAttack(cr, true);

		assertEquals(cr, ph.tryCancel(attacker, defender));
		verify(attacker, never()).sendMessage(anyString());

		verify(mockEvent).setCancelled(false);
		verify(projMockEvent).setCancelled(false);
	}

}
