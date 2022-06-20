package me.NoChance.PvPManager.Tasks;

import org.bukkit.scheduler.BukkitRunnable;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;

public class NewbieTask extends BukkitRunnable {

	private final PvPlayer player;
	private final long finishTime;

	public NewbieTask(final PvPlayer player, final PvPManager plugin, final long time) {
		this.player = player;
		final long timeLeft = time == 0 ? Settings.getNewbieProtectionTime() * 60000 : time;
		this.finishTime = System.currentTimeMillis() + timeLeft;
		this.runTaskLater(plugin, timeLeft / 50);
		if (time != 0) {
			player.message(String.format(Messages.getNewbieTimeCheck(), time / 1000));
		}
	}

	@Override
	public final void run() {
		player.setNewbie(false);
	}

	public long getTimeleft() {
		return Math.max(0, finishTime - System.currentTimeMillis());
	}

}
