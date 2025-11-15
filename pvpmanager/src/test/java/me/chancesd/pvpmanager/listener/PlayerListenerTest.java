package me.chancesd.pvpmanager.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
import me.chancesd.pvpmanager.setting.Conf;

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
