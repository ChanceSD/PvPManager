package me.NoChance.PvPManager.MySQL;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Database {

	private static final String MYSQL_URL_TEMPLATE = "jdbc:mysql://%s/%s";
	private static final String SQLITE_URL_TEMPLATE = "jdbc:sqlite:%s";
	private final JavaPlugin plugin;
	private boolean converted;
	private final HikariDataSource connectionPool;

	protected Database(final DatabaseFactory databaseFactory, final DatabaseConfigBuilder builder) {
		this.plugin = databaseFactory.getPlugin();
		final HikariConfig config = new HikariConfig();
		if (builder.getFile() != null) {
			// Use SQLITE
			config.setJdbcUrl(String.format(SQLITE_URL_TEMPLATE, builder.getFile()));
		} else {
			// Use MYSQL
			config.setJdbcUrl(String.format(MYSQL_URL_TEMPLATE, builder.getUrl(), builder.getDatabase()));
			config.setUsername(builder.getUser());
			config.setPassword(builder.getPassword());
		}
		connectionPool = new HikariDataSource(config);
		if (!converted) {
			databaseFactory.doConversion(this);
			this.converted = true;
		}
	}

	/**
	 * Register a table.
	 *
	 * @param table Table to register.
	 */
	public void registerTable(final Table table) {
		final Connection connection = getConnection();
		try {
			final PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table.getName() + table.getUsage());
			ps.executeUpdate();
		} catch (final SQLException e) {
			log("Failed to register table", e);
		}
	}

	/**
	 * Delete a table
	 *
	 * @param table Name of table
	 */
	public void deleteTable(final String table) {
		final Connection connection = getConnection();
		try {
			final PreparedStatement ps = connection.prepareStatement("DROP TABLE " + table);
			ps.executeUpdate();
		} catch (final SQLException e) {
			log("Failed to delete table", e);
		}
	}

	/**
	 * Rename a table
	 *
	 * @param oldName Old name
	 * @param newName New name
	 */
	public void renameTable(final String oldName, final String newName) {
		final Connection connection = getConnection();
		try {
			final PreparedStatement ps = connection.prepareStatement("RENAME " + oldName + " TO " + newName);
			ps.executeUpdate();
		} catch (final SQLException e) {
			log("Failed to rename table", e);
		}
	}

	/**
	 * Check if a certain table exists
	 *
	 * @param table Name of table
	 * @return Table exists?
	 */
	public boolean tableExists(final String table) {
		final Connection connection = getConnection();
		try {
			final DatabaseMetaData metadata = connection.getMetaData();
			final ResultSet result = metadata.getTables(null, null, table, null);
			return result.next();
		} catch (final SQLException e) {
			log("Failed to check database", e);
			return false;
		}
	}

	/**
	 * Check if a certain column in a table exists
	 *
	 * @param table Name of table
	 * @param column Name of column
	 * @return Column exists?
	 */
	public boolean columnExists(final String table, final String column) {
		final Connection connection = getConnection();
		try {
			final DatabaseMetaData metadata = connection.getMetaData();
			final ResultSet result = metadata.getColumns(null, null, table, column);
			return result.next();
		} catch (final SQLException e) {
			log("Failed to check database", e);
			return false;
		}
	}

	/**
	 * Insert new data to the database
	 *
	 * @param table Table to insert data in
	 * @param values Values to insert
	 */
	public void set(final Table table, final Object... values) {
		final Connection connection = getConnection();
		try {
			String valueCount = "";
			for (int i = 0; i < values.length; i++) {
				valueCount += "?";
				if (i < values.length - 1) {
					valueCount += ",";
				}
			}

			final PreparedStatement ps = connection.prepareStatement("INSERT INTO " + table.getName() + table.getValues() + " VALUES(" + valueCount + ");");
			for (int i = 0; i < values.length; i++) {
				ps.setObject(i + 1, values[i]);
			}

			ps.executeUpdate();
		} catch (final SQLException e) {
			log("Failed to insert data to database", e);
		}
	}

	/**
	 * Get a value from a table.
	 *
	 * @param table Table to get value from
	 * @param index Column to search with.
	 * @param toGet Value of column to look for
	 * @param value The value to search with.
	 * @return Value of found, NULL if not.
	 */
	public Object get(final Table table, final String index, final String toGet, final Object value) {
		final Connection connection = getConnection();
		try {
			final PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + table.getName() + " WHERE " + index + "=?;");
			ps.setObject(1, value);
			final ResultSet result = ps.executeQuery();
			if (result.next())
				return result.getObject(toGet);
		} catch (final SQLException e) {
			log("Failed to get data from database", e);
		}

		return null;
	}

	/**
	 * Check if a value exists in the database
	 *
	 * @param table Table to check from.
	 * @param index Index of the value
	 * @param value Value of the index
	 * @return TRUE if found, FALSE if not
	 */
	public boolean contains(final Table table, final String index, final Object value) {
		final Connection connection = getConnection();
		try {
			final PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + table.getName() + " WHERE " + index + "=?;");
			ps.setObject(1, value);
			final ResultSet result = ps.executeQuery();
			return result.next();
		} catch (final SQLException e) {
			log("Failed to check database", e);
			return false;
		}
	}

	/**
	 * Update a value in the database
	 *
	 * @param table Table to update
	 * @param index Index to search with
	 * @param toUpdate Index to update
	 * @param indexValue Value to swarch with.
	 * @param updateValue New value to update.
	 */
	public void update(final Table table, final String index, final String toUpdate, final Object indexValue, final Object updateValue) {
		final Connection connection = getConnection();
		try {
			final PreparedStatement ps = connection.prepareStatement("UPDATE " + table.getName() + " SET " + toUpdate + "=? WHERE " + index + "=?;");
			ps.setObject(1, updateValue);
			ps.setObject(2, indexValue);
			ps.executeUpdate();
		} catch (final SQLException e) {
			log("Failed to update database", e);
		}
	}

	/**
	 * Remove data from the database
	 *
	 * @param table Table to remove from.
	 * @param index Index to search with.
	 * @param value Value to search with.
	 */
	public void remove(final Table table, final String index, final Object value) {
		final Connection connection = getConnection();
		try {
			final PreparedStatement ps = connection.prepareStatement("DELETE FROM " + table.getName() + " WHERE " + index + "=?;");
			ps.setObject(1, value);
			ps.executeUpdate();
		} catch (final SQLException e) {
			log("Failed to remove from database", e);
		}
	}

	/**
	 * Returns the database connection.
	 *
	 * @return Database connection.
	 */
	public Connection getConnection() {
		try {
			return connectionPool.getConnection();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Closes the database.
	 */
	public void close() {
		connectionPool.close();
	}

	private void log(final String message, final Throwable t) {
		plugin.getLogger().log(Level.SEVERE, message, t);
	}

	public JavaPlugin getPlugin() {
		return plugin;
	}
}
