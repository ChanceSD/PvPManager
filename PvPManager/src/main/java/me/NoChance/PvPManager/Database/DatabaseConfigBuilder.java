package me.NoChance.PvPManager.Database;

import java.io.File;

import org.bukkit.configuration.ConfigurationSection;

public class DatabaseConfigBuilder {

	protected enum DatabaseType {
		SQLITE, MYSQL
	}

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
		final String url = String.format("%s:%d", section.getString("Host"), section.getInt("Port"));
		driver("com.mysql.jdbc.Driver").type(DatabaseType.MYSQL).url(url).database(section.getString("Database")).user(section.getString("Username"))
		        .password(section.getString("Password"));
	}

	/**
	 * Construct a database based on a config section with sqlite backup,
	 * drivers auto-generated.
	 *
	 * @param section Configuration section.
	 * @param backup SQLIte file backup.
	 */
	public DatabaseConfigBuilder(final ConfigurationSection section, final File backup) {
		final DatabaseType dbType = DatabaseType.valueOf(section.getString("Type", "SQLite").toUpperCase());
		if (dbType == DatabaseType.MYSQL) {
			final ConfigurationSection mysql = section.getConfigurationSection("MySQL");
			final String url = String.format("%s:%d", mysql.getString("Host"), mysql.getInt("Port"));
			driver("com.mysql.jdbc.Driver").type(DatabaseType.MYSQL).url(url).database(mysql.getString("Database")).user(mysql.getString("Username"))
			        .password(mysql.getString("Password"));
		} else {
			driver("org.sqlite.SQLiteDataSource").type(DatabaseType.SQLITE).sqlite(backup);
		}
	}

	public DatabaseConfigBuilder driver(final String driver) {
		this.driver = driver;
		return this;
	}

	public DatabaseConfigBuilder url(final String url) {
		this.url = url;
		return this;
	}

	public DatabaseConfigBuilder database(final String database) {
		this.database = database;
		return this;
	}

	public DatabaseConfigBuilder user(final String user) {
		this.user = user;
		return this;
	}

	public DatabaseConfigBuilder password(final String password) {
		this.password = password;
		return this;
	}

	public DatabaseConfigBuilder sqlite(final File file) {
		this.file = file.getPath();
		return this;
	}

	public DatabaseConfigBuilder type(final DatabaseType type) {
		this.type = type;
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
