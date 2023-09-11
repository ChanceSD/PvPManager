package me.chancesd.pvpmanager.utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface SchedulerProvider {

	public void runPlatformAsync(final Runnable task);

	public void runPlatformAsyncTimer(final Runnable task, final long delay, final long period);

	public void runPlatformGlobal(final Runnable task);

	public void runTask(final Runnable task, final Entity entity);

	public void runTaskLater(final Runnable task, final Entity entity, final long delay);

	public void runTaskTimer(final Runnable task, final long delay, final long period);

	public void executeConsoleCommand(final String command);

	public void executePlayerCommand(final Player player, final String command);

	public void cancelAllTasks();

}
