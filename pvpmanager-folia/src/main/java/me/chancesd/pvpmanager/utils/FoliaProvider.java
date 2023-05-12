package me.chancesd.pvpmanager.utils;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public class FoliaProvider implements SchedulerProvider {

	@Override
	public void runPlatformAsync(final JavaPlugin plugin, final Runnable task) {
		Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
	}

	@Override
	public void runPlatformAsyncTimer(final JavaPlugin plugin, final Runnable task, final long delay, final long period) {
		Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delay / 20, period / 20, TimeUnit.SECONDS);
	}

	@Override
	public void runTask(final JavaPlugin plugin, final Runnable task, final Entity entity) {
		entity.getScheduler().run(plugin, scheduledTask -> task.run(), task);
	}

	@Override
	public void runTaskLater(final JavaPlugin plugin, final Runnable task, final Entity entity, final long delay) {
		entity.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), task, delay);
	}

	@Override
	public void runTaskTimer(final JavaPlugin plugin, final Runnable task, final long delay, final long period) {
		Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delay, period);
	}

	@Override
	public void cancelAllTasks(final JavaPlugin plugin) {
		Bukkit.getAsyncScheduler().cancelTasks(plugin);
		Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
	}

}
