package me.chancesd.pvpmanager.utils;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface SchedulerProvider {

	public void runPlatformAsync(final Runnable task);

	public void runPlatformAsyncTimer(final Runnable task, final long delay, final long period);

	public void runTask(final Runnable task);

	public void runTask(final Runnable task, final Entity entity);

	public void runTaskLater(final Runnable task, final Entity entity, final long delay);

	public void runTaskTimer(final Runnable task, final long delay, final long period);

	public void executeConsoleCommand(final String command);

	public void executePlayerCommand(final Player player, final String command);

	public boolean isPrimaryThread();

	public CompletableFuture<Boolean> teleport(Player player, @NotNull Location loc);

	public void cancelAllTasks();

}
