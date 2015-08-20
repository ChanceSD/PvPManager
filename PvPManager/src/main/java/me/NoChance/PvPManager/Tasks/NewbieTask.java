package me.NoChance.PvPManager.Tasks;

import me.NoChance.PvPManager.PvPlayer;
import org.bukkit.scheduler.BukkitRunnable;

public class NewbieTask extends BukkitRunnable {

	private final PvPlayer player;

	public NewbieTask(final PvPlayer player) {
		this.player = player;
	}

	@Override
	public final void run() {
		player.setNewbie(false);
	}

}
