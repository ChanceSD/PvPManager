package me.chancesd.pvpmanager.tasks;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.UntagReason;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.sdutils.utils.ChatUtils;
import me.chancesd.sdutils.display.BossBarBuilder;
import me.chancesd.sdutils.display.CountdownData;
import me.chancesd.sdutils.display.DisplayManager;
import me.chancesd.sdutils.display.ProgressBar;
import me.chancesd.sdutils.display.DisplayManager.TimeProgressSource;
import me.chancesd.sdutils.utils.Utils;

public class TagTask {

	private final Map<CombatPlayer, CountdownData> taggedCountdowns = new ConcurrentHashMap<>();
	private final DisplayManager display;
	private final BossBarBuilder bossBar;

	public TagTask(final DisplayManager display) {
		this.display = display;
		this.bossBar = BossBarBuilder.create().barColor(Conf.BOSS_BAR_COLOR.asEnum(BarColor.class))
				.barStyle(Conf.BOSS_BAR_STYLE.asEnum(BarStyle.class));
	}

	public final boolean cancel() {
		for (final Entry<CombatPlayer, CountdownData> entry : taggedCountdowns.entrySet()) {
			final CombatPlayer combatPlayer = entry.getKey();
			if (combatPlayer.isInCombat()) {
				display.cancelCountdown(combatPlayer.getPlayer(), entry.getValue());
				combatPlayer.untag(UntagReason.PLUGIN_DISABLE);
			}
		}
		return false;
	}

	public final void startTracking(final CombatPlayer combatPlayer) {
		final ProgressBar progressBar = new ProgressBar(Conf.ACTION_BAR_MESSAGE.asString(), Conf.ACTION_BAR_BARS.asInt(), combatPlayer.getTotalTagTime(),
				Conf.ACTION_BAR_SYMBOL.asString());

		final TimeProgressSource timeProgressSource = new TimeProgressSource() {
			@Override
			public long getGoal() {
				return combatPlayer.getTotalTagTime() / 1000;
			}

			@Override
			public double getProgress() {
				return (System.currentTimeMillis() - combatPlayer.getTaggedTime()) / 1000D;
			}
		};

		final CountdownData.Builder builder = new CountdownData.Builder();
		if (Conf.ACTION_BAR_ENABLED.asBool()) {
			builder.withActionBar(progressBar, timeSource -> {
				final String message = ChatUtils.setPlaceholders(combatPlayer.getPlayer(), progressBar.getMessage());
				combatPlayer.sendActionBar(message);
				return message;
			});
		}
		if (Conf.BOSS_BAR_ENABLED.asBool()) {
			builder.withBossBar(bossBar.build(), timeSource -> {
				final String message = Conf.BOSS_BAR_MESSAGE.asString().replace("<time>",
						Double.toString(Utils.roundTo1Decimal(timeSource.getGoal() - timeSource.getProgress())));
				return ChatUtils.setPlaceholders(combatPlayer.getPlayer(), message);
			});
		}
		final CountdownData countdownData = builder.withTimeSource(timeProgressSource)
				.onFinish(() -> combatPlayer.untag(UntagReason.TIME_EXPIRED))
				.build(combatPlayer.getPlayer());

		display.createCountdown(combatPlayer.getPlayer(), countdownData);
		taggedCountdowns.put(combatPlayer, countdownData);
	}

	public final void stopTracking(final CombatPlayer combatPlayer, final UntagReason reason) {
		final CountdownData countdownData = taggedCountdowns.remove(combatPlayer);
		if (reason != UntagReason.TIME_EXPIRED) {
			display.cancelCountdown(combatPlayer.getPlayer(), countdownData);
		}
	}

	public Set<CombatPlayer> getTaggedPlayers() {
		return taggedCountdowns.keySet();
	}

}
