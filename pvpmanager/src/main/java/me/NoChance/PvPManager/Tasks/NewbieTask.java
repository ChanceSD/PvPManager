package me.NoChance.PvPManager.Tasks;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.chancesd.pvpmanager.utils.ScheduleUtils;

public class NewbieTask implements Runnable {

	private final PvPlayer player;
	private final long finishTime;
	private final ScheduledFuture<?> task;
	private boolean expired;

	public NewbieTask(final PvPlayer player, final long time) {
		this.player = player;
		final long timeLeft = time == 0 ? Settings.getNewbieProtectionTime() * 60000 : time;
		this.finishTime = System.currentTimeMillis() + timeLeft;
		task = ScheduleUtils.runAsyncLater(this, timeLeft / 1000, TimeUnit.SECONDS);
		if (time != 0) {
			player.message(String.format(Messages.getNewbieTimeCheck(), time / 1000));
		}
	}

	@Override
	public final void run() {
		this.expired = true;
		player.setNewbie(false);
	}

	public synchronized void cancel() {
		task.cancel(true);
	}

	public boolean isExpired() {
		return this.expired;
	}

	public long getTimeleft() {
		return Math.max(0, finishTime - System.currentTimeMillis());
	}

	public long getFinishTime() {
		return finishTime;
	}

}
