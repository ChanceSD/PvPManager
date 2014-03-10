package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.Utils.CombatUtils;
import me.NoChance.PvPManager.Utils.CombatUtils.CancelResult;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.events.DisallowedPVPEvent;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;

public class WGDependency implements Listener {

	private WorldGuardPlugin wg;

	public WGDependency() {
		wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
	}

	@EventHandler
	public void onWGPvPCancel(DisallowedPVPEvent event) {
		if (CombatUtils.tryCancel(event.getAttacker(), event.getDefender()) == CancelResult.FAIL_OVERRIDE)
			event.setCancelled(true);
	}

	public boolean hasWGPvPFlag(World world, Location loc) {
		ApplicableRegionSet set = wg.getRegionManager(world).getApplicableRegions(loc);
		if (set.getFlag(DefaultFlag.PVP) != null) {
			if (set.getFlag(DefaultFlag.PVP).equals(State.ALLOW))
				return true;
		}
		return false;
	}

}
