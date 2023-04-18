package me.NoChance.PvPManager.Managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Player.display.ProgressBar;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.Log;

public class DisplayManager {

	private final Map<Integer, ProgressBar> actionBars = new HashMap<>();
	private final Map<PvPlayer, BossBar> bossBars = new HashMap<>();
	private final PvPManager plugin;

	public DisplayManager(final PvPManager plugin) {
		this.plugin = plugin;
		setupActionBar();
	}

	public void updateBossbar(final PvPlayer player, final double timePassed) {
		final BossBar bossBar = bossBars.computeIfAbsent(player, this::setupBossbar);
		bossBar.setTitle(Settings.getBossBarMessage().replace("<time>", Long.toString(Settings.getTimeInCombat() - Math.round(timePassed))));
		bossBar.setProgress((Settings.getTimeInCombat() - timePassed) / Settings.getTimeInCombat());
	}

	private BossBar setupBossbar(final PvPlayer player) {
		final BossBar bossBar = Bukkit.createBossBar("", Settings.getBossBarColor(), Settings.getBossBarStyle());
		bossBar.addPlayer(player.getPlayer());
		return bossBar;
	}

	public void discardBossbar(final PvPlayer player) {
		final BossBar bossBar = bossBars.get(player);
		if (bossBar == null) {
			Log.debug("Tried to discard bossbar that didn't exist"); // display task might not have created one yet
			return;
		}
		bossBar.removePlayer(player.getPlayer());
		bossBars.remove(player);
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
