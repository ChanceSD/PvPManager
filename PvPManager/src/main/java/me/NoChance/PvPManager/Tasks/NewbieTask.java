package me.NoChance.PvPManager.Tasks;

import org.bukkit.scheduler.BukkitRunnable;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Settings;

public class NewbieTask extends BukkitRunnable {

	private final PvPlayer player;
	private final long finishTime;

	public NewbieTask(final PvPlayer player) {
		this.player = player;
		this.finishTime = System.currentTimeMillis() + Settings.getNewbieProtectionTime() * 60000;
	}

	@Override
	public final void run() {
		player.setNewbie(false);
	}

	public long getFinishTime() {
		return finishTime;
	}

	public long getTimeleft() {
		return finishTime - System.currentTimeMillis();
	}

}
