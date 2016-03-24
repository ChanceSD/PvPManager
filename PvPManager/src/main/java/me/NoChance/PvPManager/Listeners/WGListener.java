package me.NoChance.PvPManager.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.sk89q.worldguard.protection.events.DisallowedPVPEvent;

import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Utils.CancelResult;

public class WGListener implements Listener {

	private final PlayerHandler ph;

	public WGListener(final PlayerHandler ph) {
		this.ph = ph;
	}

	@EventHandler
	public final void onWGPvPCancel(final DisallowedPVPEvent event) { // NO_UCD
		if (event.getAttacker().hasMetadata("NPC") || event.getDefender().hasMetadata("NPC"))
			return;
		if (ph.tryCancel(event.getAttacker(), event.getDefender()) == CancelResult.FAIL_OVERRIDE) {
			event.setCancelled(true);
		}
	}

}
