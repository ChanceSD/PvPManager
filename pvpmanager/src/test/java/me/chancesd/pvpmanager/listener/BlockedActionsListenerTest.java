package me.chancesd.pvpmanager.listener;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import me.chancesd.pvpmanager.InstanceCreator;
import me.chancesd.pvpmanager.PluginSetup;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Lang;

@ExtendWith(InstanceCreator.class)
public class BlockedActionsListenerTest {

	private static BlockedActionsListener listener;
	private static PlayerManager ph;
	private static PluginSetup pt;

	@BeforeAll
	public static void setupClass() {
		pt = InstanceCreator.getPt();
		final PvPManager plugin = pt.getPlugin();
		ph = plugin.getPlayerManager();
		listener = new BlockedActionsListener(plugin.getPlayerManager());
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
	final void onCommandTest() {
		final Player player = pt.createPlayer("onCommandTest");
		final CombatPlayer pvPlayer = ph.get(player);
		final PlayerCommandPreprocessEvent commandPreprocessEvent = new PlayerCommandPreprocessEvent(player, "/spawn");

		assertFalse(commandPreprocessEvent.isCancelled());
		listener.onCommand(commandPreprocessEvent);
		assertFalse(commandPreprocessEvent.isCancelled());

		tagPlayer(pvPlayer);
		listener.onCommand(commandPreprocessEvent);
		assertTrue(commandPreprocessEvent.isCancelled());
		verify(player, atMostOnce()).sendMessage(Lang.COMMAND_DENIED_INCOMBAT.msg());

		final PlayerCommandPreprocessEvent commandPreprocessEvent2 = new PlayerCommandPreprocessEvent(player, "/tell");
		tagPlayer(pvPlayer);
		listener.onCommand(commandPreprocessEvent2);
		assertFalse(commandPreprocessEvent2.isCancelled());
	}

}
