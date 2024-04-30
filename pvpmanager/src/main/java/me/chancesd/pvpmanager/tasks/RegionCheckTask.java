package me.chancesd.pvpmanager.tasks;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import me.NoChance.PvPManager.PvPlayer;
import me.chancesd.pvpmanager.event.PlayerTagEvent;
import me.chancesd.pvpmanager.event.PlayerUntagEvent;
import me.chancesd.pvpmanager.manager.DependencyManager;
import me.chancesd.pvpmanager.manager.PlayerHandler;
import me.chancesd.pvpmanager.setting.Messages;

public class RegionCheckTask extends BukkitRunnable implements Listener {

	private final PlayerHandler playerHandler;
	private final Map<PvPlayer, Location> lastLocations = new HashMap<>();
	private final DependencyManager dependencyManager;

	public RegionCheckTask(final PlayerHandler playerHandler, final DependencyManager dependencyManager) {
		this.playerHandler = playerHandler;
		this.dependencyManager = dependencyManager;
	}

	@Override
	public void run() {
		for (final PvPlayer pvPlayer : playerHandler.getPlayersInCombat()) {
			final Player player = pvPlayer.getPlayer();
			final Location playerLocation = player.getLocation();
			if (!dependencyManager.canAttackAt(player, playerLocation)) {
				final Location lastLocation = lastLocations.get(pvPlayer);
				if (lastLocation == null)
					continue;
				lastLocation.setPitch(playerLocation.getPitch());
				lastLocation.setYaw(playerLocation.getYaw());
				player.teleport(lastLocation);
				pvPlayer.message(Messages.getPushbackWarning());
			} else {
				lastLocations.put(pvPlayer, playerLocation);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerTag(final PlayerTagEvent event) {
		lastLocations.put(event.getPvPlayer(), event.getPlayer().getLocation());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerTag(final PlayerUntagEvent event) {
		lastLocations.remove(event.getPvPlayer());
	}

}
