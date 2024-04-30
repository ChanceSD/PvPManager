package me.chancesd.pvpmanager.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import me.NoChance.PvPManager.PvPManager;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.pvpmanager.storage.DatabaseConfigBuilder.DatabaseType;
import me.chancesd.pvpmanager.storage.SQLStorage;
import me.chancesd.pvpmanager.storage.Storage;
import me.chancesd.pvpmanager.storage.fields.UserDataFields;
import me.chancesd.pvpmanager.tasks.StorageSaveTask;
import me.chancesd.pvpmanager.utils.ChatUtils;
import me.chancesd.pvpmanager.utils.ScheduleUtils;
import net.md_5.bungee.api.ChatColor;

public class StorageManager {

	private final PvPManager plugin;
	private final Storage storage;
	private final ScheduledFuture<?> saveTask;

	public StorageManager(final PvPManager plugin) {
		this.plugin = plugin;
		this.storage = new SQLStorage(plugin);
		convertYMLToSQL();
		saveTask = ScheduleUtils.runAsyncTimer(new StorageSaveTask(plugin, storage), 300, 300, TimeUnit.SECONDS);
	}

	public void shutdown() {
		saveTask.cancel(false);
		storage.shutdown();
	}

	public Storage getStorage() {
		return storage;
	}

	public void convertFromCurrent(final DatabaseType dbType, @NotNull final CommandSender sender, final long start) {
		final Storage destinationStorage = new SQLStorage(plugin, dbType);
		convertTo(destinationStorage, storage.getAllUserData(), sender, start);
		destinationStorage.shutdown();
	}

	private void convertYMLToSQL() {
		final File usersFile = new File(plugin.getDataFolder(), "users.yml");
		if (!usersFile.exists())
			return;

		final long start = System.currentTimeMillis();
		Log.info("Converting users.yml file to SQL database");
		final YamlConfiguration users = new YamlConfiguration();
		try {
			users.load(usersFile);
		} catch (IOException | InvalidConfigurationException e) {
			Log.severe("Error converting to SQL, can't load users file", e);
		}

		final ConfigurationSection section = users.getConfigurationSection("players");
		if (section == null) {
			Log.warning("Error converting users file, couldn't read players section");
			return;
		}

		final List<Map<String, Object>> rows = new ArrayList<>();
		final Map<String, Object> keys = section.getValues(false);
		for (final Entry<String, Object> entry : keys.entrySet()) {
			final ConfigurationSection entrySection = (ConfigurationSection) entry.getValue();
			final Map<String, Object> values = entrySection.getValues(false);
			values.put(UserDataFields.UUID, entry.getKey());
			rows.add(values);
		}

		convertTo(storage, rows, Bukkit.getConsoleSender(), start);
		try {
			Files.delete(usersFile.toPath());
		} catch (final IOException e) {
			Log.warning("Error deleting the users.yml file, please remove it or the plugin will try to convert it on every server restart", e);
		}
	}

	private void convertTo(final Storage destination, final List<Map<String, Object>> allRows, @NotNull final CommandSender sender, final long start) {
		final AtomicBoolean conversionFinished = new AtomicBoolean();
		final AtomicInteger usersConverted = new AtomicInteger();
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				if (conversionFinished.get()) {
					this.cancel();
					return;
				}
				ChatUtils.send(sender, ChatColor.GOLD + "Converting database... " + usersConverted + "/" + allRows.size());
			}
		}, 0, 1000);

		for (final Map<String, Object> row : allRows) {
			final UUID uuid = UUID.fromString((String) row.get(UserDataFields.UUID));
			if (destination.saveUserData(uuid, row))
				usersConverted.incrementAndGet();
		}

		conversionFinished.set(true);
		ChatUtils.send(sender, ChatColor.GOLD + "Converted " + usersConverted + " out of " + allRows.size() + " users to the new database");
		ChatUtils.send(sender,
		        ChatColor.DARK_GREEN + "Database conversion finished in " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start) + " seconds");
	}

}
