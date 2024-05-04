package me.chancesd.pvpmanager.tasks;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.utils.ScheduleUtils;
import me.chancesd.pvpmanager.utils.TimeUtil;

public class NewbieTask implements Runnable {

	private final CombatPlayer player;
	private long finishTime;
	private ScheduledFuture<?> task;
	private boolean expired;
	private boolean paused;
	private long pausedAt;

	public NewbieTask(final CombatPlayer player, final long time) {
		this.player = player;
		final long timeLeft = time == 0 ? Conf.NEWBIE_TIME.asInt() * 1000 : time;
		this.finishTime = System.currentTimeMillis() + timeLeft;
		task = ScheduleUtils.runAsyncLater(this, timeLeft / 1000, TimeUnit.SECONDS);
		if (time != 0) {
			player.message(Lang.NEWBIE_TIME_CHECK.msg(TimeUtil.getDiffMsg(time)));
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
