package me.NoChance.PvPManager.MySQL;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
		config.setPoolName("PvPManager");
//		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
//		ctx.getConfiguration().getLoggerConfig("com.zaxxer.hikari.HikariDataSource").setLevel(Level.WARN);
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
		try (Connection connection = getConnection()) {
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
		try (Connection connection = getConnection()) {
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
		try (Connection connection = getConnection()) {
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
		try (Connection connection = getConnection()) {
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
		try (Connection connection = getConnection()) {
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
	public void insertDefault(final Table table, final Object... values) {
		try (Connection connection = getConnection()) {
			String valueCount = "";
			for (int i = 0; i < values.length; i++) {
				valueCount += "?";
				if (i < values.length - 1) {
					valueCount += ",";
				}
			}
			final PreparedStatement ps = connection.prepareStatement("INSERT INTO " + table.getName() + " VALUES(" + valueCount + ");");
			for (int i = 0; i < values.length; i++) {
				ps.setObject(i + 1, values[i]);
			}

			ps.executeUpdate();
		} catch (final SQLException e) {
			log("Failed to insert data to database", e);
		}
	}

	/**
	 * Insert new data to the database
	 *
	 * @param table Table to insert data in
	 * @param columns Collection of column names
	 * @param values Values to insert
	 */
	public void insertColumns(final Table table, final Collection<String> columns, final Collection<Object> values) {
		try (Connection connection = getConnection()) {
			String valueCount = "";
			for (int i = 0; i < values.size(); i++) {
				valueCount += "?";
				if (i < values.size() - 1) {
					valueCount += ",";
				}
			}
			String columnList = "(";
			int index = 0;
			for (final String col : columns) {
				columnList += col;
				index++;
				if (index < columns.size()) {
					columnList += ",";
				}
			}
			columnList += ")";
			final PreparedStatement ps = connection.prepareStatement("INSERT INTO " + table.getName() + columnList + " VALUES(" + valueCount + ");");
			int i = 0;
			for (final Object object : values) {
				ps.setObject(++i, object);
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
	public Object getValue(final Table table, final String index, final String toGet, final Object value) {
		try (Connection connection = getConnection()) {
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
	 * Get a ResultSet from a table.
	 *
	 * @param table Table to get value from
	 * @param index Column to search with.
	 * @param value The value to search with.
	 * @return Value of found, NULL if not.
	 */
	public Map<String, Object> getRow(final Table table, final String index, final Object value) {
		try (Connection connection = getConnection()) {
			final PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + table.getName() + " WHERE " + index + "=?;");
			ps.setObject(1, value);
			final ResultSet result = ps.executeQuery();
			if (result.next()) {
				final ResultSetMetaData metaData = result.getMetaData();
				final Map<String, Object> row = new HashMap<>();
				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					row.put(metaData.getColumnName(i), result.getObject(i));
				}
				return row;
			}
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
		try (Connection connection = getConnection();
		        PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + table.getName() + " WHERE " + index + "=?;")) {
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
	 * @param indexValue Value to search with.
	 * @param updateValue New value to update.
	 * @param extra Extra such as increment or decrement.
	 */
	public void update(final Table table, final String index, final String toUpdate, final Object indexValue, final Object updateValue, final String extra) {
		try (Connection connection = getConnection()) {
			final String update = extra.isEmpty() ? "?" : updateValue + extra;
			final PreparedStatement ps = connection.prepareStatement("UPDATE " + table.getName() + " SET " + toUpdate + "=" + update + " WHERE " + index + "=?;");
			if (extra.isEmpty()) {
				ps.setObject(1, updateValue);
				ps.setObject(2, indexValue);
			} else {
				ps.setObject(1, indexValue);
			}
			ps.executeUpdate();
		} catch (final SQLException e) {
			log("Failed to update database", e);
		}
	}

	/**
	 * Update a value in the database
	 *
	 * @param table Table to update
	 * @param index Index to search with
	 * @param toUpdate Index to update
	 * @param indexValue Value to search with.
	 * @param updateValue New value to update.
	 */
	public void update(final Table table, final String index, final String toUpdate, final Object indexValue, final Object updateValue) {
		update(table, index, toUpdate, indexValue, updateValue, "");
	}

	/**
	 * Update multiple value in the database
	 *
	 * @param table Table to update
	 * @param index Index to search with
	 * @param indexValue Value to search with.
	 * @param columns Columns to update
	 * @param values New values to update
	 */
	public void updateValues(final Table table, final String index, final Object indexValue, final Collection<String> columns, final Collection<Object> values) {
		try (Connection connection = getConnection()) {
			String updateString = "";
			int i = 0;
			for (final String col : columns) {
				updateString += col + "=?";
				i++;
				if (i < columns.size()) {
					updateString += ",";
				}
			}
			final PreparedStatement ps = connection.prepareStatement("UPDATE " + table.getName() + " SET " + updateString + " WHERE " + index + "=?;");
			i = 0;
			for (final Object object : values) {
				ps.setObject(++i, object);
			}
			ps.setObject(values.size() + 1, indexValue);
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
		try (Connection connection = getConnection()) {
			final PreparedStatement ps = connection.prepareStatement("DELETE FROM " + table.getName() + " WHERE " + index + "=?;");
			ps.setObject(1, value);
			ps.executeUpdate();
		} catch (final SQLException e) {
			log("Failed to remove from database", e);
		}
	}

	public int getRowCount(final Table table) {
		try (final Connection connection = getConnection();
		        final PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM " + table.getName());
		        final ResultSet result = ps.executeQuery()) {
			if (result.next())
				return result.getInt(1);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return 0;
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
		plugin.getLogger().log(java.util.logging.Level.SEVERE, message, t);
	}

	public JavaPlugin getPlugin() {
		return plugin;
	}
}
