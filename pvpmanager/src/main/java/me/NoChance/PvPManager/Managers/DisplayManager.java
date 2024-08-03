package me.NoChance.PvPManager.Managers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Player.display.ProgressBar;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.ChatUtils;
import me.chancesd.sdutils.utils.Log;

public class DisplayManager {

	private final Map<PvPlayer, ProgressBar> actionBars = new ConcurrentHashMap<>();
	private final Map<PvPlayer, BossBar> bossBars = new ConcurrentHashMap<>();
	private final PvPManager plugin;

	public DisplayManager(final PvPManager plugin) {
		this.plugin = plugin;
	}

	public void updateBossbar(final PvPlayer player, final double timePassed, final int totalTime) {
		final BossBar bossBar = bossBars.computeIfAbsent(player, this::setupBossbar);
		final String message = Settings.getBossBarMessage().replace("<time>", Long.toString(totalTime - Math.round(timePassed)));
		final String placeHolderMessage = ChatUtils.setPlaceholders(player.getPlayer(), message);
		if (!bossBar.getTitle().equals(placeHolderMessage))
			bossBar.setTitle(placeHolderMessage);
		bossBar.setProgress((totalTime - timePassed) / totalTime);
	}

	private BossBar setupBossbar(final PvPlayer player) {
		final BossBar bossBar = Bukkit.createBossBar("", Settings.getBossBarColor(), Settings.getBossBarStyle());
		bossBar.addPlayer(player.getPlayer());
		return bossBar;
	}

	public void discardPlayer(final PvPlayer player) {
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

	private ProgressBar setupProgressBar(final long time, final int goal) {
		return new ProgressBar(Settings.getActionBarMessage(), Settings.getActionBarBars(), goal, Settings.getActionBarSymbol(), time);
	}

	public void showProgress(final PvPlayer p, final double timePassed, final int goal) {
		final long timePassedRounded = Math.round(timePassed);
		final ProgressBar progressBar = actionBars.computeIfAbsent(p, x -> setupProgressBar(timePassedRounded, goal));
		progressBar.setProgress(timePassedRounded).setGoal(goal).calculate();
		p.sendActionBar(ChatUtils.setPlaceholders(p.getPlayer(), progressBar.getMessage()));
	}

	public PvPManager getPlugin() {
		return plugin;
	}

}
