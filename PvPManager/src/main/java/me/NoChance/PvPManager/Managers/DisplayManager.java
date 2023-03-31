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
import net.md_5.bungee.api.ChatColor;

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
		bossBar.setTitle(
		        ChatColor.translateAlternateColorCodes('&', "&c&lIn Combat - &6") + (Settings.getTimeInCombat() - Math.round(timePassed)) + " seconds remaining");
		bossBar.setProgress((Settings.getTimeInCombat() - timePassed) / Settings.getTimeInCombat());
	}

	private BossBar setupBossbar(final PvPlayer player) {
		final BossBar bossBar = Bukkit.createBossBar("", BarColor.RED, BarStyle.SEGMENTED_10);
		bossBar.addPlayer(player.getPlayer());
		return bossBar;
	}

	public void discardBossbar(final PvPlayer player) {
		bossBars.get(player).removePlayer(player.getPlayer());
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
