package me.NoChance.PvPManager.Managers;

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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Settings.UserDataFields;
import me.NoChance.PvPManager.Storage.Database;
import me.NoChance.PvPManager.Storage.DatabaseConfigBuilder;
import me.NoChance.PvPManager.Storage.DatabaseConfigBuilder.DatabaseType;
import me.NoChance.PvPManager.Storage.DatabaseFactory;
import me.NoChance.PvPManager.Storage.Table;
import me.NoChance.PvPManager.Utils.ChatUtils;
import me.NoChance.PvPManager.Utils.Log;
import net.md_5.bungee.api.ChatColor;

public class DatabaseManager {

	private final PvPManager plugin;
	private final Database database;
	private Table usersTable;
	private final File sqliteFile;
	private final ConfigurationSection dbConfigSection;

	public DatabaseManager(final PvPManager plugin) {
		this.plugin = plugin;
		this.sqliteFile = new File(plugin.getDataFolder(), "database.db");
		this.dbConfigSection = plugin.getConfig().getConfigurationSection("Database");
		this.database = setupDatabase();
		convertYMLToSQL();
		Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> plugin.getPlayerHandler().getPlayers().values().forEach(this::saveUser), 600, 600);
	}

	private Database setupDatabase() {
		final DatabaseConfigBuilder config = new DatabaseConfigBuilder(dbConfigSection, sqliteFile);
		final Database db = new DatabaseFactory(plugin).getDatabase(config);
		usersTable = new Table("pmr_users",
		        "uuid CHAR(36) NOT NULL PRIMARY KEY, name VARCHAR(16), displayname VARCHAR(75), kills INT UNSIGNED DEFAULT 0, deaths INT UNSIGNED DEFAULT 0, pvpstatus BOOLEAN DEFAULT 1, "
		                + "toggletime BIGINT DEFAULT 0, newbie BOOLEAN DEFAULT 0, newbie_timeleft BIGINT DEFAULT 0");
		db.registerTable(usersTable);
		Log.info("Connected to " + config.getType() + " database successfully");
		Log.info("Players stored: " + db.getRowCount(usersTable));
		return db;
	}

	public Map<String, Object> getUserData(final UUID uuid) {
		return database.getRow(usersTable, UserDataFields.UUID, uuid.toString());
	}

	public boolean userExists(final UUID uuid) {
		return database.contains(usersTable, UserDataFields.UUID, uuid.toString());
	}

	public void saveUser(final PvPlayer player) {
		final UUID uuid = player.getUUID();
		final Map<String, Object> data = player.getUserData();
		if (userExists(uuid)) {
			database.updateValues(usersTable, UserDataFields.UUID, uuid.toString(), data.keySet(), data.values());
		} else {
			database.insertColumns(usersTable, data.keySet(), data.values());
		}
	}

	public void increment(final String toUpdate, final UUID uuid) {
		database.update(usersTable, UserDataFields.UUID, toUpdate, uuid, toUpdate, "+1");
	}

	public void shutdown() {
		database.close();
	}

	public DatabaseType getDatabaseType() {
		return database.getDatabaseType();
	}

	public void convertFromCurrent(final DatabaseType dbType, final CommandSender sender, final long start) {
		final DatabaseConfigBuilder config = new DatabaseConfigBuilder(dbConfigSection, sqliteFile, dbType);
		final Database destinationDb = new DatabaseFactory(plugin).getDatabase(config);
		convertTo(destinationDb, database.getAllRows(usersTable), sender, start);
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

		convertTo(database, rows, null, start);
		try {
			Files.delete(usersFile.toPath());
		} catch (final IOException e) {
			Log.warning("Error deleting the users.yml file, please remove it or the plugin will try to convert it on every server restart", e);
		}
	}

	private void convertTo(final Database destination, final List<Map<String, Object>> allRows, final CommandSender sender, final long start) {
		final AtomicInteger usersConverted = new AtomicInteger();
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				if (usersConverted.get() == allRows.size()) {
					this.cancel();
					return;
				}
				ChatUtils.logAndSend(sender, "Converting database... " + usersConverted + "/" + allRows.size());
			}
		}, 0, 1000);
		for (final Map<String, Object> row : allRows) {
			destination.insertColumns(usersTable, row.keySet(), row.values());
			usersConverted.incrementAndGet();
		}

		ChatUtils.logAndSend(sender, ChatColor.GREEN + "Converted " + usersConverted + " out of " + allRows.size() + " users to the new database");
		ChatUtils.logAndSend(sender,
		        ChatColor.GREEN + "Database conversion finished in " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start) + " seconds");
	}

}
