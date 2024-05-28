package me.NoChance.PvPManager.Listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import me.NoChance.PvPManager.InstanceCreator;
import me.NoChance.PvPManager.PluginTest;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;

@ExtendWith(InstanceCreator.class)
public class PlayerListenerTest {

	private static PlayerListener listener;
	private static PlayerHandler ph;
	private static PluginTest pt;

	@BeforeAll
	public static void setupClass() {
		pt = InstanceCreator.getPt();
		final PvPManager plugin = pt.getPlugin();
		ph = plugin.getPlayerHandler();
		listener = new PlayerListener(plugin.getPlayerHandler());
	}

	@BeforeEach
	public final void setup() {
		ph.getPlayers().clear();
	}

	private void tagPlayer(final PvPlayer player, final PvPlayer enemy) {
		player.setTagged(true, enemy);
		assertTrue(player.isInCombat());
	}

	private void tagPlayer(final PvPlayer player) {
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
		final PvPlayer pvPlayer = ph.get(player);
		final Player attacker = pt.createPlayer("Attacker");
		final PvPlayer pvpAttacker = ph.get(attacker);
		tagPlayer(pvPlayer, pvpAttacker);

		listener.onPlayerLogout(new PlayerQuitEvent(player, ""));
		listener.onPlayerLogoutMonitor(new PlayerQuitEvent(player, ""));
		verify(player, times(1)).setHealth(0);
		assertFalse(pvPlayer.isInCombat());

		assertEquals(1, ph.getPlayers().size()); // attacker and defender
		listener.onPlayerLogoutMonitor(new PlayerQuitEvent(attacker, ""));
		assertEquals(0, ph.getPlayers().size());
	}

	@Test
	void onPlayerKickTest() {
		final Player kickPlayer = pt.createPlayer("onPlayerKickTest");
		final PvPlayer pvPlayer = ph.get(kickPlayer);

		tagPlayer(pvPlayer);
		listener.onPlayerKick(new PlayerKickEvent(kickPlayer, "", ""));
		assertTrue(pvPlayer.isInCombat());

		Settings.setMatchKickReason(true);
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
		final PvPlayer pDefender = ph.get(player);
		assertFalse(pDefender.isInCombat());
		listener.onPlayerDeath(createDeathEvent(player));
	}

	@Test
	final void inCombatDeath() {
		final Player attacker = pt.createPlayer("Attacker");
		final PvPlayer pAttacker = ph.get(attacker);
		final Player defender = pt.createPlayer("Defender", attacker);
		final PvPlayer pDefender = ph.get(defender);

		tagPlayer(pDefender, pAttacker);
		tagPlayer(pAttacker, pDefender);
		listener.onPlayerDeath(createDeathEvent(defender));
		assertFalse(pDefender.isInCombat());
		assertTrue(pAttacker.isInCombat());

		Settings.setUntagEnemy(true);
		tagPlayer(pDefender, pAttacker);
		tagPlayer(pAttacker, pDefender);
		listener.onPlayerDeath(createDeathEvent(defender));
		assertFalse(pDefender.isInCombat());
		assertFalse(pAttacker.isInCombat());
	}

	@Test
	final void onCommandTest() {
		final Player player = pt.createPlayer("onCommandTest");
		final PvPlayer pvPlayer = ph.get(player);
		final PlayerCommandPreprocessEvent commandPreprocessEvent = new PlayerCommandPreprocessEvent(player, "/spawn");

		assertFalse(commandPreprocessEvent.isCancelled());
		listener.onCommand(commandPreprocessEvent);
		assertFalse(commandPreprocessEvent.isCancelled());

		tagPlayer(pvPlayer);
		listener.onCommand(commandPreprocessEvent);
		assertTrue(commandPreprocessEvent.isCancelled());
		verify(player, atMostOnce()).sendMessage(Messages.getCommandDeniedIncombat());

		final PlayerCommandPreprocessEvent commandPreprocessEvent2 = new PlayerCommandPreprocessEvent(player, "/tell");
		tagPlayer(pvPlayer);
		listener.onCommand(commandPreprocessEvent2);
		assertFalse(commandPreprocessEvent2.isCancelled());
	}

}
