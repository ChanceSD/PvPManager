package me.NoChance.PvPManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import me.NoChance.PvPManager.Listeners.PlayerListener;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Settings;

public class OnDeathTest {

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
