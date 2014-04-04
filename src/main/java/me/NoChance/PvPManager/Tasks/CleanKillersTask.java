package me.NoChance.PvPManager.Tasks;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import org.bukkit.scheduler.BukkitRunnable;

public class CleanKillersTask extends BukkitRunnable {

	private PlayerHandler ph;

	public CleanKillersTask(PvPManager plugin) {
		this.ph = plugin.getPlayerHandler();
		this.runTaskTimer(plugin, 1200, Variables.killAbuseTime * 20);
	}

	public void run() {
		for (PvPlayer p : ph.getPlayers().values()) {
			p.clearVictims();
		}
	}

}
