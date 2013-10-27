package me.NoChance.PvPManager;

import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class PvPTimer {

	private PvPManager plugin;
	private long pvpOnDelay;
	private long pvpOffDelay;
	public boolean timeForPvp;
	private String lastAnnounce;
	public World w;
	private int[] scheduledTasks = new int[5];
	private long startPvP;
	private long endPvP;

	public PvPTimer(PvPManager plugin, World w) {
		this.plugin = plugin;
		this.w = w;
		calculateDelays();
		checkWorldPvP();
	}

	public void checkWorldPvP() {
		scheduledTasks[4] = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
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
				public void run() {
					timeForPvp = false;
					announcePvP(false);
					pvpScheduler();
				}
			}, calculateClockDelay());
		} else {
			scheduledTasks[1] = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
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
				public void run() {
					timeForPvp = !timeForPvp;
					announcePvP(false);
					pvpScheduler();
				}
			}, pvpOnDelay);
		} else {
			scheduledTasks[3] = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					timeForPvp = !timeForPvp;
					announcePvP(true);
					pvpScheduler();
				}
			}, pvpOffDelay);
		}
	}

	public void calculateDelays() {
		startPvP = plugin.getConfig().getLong("PvP Timer." + w.getName() + ".Start PvP");
		endPvP = plugin.getConfig().getLong("PvP Timer." + w.getName() + ".End PvP");
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
					p.playSound(p.getLocation(), Sound.valueOf(Variables.pvpOffSound), 1, 0);
			}
			lastAnnounce = "Off";
		} else if (lastAnnounce == null && status || lastAnnounce == "Off" && status) {
			for (Player p : w.getPlayers()) {
				p.sendMessage(Messages.PvP_On);
				if (Variables.enableSound)
					p.playSound(p.getLocation(), Sound.valueOf(Variables.pvpOnSound), 1, 0);
			}
			lastAnnounce = "On";
		}
	}

	public void announcePvP(Player p) {
		if (timeForPvp)
			p.sendMessage(Messages.PvP_On);
		else
			p.sendMessage(Messages.PvP_Off);
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
			return plugin.getConfig().getString("PvP Timer." + w.getName() + "On World Change.On");
		else
			return plugin.getConfig().getString("PvP Timer." + w.getName() + "On World Change.Off");
	}
	
	public void sendWorldChangeMessage(Player p){
		p.sendMessage(getWorldChangeMessage());
	}
}
