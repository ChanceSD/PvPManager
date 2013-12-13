package me.NoChance.PvPManager;

import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Others.SimpleConfig;

import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class PvPTimer {

	private PvPManager plugin;
	private long pvpOnDelay;
	private long pvpOffDelay;
	private boolean timeForPvp;
	private String lastAnnounce;
	private World w;
	private int[] scheduledTasks = new int[5];
	private long startPvP;
	private long endPvP;
	private String startDifficulty;
	private String endDifficulty;
	private String worldChangeOn;
	private String worldChangeOff;

	public PvPTimer(PvPManager plugin, World w) {
		this.plugin = plugin;
		this.w = w;
		getWorldValues();
		calculateDelays();
		checkWorldPvP();
	}

	public void checkWorldPvP() {
		scheduledTasks[4] = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				if (endPvP > startPvP) {
					if (w.getTime() < startPvP || w.getTime() > endPvP) {
						timeForPvp = false;
						setPvpClock(false);
						announcePvP(false);
					} else if (w.getTime() > startPvP && w.getTime() < endPvP) {
						timeForPvp = true;
						setPvpClock(true);
						announcePvP(true);
					}
				}
				if (endPvP < startPvP) {
					if (w.getTime() > endPvP && w.getTime() < startPvP) {
						timeForPvp = false;
						setPvpClock(false);
						announcePvP(false);
					} else if (w.getTime() < endPvP || w.getTime() > startPvP) {
						timeForPvp = true;
						setPvpClock(true);
						announcePvP(true);
					}
				}
			}
		}, 20);
	}

	public void setPvpClock(boolean pvpOn) {
		cancelAllTasks();
		if (pvpOn) {
			scheduledTasks[0] = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					timeForPvp = false;
					announcePvP(false);
					pvpScheduler();
				}
			}, calculateClockDelay());
		} else {
			scheduledTasks[1] = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					timeForPvp = true;
					announcePvP(true);
					pvpScheduler();
				}
			}, calculateClockDelay());
		}
	}

	public void pvpScheduler() {
		if (timeForPvp) {
			scheduledTasks[2] = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					timeForPvp = !timeForPvp;
					announcePvP(false);
					pvpScheduler();
				}
			}, pvpOnDelay);
		} else {
			scheduledTasks[3] = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					timeForPvp = !timeForPvp;
					announcePvP(true);
					pvpScheduler();
				}
			}, pvpOffDelay);
		}
	}

	public void calculateDelays() {
		if (endPvP > startPvP) {
			pvpOnDelay = endPvP - startPvP;
			pvpOffDelay = 24000 - pvpOnDelay;
		}
		if (endPvP < startPvP) {
			pvpOffDelay = startPvP - endPvP;
			pvpOnDelay = 24000 - pvpOffDelay;
		}
	}

	public long calculateClockDelay() {
		long clockDelay = 0;
		long x = w.getTime();
		if (endPvP > startPvP) {
			if (x < startPvP)
				clockDelay = startPvP - x;
			if (x > startPvP || x < endPvP)
				clockDelay = endPvP - x;
			if (x > endPvP)
				clockDelay = pvpOffDelay - (x - endPvP);
		} else if (endPvP < startPvP) {
			if (x < endPvP)
				clockDelay = endPvP - x;
			if (x > endPvP || x < startPvP)
				clockDelay = startPvP - x;
			if (x > startPvP)
				clockDelay = pvpOnDelay - (x - startPvP);
		}
		return clockDelay;
	}

	public void announcePvP(boolean status) {
		if (lastAnnounce == "Off" && !status || lastAnnounce == "On" && status) {
			return;
		}
		if (lastAnnounce == null && !status || lastAnnounce == "On" && !status) {
			for (Player p : w.getPlayers()) {
				p.sendMessage(Messages.PvP_Off);
				if (Variables.enableSound)
					p.playSound(p.getLocation(), Sound.valueOf(Variables.pvpOffSound), 1, Variables.pvpOffSoundPitch);
			}
			w.setDifficulty(Difficulty.valueOf(endDifficulty));
			lastAnnounce = "Off";
		} else if (lastAnnounce == null && status || lastAnnounce == "Off" && status) {
			for (Player p : w.getPlayers()) {
				p.sendMessage(Messages.PvP_On);
				if (Variables.enableSound)
					p.playSound(p.getLocation(), Sound.valueOf(Variables.pvpOnSound), 1, Variables.pvpOnSoundPitch);
			}
			w.setDifficulty(Difficulty.valueOf(startDifficulty));
			lastAnnounce = "On";
		}
	}

	public void announcePvP(Player p) {
		if (timeForPvp)
			p.sendMessage(Messages.PvP_On);
		else
			p.sendMessage(Messages.PvP_Off);
	}

	public void getWorldValues() {
		SimpleConfig config = plugin.getConfigM().getPvpTimer();
		startPvP = config.getLong("Worlds." + w.getName() + ".Start PvP");
		endPvP = config.getLong("Worlds." + w.getName() + ".End PvP");
		startDifficulty = config.getString("Worlds." + w.getName() + ".Start Difficulty");
		endDifficulty = config.getString("Worlds." + w.getName() + ".End Difficulty");
		worldChangeOn = config.getString("Worlds." + w.getName() + ".On World Change.On");
		worldChangeOff = config.getString("Worlds." + w.getName() + ".On World Change.Off");
	}

	public void reload() {
		cancelAllTasks();
		calculateDelays();
		checkWorldPvP();
	}

	public void cancelAllTasks() {
		for (int i = 0; i < scheduledTasks.length; i++) {
			plugin.getServer().getScheduler().cancelTask(scheduledTasks[i]);
		}
	}

	public void setStartPvP(long startPvP) {
		plugin.getConfig().set("PvP Timer." + w.getName() + ".Start PvP", startPvP);
		plugin.saveConfig();
	}

	public void setEndPvP(long endPvP) {
		plugin.getConfig().set("PvP Timer." + w.getName() + ".End PvP", endPvP);
		plugin.saveConfig();
	}

	public String getWorldChangeMessage() {
		if (timeForPvp)
			return ChatColor.translateAlternateColorCodes('&', worldChangeOn);
		else
			return ChatColor.translateAlternateColorCodes('&', worldChangeOff);
	}

	public void sendWorldChangeMessage(Player p) {
		p.sendMessage(getWorldChangeMessage());
	}

	public boolean isPvpTime() {
		return timeForPvp;
	}

	public World getWorld() {
		return w;
	}
}
