package me.NoChance.PvPManager.Tasks;

import java.util.concurrent.ScheduledFuture;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.chancesd.pvpmanager.utils.ScheduleUtils;

public class NewbieTask implements Runnable {

	private final PvPlayer player;
	private final long finishTime;
	private final ScheduledFuture<?> task;

	public NewbieTask(final PvPlayer player, final long time) {
		this.player = player;
		final long timeLeft = time == 0 ? Settings.getNewbieProtectionTime() * 60000 : time;
		this.finishTime = System.currentTimeMillis() + timeLeft;
		task = ScheduleUtils.runAsyncLater(this, timeLeft / 1000);
		if (time != 0) {
			player.message(String.format(Messages.getNewbieTimeCheck(), time / 1000));
		}
	}

	@Override
	public final void run() {
		player.setNewbie(false);
	}

	public synchronized void cancel() {
		task.cancel(true);
	}

	public boolean isScheduled() {
		return task.isDone();
	}

	public long getTimeleft() {
		return Math.max(0, finishTime - System.currentTimeMillis());
	}

}
