package me.NoChance.PvPManager.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;

import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Settings;

public class EntityListener1_9 implements Listener {

	private final PlayerHandler ph;

	public EntityListener1_9(final PlayerHandler ph) {
		this.ph = ph;
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityGlide(final EntityToggleGlideEvent event) {
		if (!Settings.isBlockGlide() || !event.isGliding())
			return;
		if (ph.get((Player) event.getEntity()).isInCombat()) {
			// TODO add feedback message to player
			event.setCancelled(true);
		}
	}

}
