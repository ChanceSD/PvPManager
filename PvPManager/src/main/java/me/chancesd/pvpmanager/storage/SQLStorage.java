package me.chancesd.pvpmanager.storage;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Settings.UserDataFields;
import me.NoChance.PvPManager.Utils.Log;
import me.chancesd.pvpmanager.storage.DatabaseConfigBuilder.DatabaseType;

public class SQLStorage implements Storage {

	private Table usersTable;
	private final JavaPlugin plugin;
	private final File sqliteFile;
	private final ConfigurationSection dbConfigSection;
	private final Database database;

	public SQLStorage(final PvPManager plugin) {
		this(plugin, null);
	}

	public SQLStorage(final PvPManager plugin, final DatabaseType dbType) {
		this.plugin = plugin;
		this.sqliteFile = new File(plugin.getDataFolder(), "database.db");
		this.dbConfigSection = plugin.getConfig().getConfigurationSection("Database");
		final DatabaseConfigBuilder config;
		if (dbType == null) {
			config = new DatabaseConfigBuilder(dbConfigSection, sqliteFile);
		} else {
			config = new DatabaseConfigBuilder(dbConfigSection, sqliteFile, dbType);
		}
		this.database = setupDatabase(config);
	}

	private Database setupDatabase(final DatabaseConfigBuilder config) {
		final Database db = new DatabaseFactory(plugin).getDatabase(config);
		usersTable = new Table("pmr_users",
		        "uuid CHAR(36) NOT NULL PRIMARY KEY, name VARCHAR(16), displayname VARCHAR(75), kills INT UNSIGNED DEFAULT 0, deaths INT UNSIGNED DEFAULT 0, pvpstatus BOOLEAN DEFAULT 1, "
		                + "toggletime BIGINT DEFAULT 0, newbie BOOLEAN DEFAULT 0, newbie_timeleft BIGINT DEFAULT 0");
		db.registerTable(usersTable);
		Log.info("Connected to " + config.getType() + " database successfully");
		Log.info("Players stored: " + db.getRowCount(usersTable));
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
	public void saveUserData(final UUID uuid, final Map<String, Object> userData) {
		if (userExists(uuid)) {
			database.updateValues(usersTable, UserDataFields.UUID, uuid.toString(), userData.keySet(), userData.values());
		} else {
			database.insertColumns(usersTable, userData.keySet(), userData.values());
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
	public void shutdown() {
		database.close();
		Log.info("Closed " + getDatabaseType() + " storage");
	}

	@Override
	public DatabaseType getDatabaseType() {
		return database.getDatabaseType();
	}

}
