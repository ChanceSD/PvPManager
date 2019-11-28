package me.NoChance.PvPManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;

import me.NoChance.PvPManager.Listeners.PlayerListener;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Utils.CombatUtils;

public class OnDeathTest {

	private static PlayerListener listener;
	private static PlayerHandler ph;
	private PlayerDeathEvent event;
	@Mock(answer = Answers.RETURNS_MOCKS)
	private Player attacker;
	@Mock(answer = Answers.RETURNS_MOCKS)
	private Player defender;

	@BeforeClass
	public static void setupClass() {
		final PluginTest pt = AllTests.getPt();
		final PvPManager plugin = pt.getPlugin();
		ph = plugin.getPlayerHandler();
		PowerMockito.mockStatic(CombatUtils.class);
		when(CombatUtils.isWorldAllowed(anyString())).thenReturn(true);
		when(CombatUtils.isPvP((EntityDamageByEntityEvent) Matchers.anyObject())).thenCallRealMethod();
		listener = new PlayerListener(plugin.getPlayerHandler());
	}

	@Before
	public final void setup() {
		MockitoAnnotations.initMocks(this);
		when(attacker.hasMetadata(Matchers.anyString())).thenReturn(false);
		when(defender.hasMetadata(Matchers.anyString())).thenReturn(false);
		when(attacker.getUniqueId()).thenReturn(UUID.randomUUID());
		when(defender.getUniqueId()).thenReturn(UUID.randomUUID());
		when(defender.getKiller()).thenReturn(attacker);
		event = new PlayerDeathEvent(defender, null, 0, null);
	}

	@Test
	public final void regularDeath() {
		listener.onPlayerDeath(event);
	}

	@Test
	public final void inCombatDeath() {
		final PvPlayer pAttacker = ph.get(attacker);
		final PvPlayer pDefender = ph.get(defender);

		pDefender.setTagged(false, pAttacker);
		assertTrue(pDefender.isInCombat());
		listener.onPlayerDeath(event);
		assertFalse(pDefender.isInCombat());

		pAttacker.setTagged(true, pDefender);
		pDefender.setTagged(false, pAttacker);
		assertTrue(pAttacker.isInCombat());
		assertTrue(pDefender.isInCombat());
		listener.onPlayerDeath(event);
		assertFalse(pDefender.isInCombat());
		assertFalse(pAttacker.isInCombat());
	}

}
