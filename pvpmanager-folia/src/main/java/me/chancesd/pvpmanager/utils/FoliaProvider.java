package me.chancesd.pvpmanager.utils;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class FoliaProvider implements SchedulerProvider {

	@NotNull
	private final JavaPlugin plugin;

	public FoliaProvider(@NotNull final JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void runPlatformAsync(final Runnable task) {
		Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
	}

	@Override
	public void runPlatformAsyncTimer(final Runnable task, final long delay, final long period) {
		Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delay / 20, period / 20, TimeUnit.SECONDS);
	}

	@Override
	public void runTask(final Runnable task, final Entity entity) {
		entity.getScheduler().run(plugin, scheduledTask -> task.run(), task);
	}

	@Override
	public void runTaskLater(final Runnable task, final Entity entity, final long delay) {
		entity.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), task, delay);
	}

	@Override
	public void runTaskTimer(final Runnable task, final long delay, final long period) {
		Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delay, period);
	}

	@Override
	public void executeConsoleCommand(final String command) {
		Bukkit.getGlobalRegionScheduler().execute(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
	}

	@Override
	public void executePlayerCommand(final Player player, final String command) {
		player.performCommand(command);
	}

	@Override
	public void cancelAllTasks() {
		Bukkit.getAsyncScheduler().cancelTasks(plugin);
		Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
	}

}
