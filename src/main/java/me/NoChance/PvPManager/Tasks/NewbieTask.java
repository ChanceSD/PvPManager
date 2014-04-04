package me.NoChance.PvPManager.Tasks;

import me.NoChance.PvPManager.PvPlayer;

import org.bukkit.scheduler.BukkitRunnable;

public class NewbieTask extends BukkitRunnable {

	private PvPlayer player;

	public NewbieTask(PvPlayer player) {
		this.player = player;
	}

	public void run() {
		player.setNewbie(false);
	}

}
