package me.NoChance.PvPManager.Managers;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Database.Database;
import me.NoChance.PvPManager.Database.DatabaseConfigBuilder;
import me.NoChance.PvPManager.Database.DatabaseFactory;
import me.NoChance.PvPManager.Database.Table;
import me.NoChance.PvPManager.Settings.UserDataFields;
import me.NoChance.PvPManager.Utils.ChatUtils;
import me.NoChance.PvPManager.Utils.Log;
import net.md_5.bungee.api.ChatColor;

public class DatabaseManager {

	private final PvPManager plugin;
	private final Database database;
	private Table usersTable;

	public DatabaseManager(final PvPManager plugin) {
		this.plugin = plugin;
		this.database = setupDatabase();
		convertYMLToSQL();
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
			e.printStackTrace();
		}
		final ConfigurationSection section = users.getConfigurationSection("players");
		if (section == null) {
			Log.warning("Error converting users file, couldn't read players section");
			return;
		}

		final AtomicInteger usersConverted = new AtomicInteger();
		final Set<String> uuids = section.getKeys(false);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				if (usersConverted.get() == uuids.size()) {
					this.cancel();
					return;
				}
				Log.info("Converting database... " + usersConverted + "/" + uuids.size());
			}
		}, 0, 1000);
		for (final String id : uuids) {
			final UUID uuid = UUID.fromString(id);
			saveUserData(uuid, section.getConfigurationSection(id).getValues(false));
			usersConverted.incrementAndGet();
		}
		if (usersFile.delete()) {
			Log.info(ChatColor.GREEN + "Converted " + usersConverted + " out of " + uuids.size() + " users to the new database");
			Log.info(ChatColor.GREEN + "Database conversion finished in " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start) + " seconds");
		} else {
			Log.warning(ChatColor.RED + "Error deleting the users.yml file, please remove it or the plugin will try to convert it on every server restart");
		}
	}

	private void convert(final CommandSender sender, final File usersFile, final long start, final ConfigurationSection section) {
		final AtomicInteger usersConverted = new AtomicInteger();
		final Set<String> uuids = section.getKeys(false);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				if (usersConverted.get() == uuids.size()) {
					this.cancel();
					return;
				}
				ChatUtils.logAndSend(sender, "Converting database... " + usersConverted + "/" + uuids.size());
			}
		}, 0, 1000);
		for (final String id : uuids) {
			final UUID uuid = UUID.fromString(id);
			saveUserData(uuid, section.getConfigurationSection(id).getValues(false));
			usersConverted.incrementAndGet();
		}
		if (usersFile.delete()) {
			ChatUtils.logAndSend(sender, ChatColor.GREEN + "Converted " + usersConverted + " out of " + uuids.size() + " users to the new database");
			ChatUtils.logAndSend(sender,
			        ChatColor.GREEN + "Database conversion finished in " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start) + " seconds");
		} else {
			ChatUtils.logAndSend(sender,
			        ChatColor.RED + "Error deleting the users.yml file, please remove it or the plugin will try to convert it on every server restart");
		}
	}

	private Database setupDatabase() {
		final ConfigurationSection section = plugin.getConfig().getConfigurationSection("Database");
		final File sqliteFile = new File(plugin.getDataFolder(), "database.db");
		final DatabaseConfigBuilder config = new DatabaseConfigBuilder(section, sqliteFile);
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
			saveUserData(uuid, data);
		}
	}

	private void saveUserData(final UUID uuid, final Map<String, Object> data) {
		data.put(UserDataFields.UUID, uuid.toString());
		database.insertColumns(usersTable, data.keySet(), data.values());
	}

	public void increment(final String toUpdate, final UUID uuid) {
		database.update(usersTable, UserDataFields.UUID, toUpdate, uuid, toUpdate, "+1");
	}

	public void printTopThree() {
		final String kills = "select kills,uuid from users order by kills desc limit 3";
	}

	public void close() {
		database.close();
	}

}
