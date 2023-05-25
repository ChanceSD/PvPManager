package me.chancesd.pvpmanager.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import me.NoChance.PvPManager.PvPManager;

public class ScheduleUtils {

	private static final boolean FOLIA_SUPPORT = checkFolia();
	private static ScheduledExecutorService executor;
	private static final List<ScheduledFuture<?>> scheduledTasks = new ArrayList<>();
	private static SchedulerProvider provider = FOLIA_SUPPORT ? new FoliaProvider(PvPManager.getInstance())
			: new BukkitProvider(PvPManager.getInstance());

	private ScheduleUtils() {
	}

	public static void setupExecutor() {
		executor = Executors.newScheduledThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors() / 2),
				new ThreadFactoryBuilder().setNameFormat("PvPManager Worker Thread - %d").build());
	}

	public static void runAsync(final Runnable task) {
		executor.submit(task);
	}

	public static ScheduledFuture<?> runAsyncLater(final Runnable task, final long delay) {
		return executor.schedule(task, delay, TimeUnit.SECONDS);
	}

	public static void runAsyncTimer(final Runnable task, final long delay, final long period) {
		scheduledTasks.add(executor.scheduleAtFixedRate(task, delay, period, TimeUnit.SECONDS));
	}

	public static void runPlatformAsync(final Runnable task) {
		provider.runPlatformAsync(task);
	}

	public static void runPlatformAsyncTimer(final Runnable task, final long delay, final long period) {
		provider.runPlatformAsyncTimer(task, delay, period);
	}

	public static void runTask(final Runnable task, final Entity entity) {
		provider.runTask(task, entity);
	}

	public static void runTaskLater(final Runnable task, final Entity entity, final long delay) {
		provider.runTaskLater(task, entity, delay);
	}

	public static void runTaskTimer(final Runnable task, final long delay, final long period) {
		provider.runTaskTimer(task, delay, period);
	}

	public static void executeConsoleCommand(final String command) {
		provider.executeConsoleCommand(command);
	}

	public static void executePlayerCommand(final Player player, final String command) {
		provider.executePlayerCommand(player, command);
	}

	public static void cancelAllTasks() {
		scheduledTasks.forEach(scheduledTask -> scheduledTask.cancel(false));
		executor.shutdown();
		provider.cancelAllTasks();
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
