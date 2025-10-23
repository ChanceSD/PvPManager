package me.chancesd.pvpmanager.tasks;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.sdutils.tasks.PausableTask;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.TimeUtil;
import me.chancesd.sdutils.display.BossBarBuilder;
import me.chancesd.sdutils.display.CountdownData;
import me.chancesd.sdutils.display.DisplayManager;
import me.chancesd.sdutils.display.DisplayManager.TimeProgressSource;

public class NewbieTask extends PausableTask {

	private final CombatPlayer player;
	private CountdownData countdownData;

	public NewbieTask(final CombatPlayer player, final long time) {
		super(time == 0 ? Conf.NEWBIE_TIME.asInt() * 1000L : time);
		this.player = player;
		if (time == 0) {
			player.message(Lang.NEWBIE_PROTECTION.msgTimeUntil(getFinishTime()));
		} else {
			player.message(Lang.NEWBIE_TIME_CHECK.msgTime(time));
		}

		if (Conf.NEWBIE_BOSS_BAR_ENABLED.asBool()) {
			startBossBarDisplay();
		}
	}

	@Override
	protected void onComplete() {
		player.setNewbie(false);
		player.message(Lang.NEWBIE_PROTECTION_END);
		stopBossBarDisplay();
	}

	@Override
	public synchronized void pause() {
		super.pause();
		Log.info("Paused new player protection for " + player.getName() + " because they entered an excluded world.");
		if (Conf.NEWBIE_BOSS_BAR_ENABLED.asBool() && countdownData != null) {
			stopBossBarDisplay();
		}
	}

	@Override
	public synchronized void resume() {
		super.resume();
		Log.info("Resumed new player protection for " + player.getName() + " because they left an excluded world.");
		if (Conf.NEWBIE_BOSS_BAR_ENABLED.asBool() && countdownData == null) {
			startBossBarDisplay();
		}
	}

	@Override
	public synchronized void cancel() {
		super.cancel();
		stopBossBarDisplay();
	}

	private void startBossBarDisplay() {
		final DisplayManager displayManager = getDisplayManager();

		final BossBarBuilder bossBar = BossBarBuilder.create()
				.barColor(Conf.NEWBIE_BOSS_BAR_COLOR.asEnum(BarColor.class))
				.barStyle(Conf.NEWBIE_BOSS_BAR_STYLE.asEnum(BarStyle.class));

		final TimeProgressSource timeProgressSource = new TimeProgressSource() {
			@Override
			public long getGoal() {
				return getDuration() / 1000;
			}

			@Override
			public double getProgress() {
				return (getDuration() - getTimeleft()) / 1000.0;
			}
		};

		final CountdownData.Builder builder = new CountdownData.Builder()
				.withBossBar(bossBar.build(), timeSource -> {
					final long currentTimeLeft = getTimeleft();
					final String currentFormattedTime = TimeUtil.getDiffDuration(Lang.class, currentTimeLeft);
					final String message = Conf.NEWBIE_BOSS_BAR_MESSAGE.asString()
							.replace("<time>", currentFormattedTime);
					return CombatUtils.processPlaceholders(player.getPlayer(), message);
				})
				.withTimeSource(timeProgressSource);

		countdownData = builder.build(player.getPlayer());
		displayManager.createCountdown(player.getPlayer(), countdownData);
	}

	private void stopBossBarDisplay() {
		if (countdownData != null) {
			final DisplayManager displayManager = getDisplayManager();
			if (displayManager != null) {
				displayManager.cancelCountdown(player.getPlayer(), countdownData);
			}
			countdownData = null;
		}
	}

	private DisplayManager getDisplayManager() {
		return PvPManager.getInstance().getDisplayManager();
	}

}
