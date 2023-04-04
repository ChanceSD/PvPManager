package me.NoChance.PvPManager.Listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
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
import me.NoChance.PvPManager.Settings.Settings;

@ExtendWith(InstanceCreator.class)
public class PlayerListenerTest {

	private static PlayerListener listener;
	private static PlayerHandler ph;
	private PlayerDeathEvent event;
	private static Player attacker;
	private static Player defender;
	private static PluginTest pt;

	@BeforeAll
	public static void setupClass() {
		pt = InstanceCreator.getPt();
		final PvPManager plugin = pt.getPlugin();
		ph = plugin.getPlayerHandler();
		listener = new PlayerListener(plugin.getPlayerHandler());
		attacker = pt.getAttacker();
		defender = pt.getDefender();
	}

	@BeforeEach
	public final void setup() {
		event = new PlayerDeathEvent(defender, null, 0, "");
		ph.getPlayers().clear();
	}

	private void tagPlayer(final PvPlayer player, final PvPlayer enemy) {
		player.setTagged(true, enemy);
		assertTrue(player.isInCombat());
	}

	private void tagPlayer(final PvPlayer player) {
		tagPlayer(player, ph.get(attacker));
	}

	@Test
	void onPlayerJoinTest() {
		assertEquals(0, ph.getPlayers().size());
		listener.onPlayerJoin(new PlayerJoinEvent(attacker, ""));
		assertEquals(1, ph.getPlayers().size());
		assertEquals(attacker, ph.getPlayers().values().stream().findFirst().get().getPlayer());
	}

	@Test
	void onPlayerLogoutTest() {
		final PvPlayer pvPlayer = ph.get(defender);
		tagPlayer(pvPlayer);

		listener.onPlayerLogout(new PlayerQuitEvent(defender, ""));
		verify(defender, times(1)).setHealth(0);
		assertFalse(pvPlayer.isInCombat());

		assertEquals(1, ph.getPlayers().size());
		listener.onPlayerLogout(new PlayerQuitEvent(attacker, ""));
		assertEquals(0, ph.getPlayers().size());
	}

	@Test
	void onPlayerKickTest() {
		final Player kickPlayer = pt.createPlayer("PlayerKickTest");
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

	@Test
	final void regularDeath() {
		final PvPlayer pDefender = ph.get(defender);
		assertFalse(pDefender.isInCombat());
		listener.onPlayerDeath(event);
	}

	@Test
	final void inCombatDeath() {
		final PvPlayer pAttacker = ph.get(attacker);
		final PvPlayer pDefender = ph.get(defender);

		tagPlayer(pDefender);
		listener.onPlayerDeath(event);
		assertFalse(pDefender.isInCombat());

		Settings.setUntagEnemy(true);
		tagPlayer(pDefender, pAttacker);
		tagPlayer(pAttacker, pDefender);
		listener.onPlayerDeath(event);
		assertFalse(pDefender.isInCombat());
		assertFalse(pAttacker.isInCombat());
	}

}
