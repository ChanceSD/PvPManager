package me.NoChance.PvPManager.Managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Player.display.ProgressBar;
import me.NoChance.PvPManager.Settings.Settings;

public class DisplayManager {

	private final Map<Integer, ProgressBar> actionBars = new HashMap<>();
	private final Map<PvPlayer, BossBar> bossBars = new HashMap<>();
	private final PvPManager plugin;

	public DisplayManager(final PvPManager plugin) {
		this.plugin = plugin;
		setupActionBar();
	}

	public void updateBossbar(final PvPlayer player) {
		BossBar bossBar = bossBars.get(player);
		if (bossBar == null) {
			bossBar = setupBossbar();
		}
		bossBar.setTitle(null);
		bossBar.setProgress(0);
	}

	private BossBar setupBossbar() {
		final String title = "title";
		return Bukkit.createBossBar(title, BarColor.RED, BarStyle.SEGMENTED_20);
	}

	private void setupActionBar() {
		if (Settings.getActionBarMessage().isEmpty())
			return;

		for (int i = 0; i < Settings.getTimeInCombat() + 1; i++) {
			actionBars.put(i,
			        new ProgressBar(Settings.getActionBarMessage(), Settings.getActionBarBars(), Settings.getTimeInCombat(), Settings.getActionBarSymbol(), i));
		}
	}

	public void showProgress(final PvPlayer p, final double timePassed) {
		p.sendActionBar(actionBars.get((int) (timePassed + 0.5)).getMessage());
	}

	public PvPManager getPlugin() {
		return plugin;
	}

}
