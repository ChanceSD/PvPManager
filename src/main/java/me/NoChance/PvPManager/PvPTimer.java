package me.NoChance.PvPManager;

import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Config;
import me.NoChance.PvPManager.Config.Variables;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class PvPTimer {

	private PvPManager plugin;
	private long pvpOnDelay;
	private long pvpOffDelay;
	private boolean timeForPvp;
	private boolean startBiggerEnd;
	private boolean lastAnnounce;
	private World w;
	private long startPvP;
	private long endPvP;
	private String startDifficulty;
	private String endDifficulty;
	private String worldChangeOn;
	private String worldChangeOff;
	private BukkitTask changePvPTask;

	public PvPTimer(PvPManager plugin, World w) {
		this.plugin = plugin;
		this.w = w;
		getWorldValues();
		calculateDelays();
		checkTime();
	}

	public void checkTime() {
		if (!startBiggerEnd) {
			if (w.getTime() < startPvP || w.getTime() > endPvP)
				setPvP(false);
			else if (w.getTime() > startPvP && w.getTime() < endPvP)
				setPvP(true);
		} else {
			if (w.getTime() > endPvP && w.getTime() < startPvP)
				setPvP(false);
			else if (w.getTime() < endPvP || w.getTime() > startPvP)
				setPvP(true);
		}
	}

	public void setPvP(boolean pvpState) {
		if (pvpState == timeForPvp)
			return;
		timeForPvp = pvpState;
		announcePvP();
		scheduleNextChange();
	}

	private void scheduleNextChange() {
		cancel();
		changePvPTask = new BukkitRunnable() {
			public void run() {
				setPvP(!timeForPvp);
				regularScheduler();
			}
		}.runTaskLater(plugin, calculateClockDelay());
	}

	private void regularScheduler() {
		changePvPTask = new BukkitRunnable() {
			public void run() {
				setPvP(!timeForPvp);
				regularScheduler();
			}
		}.runTaskLater(plugin, timeForPvp ? pvpOnDelay : pvpOffDelay);
	}

	private void calculateDelays() {
		if (endPvP < startPvP) {
			startBiggerEnd = true;
			pvpOffDelay = startPvP - endPvP;
			pvpOnDelay = 24000 - pvpOffDelay;
		} else {
			startBiggerEnd = false;
			pvpOnDelay = endPvP - startPvP;
			pvpOffDelay = 24000 - pvpOnDelay;
		}
	}

	private long calculateClockDelay() {
		long clockDelay = 0;
		long x = w.getTime();
		if (startBiggerEnd) {
			if (timeForPvp)
				clockDelay = 24000 - x + endPvP;
			else
				clockDelay = startPvP - x;
		} else {
			if (timeForPvp)
				clockDelay = endPvP - x;
			else
				clockDelay = 24000 - x + startPvP;
		}
		return clockDelay;
	}

	private void announcePvP() {
		if (lastAnnounce == timeForPvp)
			return;
		if (timeForPvp) {
			for (Player p : w.getPlayers()) {
				p.sendMessage(Messages.PvP_On);
				if (Variables.enableSound)
					p.playSound(p.getLocation(), Sound.valueOf(Variables.pvpOnSound), 1, Variables.pvpOnSoundPitch);
			}
			w.setDifficulty(Difficulty.valueOf(startDifficulty));
		} else {
			for (Player p : w.getPlayers()) {
				p.sendMessage(Messages.PvP_Off);
				if (Variables.enableSound)
					p.playSound(p.getLocation(), Sound.valueOf(Variables.pvpOffSound), 1, Variables.pvpOffSoundPitch);
			}
			w.setDifficulty(Difficulty.valueOf(endDifficulty));
		}
		lastAnnounce = !lastAnnounce;
	}

	private void getWorldValues() {
		Config config = plugin.getConfigM().getPvpTimer();
		startPvP = config.getLong("Worlds." + w.getName() + ".Start PvP");
		endPvP = config.getLong("Worlds." + w.getName() + ".End PvP");
		startDifficulty = config.getString("Worlds." + w.getName() + ".Start Difficulty");
		endDifficulty = config.getString("Worlds." + w.getName() + ".End Difficulty");
		worldChangeOn = config.getString("Worlds." + w.getName() + ".On World Change.On");
		worldChangeOff = config.getString("Worlds." + w.getName() + ".On World Change.Off");
	}

	public void reload() {
		cancel();
		calculateDelays();
		checkTime();
	}

	public void cancel() {
		if (changePvPTask != null)
			changePvPTask.cancel();
	}

	public void setStartPvP(long startPvP) {
		plugin.getConfig().set("PvP Timer." + w.getName() + ".Start PvP", startPvP);
		plugin.saveConfig();
	}

	public void setEndPvP(long endPvP) {
		plugin.getConfig().set("PvP Timer." + w.getName() + ".End PvP", endPvP);
		plugin.saveConfig();
	}

	public void sendWorldChangeMessage(Player p) {
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', timeForPvp ? worldChangeOn : worldChangeOff));
	}

	public boolean isPvpTime() {
		return timeForPvp;
	}

	public World getWorld() {
		return w;
	}
}
