package me.chancesd.pvpmanager.storage;

import java.io.File;

import org.bukkit.configuration.ConfigurationSection;

import me.chancesd.sdutils.utils.Log;

public class DatabaseConfigBuilder {

	public enum DatabaseType {
		SQLITE, MYSQL;// MARIADB; spigot/paper dont have the driver for this and don't want to shade it for now
	}

	private static final String DATABASE_FIELD = "Database";
	private static final String USERNAME_FIELD = "Username";
	private static final String PASSWORD_FIELD = "Password";
	private String driver;
	private String url;
	private String database;
	private String user;
	private String password;
	private String file;
	private DatabaseType type;

	/**
	 * Default constructor, no settings.
	 */
	public DatabaseConfigBuilder() {
	}

	/**
	 * Construct a database based on a config section with a custom url.
	 *
	 * @param section Configuration section.
	 */
	public DatabaseConfigBuilder(final ConfigurationSection section) {
		final DatabaseType dbType = getDBTypeFrom(section);
		final String newURL = String.format("%s:%d", section.getString("Host"), section.getInt("Port"));

		if (dbType == DatabaseType.MYSQL) {
			driver("com.mysql.jdbc.Driver").type(DatabaseType.MYSQL).url(newURL).database(section.getString(DATABASE_FIELD))
					.user(section.getString(USERNAME_FIELD))
					.password(section.getString(PASSWORD_FIELD));
		}
	}

	/**
	 * Construct a database based on a config section with sqlite backup,
	 * drivers auto-generated.
	 *
	 * @param section Configuration section.
	 * @param backup  SQLite file backup.
	 */
	public DatabaseConfigBuilder(final ConfigurationSection section, final File backup) {
		this(section, backup, getDBTypeFrom(section));
	}

	/**
	 * Construct a database based on a config section with sqlite backup,
	 * drivers auto-generated.
	 *
	 * @param section Configuration section.
	 * @param backup  SQLIte file backup.
	 * @param dbType  Type of database
	 */
	public DatabaseConfigBuilder(final ConfigurationSection section, final File backup, final DatabaseType dbType) {
		if (dbType == DatabaseType.MYSQL) {
			final ConfigurationSection mysql = section.getConfigurationSection("MySQL");
			if (mysql == null)
				throw new IllegalArgumentException("The MySQL config section is missing");
			final String newURL = String.format("%s:%d", mysql.getString("Host"), mysql.getInt("Port"));
			driver("com.mysql.jdbc.Driver").type(DatabaseType.MYSQL).url(newURL).database(mysql.getString(DATABASE_FIELD))
					.user(mysql.getString(USERNAME_FIELD))
					.password(mysql.getString(PASSWORD_FIELD));
		} else {
			driver("org.sqlite.SQLiteDataSource").type(DatabaseType.SQLITE).sqlite(backup);
		}
	}

	private static DatabaseType getDBTypeFrom(final ConfigurationSection section) {
		@SuppressWarnings("null")
		final String dbType = section.getString("Type", "SQLite").toUpperCase();
		DatabaseType databaseType;
		try {
			databaseType = DatabaseType.valueOf(dbType);
		} catch (final IllegalArgumentException e) {
			Log.severe("The database type you specified in the config does not exist: " + dbType + ". Defaulting to SQLite");
			databaseType = DatabaseType.SQLITE;
		}
		return databaseType;
	}

	public DatabaseConfigBuilder driver(final String newDriver) {
		this.driver = newDriver;
		return this;
	}

	public DatabaseConfigBuilder url(final String newURL) {
		this.url = newURL;
		return this;
	}

	public DatabaseConfigBuilder database(final String newDatabase) {
		this.database = newDatabase;
		return this;
	}

	public DatabaseConfigBuilder user(final String newUser) {
		this.user = newUser;
		return this;
	}

	public DatabaseConfigBuilder password(final String newPassword) {
		this.password = newPassword;
		return this;
	}

	public DatabaseConfigBuilder sqlite(final File newFile) {
		this.file = newFile.getPath();
		return this;
	}

	public DatabaseConfigBuilder type(final DatabaseType newType) {
		this.type = newType;
		return this;
	}

	public String getFile() {
		return file;
	}

	public String getDriver() {
		return driver;
	}

	public String getUrl() {
		return url;
	}

	public String getDatabase() {
		return database;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public DatabaseType getType() {
		return type;
	}
}
