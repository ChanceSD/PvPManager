package me.chancesd.pvpmanager.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public class ScheduleUtils {

	private static final boolean FOLIA_SUPPORT = checkFolia();
	private static ScheduledExecutorService executor;
	private static final List<ScheduledFuture<?>> scheduledTasks = new ArrayList<>();
	private static SchedulerProvider provider = FOLIA_SUPPORT ? new FoliaProvider() : new BukkitProvider();

	public static void setupExecutor() {
		executor = Executors.newScheduledThreadPool(4);
	}

	public static ScheduledFuture<?> runAsyncLater(final Runnable task, final long delay) {
		return executor.schedule(task, delay, TimeUnit.SECONDS);
	}

	public static void runAsyncTimer(final Runnable task, final long delay, final long period) {
		scheduledTasks.add(executor.scheduleAtFixedRate(task, delay, period, TimeUnit.SECONDS));
	}

	public static void runAsync(final JavaPlugin plugin, final Runnable task) {
		provider.runPlatformAsync(plugin, task);
	}

	public static void runAsyncTimer(final JavaPlugin plugin, final Runnable task, final long delay, final long period) {
		provider.runPlatformAsyncTimer(plugin, task, delay, period);
	}

	public static void runTask(final JavaPlugin plugin, final Runnable task, final Entity entity) {
		provider.runTask(plugin, task, entity);
	}

	public static void runTaskLater(final JavaPlugin plugin, final Runnable task, final Entity entity, final long delay) {
		provider.runTaskLater(plugin, task, entity, delay);
	}

	public static void runTaskTimer(final JavaPlugin plugin, final Runnable task, final long delay, final long period) {
		provider.runTaskTimer(plugin, task, delay, period);
	}

	public static void dispatchCommand() {

	}

	public static void cancelAllTasks(final JavaPlugin plugin) {
		scheduledTasks.forEach(scheduledTask -> scheduledTask.cancel(false));
		executor.shutdown();
		provider.cancelAllTasks(plugin);
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
