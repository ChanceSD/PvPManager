package me.NoChance.PvPManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import me.NoChance.PvPManager.Listeners.PlayerListener;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Settings;

public class PlayerListenerTest {

	private static PlayerListener listener;
	private static PlayerHandler ph;
	private PlayerDeathEvent event;
	private static Player attacker;
	private static Player defender;

	@BeforeClass
	public static void setupClass() {
		final PluginTest pt = AllTests.getPt();
		final PvPManager plugin = pt.getPlugin();
		ph = plugin.getPlayerHandler();
		listener = new PlayerListener(plugin.getPlayerHandler());
		attacker = pt.getAttacker();
		defender = pt.getDefender();
	}

	@Before
	public final void setup() {
		event = new PlayerDeathEvent(defender, null, 0, null);
		ph.getPlayers().clear();
	}

	@Test
	public void onPlayerJoinTest() {
		assertEquals(0, ph.getPlayers().size());
		listener.onPlayerJoin(new PlayerJoinEvent(attacker, ""));
		assertEquals(1, ph.getPlayers().size());
		assertEquals(attacker, ph.getPlayers().values().stream().findFirst().get().getPlayer());
	}

	@Test
	public void onPlayerLogoutTest() {
		final PvPlayer pvPlayer = ph.get(defender);
		pvPlayer.setTagged(true, ph.get(attacker));
		assertTrue(pvPlayer.isInCombat());

		listener.onPlayerLogout(new PlayerQuitEvent(defender, ""));
		verify(defender, times(1)).setHealth(0);
		assertFalse(pvPlayer.isInCombat());

		assertEquals(1, ph.getPlayers().size());
		listener.onPlayerLogout(new PlayerQuitEvent(attacker, ""));
		assertEquals(0, ph.getPlayers().size());
	}

	@Test
	public final void regularDeath() {
		final PvPlayer pDefender = ph.get(defender);
		assertFalse(pDefender.isInCombat());
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

		Settings.setUntagEnemy(true);
		pAttacker.setTagged(true, pDefender);
		pDefender.setTagged(false, pAttacker);
		assertTrue(pAttacker.isInCombat());
		assertTrue(pDefender.isInCombat());
		listener.onPlayerDeath(event);
		assertFalse(pDefender.isInCombat());
		assertFalse(pAttacker.isInCombat());
	}

}
