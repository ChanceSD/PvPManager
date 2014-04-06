package me.NoChance.PvPManager.Tasks;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import org.bukkit.scheduler.BukkitRunnable;

public class CleanKillersTask extends BukkitRunnable {

	private PlayerHandler ph;

	public CleanKillersTask(PlayerHandler ph) {
		this.ph = ph;
	}

	public void run() {
		for (PvPlayer p : ph.getPlayers().values()) {
			p.clearVictims();
		}
	}

}
