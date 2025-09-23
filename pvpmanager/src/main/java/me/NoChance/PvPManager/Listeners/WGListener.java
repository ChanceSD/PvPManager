package me.NoChance.PvPManager.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.sk89q.worldguard.bukkit.protection.events.DisallowedPVPEvent;

import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Player.ProtectionResult;
import me.chancesd.sdutils.utils.Log;

public class WGListener implements Listener {

	private final PlayerHandler ph;

	public WGListener(final PlayerHandler ph) {
		this.ph = ph;
	}

	@EventHandler
	public final void onWGPvPCancel(final DisallowedPVPEvent event) { // NO_UCD
		if (event.getAttacker().hasMetadata("NPC") || event.getDefender().hasMetadata("NPC"))
			return;
		if (ph.tryCancel(event.getAttacker(), event.getDefender()) == ProtectionResult.FAIL_OVERRIDE) {
			event.setCancelled(true);
			Log.debug("Force allowing PvP even though WorldGuard blocked it because a player has override or Vulnerable is enabled");
		}
	}

}
