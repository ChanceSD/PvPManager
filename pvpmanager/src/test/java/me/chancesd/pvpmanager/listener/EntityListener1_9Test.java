package me.chancesd.pvpmanager.listener;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import me.chancesd.pvpmanager.InstanceCreator;
import me.chancesd.pvpmanager.PluginSetup;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.utils.CombatUtils;

@ExtendWith(InstanceCreator.class)
class EntityListener1_9Test {

	private static PluginSetup pt;
	private static PlayerManager ph;
	private static EntityListener1_9 listener;

	@BeforeAll
	static void setupClass() {
		pt = InstanceCreator.getPt();
		final PvPManager plugin = pt.getPlugin();
		ph = plugin.getPlayerManager();
		listener = new EntityListener1_9(ph);
	}

	@BeforeEach
	void setup() {
		ph.getPlayers().clear();
		Conf.BLOCK_GLIDE_IN_COMBAT.set(true);
		Conf.ELYTRA_UNEQUIP_ON_GLIDE_SPAM.set(true);
	}

	@AfterEach
	void cleanup() {
		Conf.BLOCK_GLIDE_IN_COMBAT.set(false);
		Conf.ELYTRA_UNEQUIP_ON_GLIDE_SPAM.set(true);
	}

	private Player createTaggedPlayer() {
		final Player player = pt.createPlayer("GlideTest");
		final CombatPlayer combatPlayer = ph.createPlayer(player, true);
		final CombatPlayer enemy = ph.createPlayer(pt.createPlayer("GlideEnemy"), true);
		combatPlayer.tag(true, enemy);
		return player;
	}

	@Test
	void glideSpamUnequipsElytraOnlyWhenOptionEnabled() {
		final Player player = createTaggedPlayer();

		try (MockedStatic<CombatUtils> combatUtils = Mockito.mockStatic(CombatUtils.class)) {
			for (int attempt = 0; attempt < 3; attempt++) {
				final EntityToggleGlideEvent event = new EntityToggleGlideEvent(player, true);
				listener.onEntityGlide(event);
				assertTrue(event.isCancelled());
			}

			combatUtils.verify(() -> CombatUtils.removeElytra(player), times(1));

			Conf.ELYTRA_UNEQUIP_ON_GLIDE_SPAM.set(false);
			final Player playerWithOptionDisabled = createTaggedPlayer();
			for (int attempt = 0; attempt < 4; attempt++) {
				final EntityToggleGlideEvent event = new EntityToggleGlideEvent(playerWithOptionDisabled, true);
				listener.onEntityGlide(event);
				assertTrue(event.isCancelled());
			}

			combatUtils.verify(() -> CombatUtils.removeElytra(playerWithOptionDisabled), never());
		}
	}
}
