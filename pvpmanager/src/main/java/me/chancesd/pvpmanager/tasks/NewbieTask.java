package me.chancesd.pvpmanager.tasks;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Settings;
import me.chancesd.pvpmanager.utils.ScheduleUtils;
import me.chancesd.pvpmanager.utils.TimeUtil;

public class NewbieTask implements Runnable {

	private final CombatPlayer player;
	private final long finishTime;
	private final ScheduledFuture<?> task;
	private boolean expired;

	public NewbieTask(final CombatPlayer player, final long time) {
		this.player = player;
		final long timeLeft = time == 0 ? Settings.getNewbieProtectionTime() * 60000 : time;
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
