package me.chancesd.pvpmanager.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;


public class BukkitProvider implements SchedulerProvider {

	@Override
	public void runPlatformAsync(final JavaPlugin plugin, final Runnable task) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
	}

	@Override
	public void runPlatformAsyncTimer(final JavaPlugin plugin, final Runnable task, final long delay, final long period) {
		Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period);
	}

	@Override
	public void runTask(final JavaPlugin plugin, final Runnable task, final Entity entity) {
		Bukkit.getScheduler().runTask(plugin, task);
	}

	@Override
	public void runTaskLater(final JavaPlugin plugin, final Runnable task, final Entity entity, final long delay) {
		Bukkit.getScheduler().runTaskLater(plugin, task, delay);
	}

	@Override
	public void runTaskTimer(final JavaPlugin plugin, final Runnable task, final long delay, final long period) {
		Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
	}

	@Override
	public void cancelAllTasks(final JavaPlugin plugin) {
		Bukkit.getScheduler().cancelTasks(plugin);
	}

}
