package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.Utils.CombatUtils;
import me.NoChance.PvPManager.Utils.CombatUtils.CancelResult;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.sk89q.worldguard.protection.events.DisallowedPVPEvent;

public class WGListener implements Listener {

	@EventHandler
	public void onWGPvPCancel(DisallowedPVPEvent event) {
		if (event.getAttacker().hasMetadata("NPC") || event.getDefender().hasMetadata("NPC"))
			return;
		if (CombatUtils.tryCancel(event.getAttacker(), event.getDefender()) == CancelResult.FAIL_OVERRIDE)
			event.setCancelled(true);
	}

}
