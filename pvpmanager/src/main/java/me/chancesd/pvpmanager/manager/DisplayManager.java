package me.chancesd.pvpmanager.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.display.ProgressBar;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.utils.ChatUtils;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.Utils;

public class DisplayManager {

	private final Map<CombatPlayer, ProgressBar> actionBars = new ConcurrentHashMap<>();
	private final Map<CombatPlayer, BossBar> bossBars = new ConcurrentHashMap<>();

	public void updateBossbar(final CombatPlayer player, final double timePassed, final int totalTime) {
		final BossBar bossBar = bossBars.computeIfAbsent(player, this::setupBossbar);
		final String message = Conf.BOSS_BAR_MESSAGE.asString().replace("<time>", Long.toString(totalTime - Math.round(timePassed)));
		final String placeHolderMessage = ChatUtils.setPlaceholders(player.getPlayer(), message);
		if (!bossBar.getTitle().equals(placeHolderMessage))
			bossBar.setTitle(placeHolderMessage);
		bossBar.setProgress((totalTime - timePassed) / totalTime);
	}

	private BossBar setupBossbar(final CombatPlayer player) {
		final BossBar bossBar = Bukkit.createBossBar("", Conf.BOSS_BAR_COLOR.asEnum(BarColor.class), Conf.BOSS_BAR_STYLE.asEnum(BarStyle.class));
		bossBar.addPlayer(player.getPlayer());
		return bossBar;
	}

	public void discardPlayer(final CombatPlayer player) {
		if (actionBars.remove(player) == null)
			Log.debug("Tried to discard action bar that didn't exist");
		final BossBar bossBar = bossBars.get(player);
		if (bossBar == null) {
			Log.debug("Tried to discard bossbar that didn't exist"); // display task might not have created one yet
			return;
		}
		bossBar.removePlayer(player.getPlayer());
		bossBars.remove(player);
	}

	private ProgressBar setupProgressBar(final double time, final int goal) {
		return new ProgressBar(Conf.ACTION_BAR_MESSAGE.asString(), Conf.ACTION_BAR_BARS.asInt(), goal, Conf.ACTION_BAR_SYMBOL.asString(), time);
	}

	public void showProgress(final CombatPlayer p, final double timePassed, final int goal) {
		final double timePassedRounded = Utils.roundTo1Decimal(timePassed);
		final ProgressBar progressBar = actionBars.computeIfAbsent(p, x -> setupProgressBar(timePassedRounded, goal));
		progressBar.setProgress(timePassedRounded).setGoal(goal).calculate();
		p.sendActionBar(ChatUtils.setPlaceholders(p.getPlayer(), progressBar.getMessage()));
	}

}
