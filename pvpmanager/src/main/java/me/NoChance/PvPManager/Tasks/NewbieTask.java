package me.NoChance.PvPManager.Tasks;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.chancesd.pvpmanager.utils.ScheduleUtils;
import me.chancesd.sdutils.utils.Log;

public class NewbieTask implements Runnable {

	private final PvPlayer player;
	private long finishTime;
	private ScheduledFuture<?> task;
	private boolean expired;
	private boolean paused;
	private long pausedAt;

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
		task.cancel(false);
	}

	// TODO transfer to utils
	public synchronized void pause() {
		if (!paused) {
			cancel();
			pausedAt = System.currentTimeMillis();
			paused = true;
			Log.info("Paused new player protection for " + player.getName() + " because they entered an excluded world.");
		}
	}

	public synchronized void resume() {
		if (paused) {
			final long timeLeft = getTimeleft();
			task = ScheduleUtils.runAsyncLater(this, timeLeft / 1000, TimeUnit.SECONDS);
			finishTime = System.currentTimeMillis() + timeLeft;
			paused = false;
		}
	}

	public boolean isExpired() {
		return this.expired;
	}

	public long getTimeleft() {
		if (paused) {
			final long pauseDuration = System.currentTimeMillis() - pausedAt;
			return Math.max(0, finishTime + pauseDuration - System.currentTimeMillis());
		}
		return Math.max(0, finishTime - System.currentTimeMillis());
	}

	public long getFinishTime() {
		return finishTime;
	}

}
