package me.NoChance.PvPManager.Tasks;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Managers.PlayerHandler;

import org.bukkit.scheduler.BukkitRunnable;

public class CleanKillersTask extends BukkitRunnable {

	private final PlayerHandler ph;

	public CleanKillersTask(final PlayerHandler ph) {
		this.ph = ph;
	}

	public final void run() {
		for (final PvPlayer p : ph.getPlayers().values()) {
			p.clearVictims();
		}
	}

}
