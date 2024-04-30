package me.chancesd.pvpmanager.integration.hook.worldguard;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.sk89q.worldguard.bukkit.protection.events.DisallowedPVPEvent;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.ProtectionType;
import me.chancesd.sdutils.utils.Log;

public class WGListener implements Listener {

	private final PlayerManager ph;

	public WGListener(final PlayerManager ph) {
		this.ph = ph;
	}

	@EventHandler
	public final void onWGPvPCancel(final DisallowedPVPEvent event) { // NO_UCD
		if (event.getAttacker().hasMetadata("NPC") || event.getDefender().hasMetadata("NPC"))
			return;
		if (ph.checkProtection(event.getAttacker(), event.getDefender()).type() == ProtectionType.FAIL_OVERRIDE) {
			event.setCancelled(true);
			Log.debug("Force allowing PvP even though WorldGuard blocked it because a player has override or Vulnerable is enabled");
		}
	}

}
