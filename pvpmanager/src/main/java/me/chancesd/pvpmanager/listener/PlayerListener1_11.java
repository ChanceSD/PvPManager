package me.chancesd.pvpmanager.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Conf;

public class PlayerListener1_11 implements Listener {

	private final PlayerManager playerManager;

	public PlayerListener1_11(final PlayerManager ph) {
		this.playerManager = ph;
	}

	@EventHandler
	public void onEntityRessurrect(final EntityResurrectEvent event) {
		if (!Conf.BLOCK_TOTEM_UNDYING.asBool() || event.isCancelled() || event.getEntityType() != EntityType.PLAYER)
			return;

		final Player player = (Player) event.getEntity();
		final CombatPlayer pvPlayer = playerManager.get(player);
		if (pvPlayer.isInCombat()) {
			event.setCancelled(true);
			pvPlayer.message(Lang.TOTEM_BLOCKED_IN_COMBAT);
		}

	}

}
