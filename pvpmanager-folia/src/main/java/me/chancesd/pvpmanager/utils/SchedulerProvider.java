package me.chancesd.pvpmanager.utils;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public interface SchedulerProvider {

	public void runPlatformAsync(final JavaPlugin plugin, final Runnable task);

	public void runPlatformAsyncTimer(final JavaPlugin plugin, final Runnable task, final long delay, final long period);

	public void runTask(final JavaPlugin plugin, final Runnable task, final Entity entity);

	public void runTaskLater(final JavaPlugin plugin, final Runnable task, final Entity entity, final long delay);

	public void runTaskTimer(final JavaPlugin plugin, final Runnable task, final long delay, final long period);

	public void cancelAllTasks(final JavaPlugin plugin);

}
