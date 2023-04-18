package me.chancesd.pvpmanager.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public class ScheduleUtils {

	private static final boolean FOLIA_SUPPORT = checkFolia();
	private static ScheduledExecutorService executor;
	private static final List<ScheduledFuture<?>> scheduledTasks = new ArrayList<>();

	public static void setupExecutor() {
		executor = Executors.newScheduledThreadPool(4);
	}

	public static void runAsync(final JavaPlugin plugin, final Runnable task) {
		if (!FOLIA_SUPPORT)
			Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
		else
			Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
	}

	public static ScheduledFuture<?> runAsyncLater(final Runnable task, final long delay) {
		return executor.schedule(task, delay, TimeUnit.SECONDS);
	}

	public static void runAsyncTimer(final JavaPlugin plugin, final Runnable task, final long delay, final long period) {
		if (!FOLIA_SUPPORT)
			Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period);
		else
			Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delay / 20, period / 20, TimeUnit.SECONDS);
	}

	public static void runAsyncTimer(final Runnable task, final long delay, final long period) {
		scheduledTasks.add(executor.scheduleAtFixedRate(task, delay, period, TimeUnit.SECONDS));
	}

	public static void runTask(final JavaPlugin plugin, final Runnable task, final Entity entity) {
		if (!FOLIA_SUPPORT)
			Bukkit.getScheduler().runTask(plugin, task);
		else
			entity.getScheduler().run(plugin, scheduledTask -> task.run(), task);
	}

	public static void runTaskLater(final JavaPlugin plugin, final Runnable task, final Entity entity, final long delay) {
		if (!FOLIA_SUPPORT)
			Bukkit.getScheduler().runTaskLater(plugin, task, delay);
		else
			entity.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), task, delay);
	}

	public static void runTaskTimer(final JavaPlugin plugin, final Runnable task, final long delay, final long period) {
		if (!FOLIA_SUPPORT)
			Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
		else
			Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delay, period);
	}

	public static void cancelAllTasks(final JavaPlugin plugin) {
		scheduledTasks.forEach(scheduledTask -> scheduledTask.cancel(false));
		executor.shutdown();
		if (!FOLIA_SUPPORT) {
			Bukkit.getScheduler().cancelTasks(plugin);
		} else {
			Bukkit.getAsyncScheduler().cancelTasks(plugin);
			Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
		}
	}

	public static boolean checkFolia() {
		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
			return true;
		} catch (final ClassNotFoundException ignored) {
			return false;
		}
	}

}
