package me.chancesd.pvpmanager.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;


public class BukkitProvider implements SchedulerProvider {

	@NotNull
	private final JavaPlugin plugin;

	public BukkitProvider(@NotNull final JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void runPlatformAsync(final Runnable task) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
	}

	@Override
	public void runPlatformAsyncTimer(final Runnable task, final long delay, final long period) {
		Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period);
	}

	@Override
	public void runPlatformGlobal(final Runnable task) {
		Bukkit.getScheduler().runTask(plugin, task);
	}

	@Override
	public void runTask(final Runnable task, final Entity entity) {
		Bukkit.getScheduler().runTask(plugin, task);
	}

	@Override
	public void runTaskLater(final Runnable task, final Entity entity, final long delay) {
		Bukkit.getScheduler().runTaskLater(plugin, task, delay);
	}

	@Override
	public void runTaskTimer(final Runnable task, final long delay, final long period) {
		Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
	}

	@Override
	public void executeConsoleCommand(final String command) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
	}

	@Override
	public void executePlayerCommand(final Player player, final String command) {
		player.performCommand(command);
	}

	@Override
	public void cancelAllTasks() {
		Bukkit.getScheduler().cancelTasks(plugin);
	}

}
