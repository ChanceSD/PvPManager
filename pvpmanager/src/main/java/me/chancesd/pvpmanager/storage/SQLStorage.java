package me.chancesd.pvpmanager.storage;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.chancesd.pvpmanager.storage.fields.WorldDataFields;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import me.chancesd.sdutils.database.Database;
import me.chancesd.sdutils.database.DatabaseConfigBuilder;
import me.chancesd.sdutils.database.DatabaseConfigBuilder.DatabaseType;
import me.chancesd.sdutils.database.DatabaseFactory;
import me.chancesd.sdutils.database.Table;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.storage.converter.DisplayNameConverter;
import me.chancesd.pvpmanager.storage.fields.UserDataFields;

public class SQLStorage implements Storage {

	private Table usersTable;
	private Table worldsTable;
	private final JavaPlugin plugin;
	private final ConfigurationSection dbConfigSection;
	private final Database database;

	public SQLStorage(final PvPManager plugin) {
		this(plugin, null);
	}

	public SQLStorage(final PvPManager plugin, final DatabaseType dbType) {
		this.plugin = plugin;
		this.dbConfigSection = plugin.getConfig().getConfigurationSection("Database");
		final File sqliteFile = new File(plugin.getDataFolder(), "database.db");
		final DatabaseConfigBuilder config;
		if (dbType == null) {
			config = new DatabaseConfigBuilder(dbConfigSection, sqliteFile);
		} else {
			config = new DatabaseConfigBuilder(dbConfigSection, sqliteFile, dbType);
		}
		this.database = setupDatabase(config);
	}

	private Database setupDatabase(final DatabaseConfigBuilder config) {
		final Database db = new DatabaseFactory(plugin).registerConverter(new DisplayNameConverter()).getDatabase(config);
		usersTable = new Table("pmr_users",
				"uuid CHAR(36) NOT NULL PRIMARY KEY, name VARCHAR(16), displayname VARCHAR(255), kills INT UNSIGNED DEFAULT 0, deaths INT UNSIGNED DEFAULT 0, pvpstatus BOOLEAN DEFAULT 1, "
		                + "toggletime BIGINT DEFAULT 0, newbie BOOLEAN DEFAULT 0, newbie_timeleft BIGINT DEFAULT 0, last_seen BIGINT DEFAULT 0");
		db.registerTable(usersTable);
		worldsTable = new Table("pmr_worlds", "uuid CHAR(36) NOT NULL, name VARCHAR(255) NOT NULL PRIMARY KEY, pvp BOOLEAN DEFAULT 1, forcepvp VARCHAR(16) DEFAULT 'NONE'");
		db.registerTable(worldsTable);
		Log.infoColor(ChatColor.GREEN + "Connected to " + ChatColor.AQUA + config.getType() + ChatColor.GREEN + " database successfully");
		Log.infoColor(ChatColor.GREEN + "Players stored: " + ChatColor.GOLD + db.getRowCount(usersTable));
		return db;
	}

	@Override
	public boolean userExists(final UUID uuid) {
		return database.contains(usersTable, UserDataFields.UUID, uuid.toString());
	}

	@Override
	public Map<String, Object> getUserData(final UUID uuid) {
		return database.getRow(usersTable, UserDataFields.UUID, uuid.toString());
	}

	@Override
	public void removeUserData(final UUID uuid) {
		database.remove(usersTable, UserDataFields.UUID, uuid.toString());
	}

	@Override
	public boolean saveUserData(final UUID uuid, final Map<String, Object> userData) {
		if (userExists(uuid)) {
			return database.updateValues(usersTable, UserDataFields.UUID, uuid.toString(), userData.keySet(), userData.values());
		} else {
			return database.insertColumns(usersTable, userData.keySet(), userData.values());
		}
	}

	@Override
	public List<Map<String, Object>> getAllUserData() {
		return database.getAllRows(usersTable);
	}

	@Override
	public void increment(final String field, final UUID uuid) {
		database.update(usersTable, UserDataFields.UUID, field, uuid, field, "+1");
	}

	@Override
	public Map<String, Object> getWorldData(final String name) {
		return database.getRow(worldsTable, WorldDataFields.NAME, name);
	}

	@Override
	public boolean saveWorldData(final String name, final Map<String, Object> worldData) {
		final Map<String, Object> data = getWorldData(name);
		if (data.isEmpty()) {
			return database.insertColumns(worldsTable, worldData.keySet(), worldData.values());
		} else {
			return database.updateValues(worldsTable, WorldDataFields.NAME, name, worldData.keySet(), worldData.values());
		}
	}

	@Override
	public void shutdown() {
		database.close();
		Log.infoColor(ChatColor.RED + "Closed " + getDatabaseType() + " storage");
	}

	@Override
	public DatabaseType getDatabaseType() {
		return database.getDatabaseType();
	}

}
