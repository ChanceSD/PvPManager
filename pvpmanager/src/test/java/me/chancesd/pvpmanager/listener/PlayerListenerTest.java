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
import me.chancesd.pvpmanager.setting.Conf;

@ExtendWith(InstanceCreator.class)
public class PlayerListenerTest {

	private static PlayerListener listener;
	private static PlayerManager ph;
	private static PluginSetup pt;

	@BeforeAll
	public static void setupClass() {
		pt = InstanceCreator.getPt();
		final PvPManager plugin = pt.getPlugin();
		ph = plugin.getPlayerManager();
		listener = new PlayerListener(plugin.getPlayerManager());
	}

	@BeforeEach
	public final void setup() {
		ph.getPlayers().clear();
	}

	private void tagPlayer(final CombatPlayer player, final CombatPlayer enemy) {
		player.tag(true, enemy);
		assertTrue(player.isInCombat());
	}

	private void tagPlayer(final CombatPlayer player) {
		tagPlayer(player, ph.get(pt.createPlayer("Attacker")));
	}

	@Test
	void onPlayerJoinTest() {
		final Player playerJoined = pt.createPlayer("onPlayerJoinTest");
		assertEquals(0, ph.getPlayers().size());
		listener.onPlayerJoin(new PlayerJoinEvent(playerJoined, ""));
		assertEquals(1, ph.getPlayers().size());
		assertEquals(playerJoined, ph.getPlayers().values().stream().findFirst().get().getPlayer());
	}

	@Test
	void onPlayerLogoutTest() {
		final Player player = pt.createPlayer("onPlayerLogoutTest");
		final CombatPlayer pvPlayer = ph.get(player);
		final Player attacker = pt.createPlayer("Attacker");
		final CombatPlayer pvpAttacker = ph.get(attacker);
		tagPlayer(pvPlayer, pvpAttacker);

		listener.onPlayerLogout(new PlayerQuitEvent(player, ""));
		listener.onPlayerLogoutMonitor(new PlayerQuitEvent(player, ""));
		verify(player, times(1)).setHealth(0);

		assertEquals(1, ph.getPlayers().size());
		listener.onPlayerLogoutMonitor(new PlayerQuitEvent(attacker, ""));
		assertEquals(0, ph.getPlayers().size());
	}

	@Test
	void onPlayerKickTest() {
		final Player kickPlayer = pt.createPlayer("onPlayerKickTest");
		final CombatPlayer pvPlayer = ph.get(kickPlayer);

		tagPlayer(pvPlayer);
		listener.onPlayerKick(new PlayerKickEvent(kickPlayer, "", ""));
		assertTrue(pvPlayer.isInCombat());

		Conf.MATCH_KICK_REASON.set(true);
		tagPlayer(pvPlayer);
		listener.onPlayerKick(new PlayerKickEvent(kickPlayer, "", ""));
		assertFalse(pvPlayer.isInCombat());

		tagPlayer(pvPlayer);
		listener.onPlayerKick(new PlayerKickEvent(kickPlayer, "Kicked for spamming", ""));
		assertTrue(pvPlayer.isInCombat());
	}

	private PlayerDeathEvent createDeathEvent(final Player player) {
		final PlayerDeathEvent event = mock(PlayerDeathEvent.class);
		when(event.getEntity()).thenReturn(player);
		return event;
	}

	@Test
	final void regularDeath() {
		final Player player = pt.createPlayer("regularDeath");
		final CombatPlayer pDefender = ph.get(player);
		assertFalse(pDefender.isInCombat());
		listener.onPlayerDeath(createDeathEvent(player));
	}

	@Test
	final void inCombatDeath() {
		final Player attacker = pt.createPlayer("Attacker");
		final CombatPlayer pAttacker = ph.get(attacker);
		final Player defender = pt.createPlayer("Defender", attacker);
		final CombatPlayer pDefender = ph.get(defender);

		tagPlayer(pDefender, pAttacker);
		tagPlayer(pAttacker, pDefender);
		listener.onPlayerDeath(createDeathEvent(defender));
		assertFalse(pDefender.isInCombat());
		assertTrue(pAttacker.isInCombat());

		Conf.UNTAG_ON_KILL.set(true);
		tagPlayer(pDefender, pAttacker);
		tagPlayer(pAttacker, pDefender);
		listener.onPlayerDeath(createDeathEvent(defender));
		assertFalse(pDefender.isInCombat());
		assertFalse(pAttacker.isInCombat());

		Conf.SELF_TAG.set(true);
		tagPlayer(pAttacker, pAttacker);
		tagPlayer(pDefender, pAttacker);
		tagPlayer(pAttacker, pDefender);
		listener.onPlayerDeath(createDeathEvent(defender));
		assertFalse(pDefender.isInCombat());
		assertFalse(pAttacker.isInCombat());
	}

}
