package me.chancesd.pvpmanager.storage;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import me.chancesd.pvpmanager.storage.DatabaseConfigBuilder.DatabaseType;
import me.chancesd.sdutils.utils.MCVersion;

public class Database {

	private static final String MYSQL_URL_TEMPLATE = "jdbc:mysql://%s/%s";
	private static final String SQLITE_URL_TEMPLATE = "jdbc:sqlite:%s";
	private final JavaPlugin plugin;
	private final DatabaseType databaseType;
	private final Map<String, Table> tableRegister = new HashMap<>();
	private boolean converted;
	private final HikariDataSource connectionPool;

	private static final String SQL_SELECT_ALL = "SELECT * FROM ";
	private static final String SQL_INSERT_INTO = "INSERT INTO ";
	private static final String SQL_WHERE = " WHERE ";
	private static final String SQL_VALUES = " VALUES(";

	protected Database(final DatabaseFactory databaseFactory, final DatabaseConfigBuilder builder) {
		this.plugin = databaseFactory.getPlugin();
		this.databaseType = builder.getType();
		final HikariConfig config = new HikariConfig();
		if (databaseType == DatabaseType.SQLITE) {
			// Use SQLITE
			if (MCVersion.isLowerThan(MCVersion.V1_9)) {
				try {
					Class.forName("org.sqlite.JDBC"); // got to do this for 1.8 sigh
					config.setConnectionTestQuery("SELECT 1;");
				} catch (final ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			config.setJdbcUrl(String.format(SQLITE_URL_TEMPLATE, builder.getFile()));
			if (MCVersion.isAtLeast(MCVersion.V1_9)) {
				config.addDataSourceProperty("journal_mode", "wal");
			}
			config.addDataSourceProperty("synchronous", "normal");
		} else {
			// Use MYSQL
			config.setJdbcUrl(String.format(MYSQL_URL_TEMPLATE, builder.getUrl(), builder.getDatabase()));
			config.setUsername(builder.getUser());
			config.setPassword(builder.getPassword());
		}
		config.setPoolName(plugin.getName());
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		setLogLevel(Level.WARN);
		this.connectionPool = new HikariDataSource(config);
		setLogLevel(Level.INFO);
		if (!converted) {
			databaseFactory.doConversion(this);
			this.converted = true;
		}
	}

	public <T> void doQuery(final String sql, final Consumer<T> consumer, final String field, final Class<T> type) {
		CompletableFuture.runAsync(() -> {
			try (Connection connection = getConnection();
					PreparedStatement ps = connection.prepareStatement(sql);
					ResultSet result = ps.executeQuery()) {
				if (result.next()) {
					final T value = result.getObject(field, type);
					consumer.accept(value);
				}
			} catch (final SQLException e) {
				log("Failed to do query", e);
			}
		});
	}

	/**
	 * Register a table.
	 *
	 * @param table Table to register.
	 */
	public void registerTable(final Table table) {
		try (Connection connection = getConnection();
				PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table.getName() + table.getUsage())) {
			ps.executeUpdate();
			tableRegister.put(table.getName(), table);
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
		try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement("DROP TABLE " + table)) {
			ps.executeUpdate();
			tableRegister.remove(table);
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
		try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement("RENAME " + oldName + " TO " + newName)) {
			ps.executeUpdate();
			final Table table = tableRegister.get(oldName);
			if (table != null) {
				tableRegister.remove(oldName);
				tableRegister.put(newName, table);
			}
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
			try (ResultSet result = metadata.getTables(null, null, table, null)) {
				return result.next();
			}
		} catch (final SQLException e) {
			log("Failed to check if table exists", e);
			return false;
		}
	}

	/**
	 * Check if a certain column in a table exists
	 *
	 * @param table  Name of table
	 * @param column Name of column
	 * @return Column exists?
	 */
	public boolean columnExists(final String table, final String column) {
		try (Connection connection = getConnection()) {
			final DatabaseMetaData metadata = connection.getMetaData();
			try (ResultSet result = metadata.getColumns(null, null, table, column)) {
				return result.next();
			}
		} catch (final SQLException e) {
			log("Failed to check database if column exists", e);
			return false;
		}
	}

	/**
	 * Insert new data to the database
	 *
	 * @param table  Table to insert data in
	 * @param values Values to insert
	 */
	public void insertDefault(final Table table, final Object... values) {
		try (Connection connection = getConnection()) {
			final StringBuilder valueCount = getValueParameteres(Arrays.asList(values));
			try (PreparedStatement ps = connection.prepareStatement(SQL_INSERT_INTO + table.getName() + SQL_VALUES + valueCount + ");")) {
				for (int i = 0; i < values.length; i++) {
					ps.setObject(i + 1, values[i]);
				}

				ps.executeUpdate();
			}
		} catch (final SQLException e) {
			log("Failed to insert data to database", e);
		}
	}

	/**
	 * Insert new data to the database
	 *
	 * @param table   Table to insert data in
	 * @param columns Collection of column names
	 * @param values  Values to insert
	 */
	public boolean insertColumns(final Table table, final Collection<String> columns, final Collection<Object> values) {
		try (Connection connection = getConnection()) {
			final StringBuilder valueCount = getValueParameteres(values);
			final StringBuilder columnList = new StringBuilder("(");
			int index = 0;
			for (final String col : columns) {
				columnList.append(col);
				index++;
				if (index < columns.size()) {
					columnList.append(",");
				}
			}
			columnList.append(")");
			try (PreparedStatement ps = connection.prepareStatement(SQL_INSERT_INTO + table.getName() + columnList + SQL_VALUES + valueCount + ");")) {
				int i = 0;
				for (final Object object : values) {
					ps.setObject(++i, object);
				}

				ps.executeUpdate();
			}
			return true;
		} catch (final SQLException e) {
			log("Failed to insert data to database", e);
			return false;
		}
	}

	/**
	 * Insert new data to the database
	 *
	 * All the collections of values must have the same size and the same object order
	 *
	 * @param table   Table to insert data in
	 * @param columns Collection of column names
	 * @param values  Collection of values to insert
	 */
	public void insertColumnsBatch(final Table table, final Collection<String> columns, final Collection<Collection<Object>> values) {
		try (Connection connection = getConnection()) {
			final Collection<Object> collection = values.stream().findFirst().orElse(Collections.emptyList());
			final StringBuilder valueCount = getValueParameteres(collection);
			final StringBuilder columnList = new StringBuilder("(");
			int index = 0;
			for (final String col : columns) {
				columnList.append(col);
				index++;
				if (index < columns.size()) {
					columnList.append(",");
				}
			}
			columnList.append(")");
			try (PreparedStatement ps = connection.prepareStatement(SQL_INSERT_INTO + table.getName() + columnList + SQL_VALUES + valueCount + ");")) {
				int inserts = 0;
				for (final Collection<Object> object : values) {
					int i = 0;
					for (final Object value : object) {
						ps.setObject(++i, value);
					}
					ps.addBatch();
					inserts++;
					if (inserts % 1000 == 0 || inserts == values.size()) {
						ps.executeBatch();
					}
				}
			}
		} catch (final SQLException e) {
			log("Failed to insert batch data to database", e);
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
		try (Connection connection = getConnection();
				final PreparedStatement ps = connection.prepareStatement(SQL_SELECT_ALL + table.getName() + SQL_WHERE + index + "=?")) {
			ps.setObject(1, value);
			try (ResultSet result = ps.executeQuery()) {
				if (result.next())
					return result.getObject(toGet);
			}
		} catch (final SQLException e) {
			log("Failed to get value from database", e);
		}

		return null;
	}

	/**
	 * Get a ResultSet from a table.
	 *
	 * @param table Table to get value from
	 * @param index Column to search with.
	 * @param value The value to search with.
	 * @return Value of found, empty map if not.
	 */
	public Map<String, Object> getRow(final Table table, final String index, final Object value) {
		try (Connection connection = getConnection();
				final PreparedStatement ps = connection.prepareStatement(SQL_SELECT_ALL + table.getName() + SQL_WHERE + index + "=?")) {
			ps.setObject(1, value);
			try (final ResultSet result = ps.executeQuery()) {
				if (result.next()) {
					final ResultSetMetaData metaData = result.getMetaData();
					final Map<String, Object> row = new HashMap<>();
					for (int i = 1; i <= metaData.getColumnCount(); i++) {
						row.put(metaData.getColumnName(i), result.getObject(i));
					}
					return row;
				}
			}
		} catch (final SQLException e) {
			log("Failed to get data from database", e);
		}

		return Collections.emptyMap();
	}

	/**
	 * Get a ResultSet from a table.
	 *
	 * @param table Table to get values from
	 * @return All rows found, empty map if none.
	 */
	public List<Map<String, Object>> getAllRows(final Table table) {
		try (Connection connection = getConnection();
				final PreparedStatement ps = connection.prepareStatement(SQL_SELECT_ALL + table.getName() + ";")) {
			try (final ResultSet result = ps.executeQuery()) {
				final List<Map<String, Object>> rows = new ArrayList<>();
				while (result.next()) {
					final ResultSetMetaData metaData = result.getMetaData();
					final Map<String, Object> row = new HashMap<>();
					for (int i = 1; i <= metaData.getColumnCount(); i++) {
						row.put(metaData.getColumnName(i), result.getObject(i));
					}
					rows.add(row);
				}
				return rows;
			}
		} catch (final SQLException e) {
			log("Failed to get data from database", e);
		}
		return Collections.emptyList();
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
				PreparedStatement ps = connection.prepareStatement(SQL_SELECT_ALL + table.getName() + SQL_WHERE + index + "=?")) {
			ps.setObject(1, value);
			try (ResultSet result = ps.executeQuery()) {
				return result.next();
			}
		} catch (final SQLException e) {
			log("Failed to check database", e);
			return false;
		}
	}

	/**
	 * Update a value in the database
	 *
	 * @param table       Table to update
	 * @param index       Index to search with
	 * @param toUpdate    Index to update
	 * @param indexValue  Value to search with.
	 * @param updateValue New value to update.
	 * @param extra       Extra such as increment or decrement.
	 */
	public void update(final Table table, final String index, final String toUpdate, final Object indexValue, final Object updateValue,
			final String extra) {
		try (Connection connection = getConnection()) {
			final String update = extra.isEmpty() ? "?" : updateValue + extra;
			try (PreparedStatement ps = connection
					.prepareStatement("UPDATE " + table.getName() + " SET " + toUpdate + "=" + update + SQL_WHERE + index + "=?;")) {
				if (extra.isEmpty()) {
					ps.setObject(1, updateValue);
					ps.setObject(2, indexValue);
				} else {
					ps.setObject(1, indexValue);
				}
				ps.executeUpdate();
			}
		} catch (final SQLException e) {
			log("Failed to update database", e);
		}
	}

	/**
	 * Update a value in the database
	 *
	 * @param table       Table to update
	 * @param index       Index to search with
	 * @param toUpdate    Index to update
	 * @param indexValue  Value to search with.
	 * @param updateValue New value to update.
	 */
	public void update(final Table table, final String index, final String toUpdate, final Object indexValue, final Object updateValue) {
		update(table, index, toUpdate, indexValue, updateValue, "");
	}

	/**
	 * Update multiple values in the database
	 *
	 * @param table      Table to update
	 * @param index      Index to search with
	 * @param indexValue Value to search with.
	 * @param columns    Columns to update
	 * @param values     New values to update
	 */
	public boolean updateValues(final Table table, final String index, final Object indexValue, final Collection<String> columns,
			final Collection<Object> values) {
		try (Connection connection = getConnection()) {
			final StringBuilder updateString = new StringBuilder();
			int i = 0;
			for (final String col : columns) {
				updateString.append(col + "=?");
				i++;
				if (i < columns.size()) {
					updateString.append(",");
				}
			}
			try (PreparedStatement ps = connection
					.prepareStatement("UPDATE " + table.getName() + " SET " + updateString + SQL_WHERE + index + "=?;")) {
				i = 0;
				for (final Object object : values) {
					ps.setObject(++i, object);
				}
				ps.setObject(values.size() + 1, indexValue);
				ps.executeUpdate();
			}
			return true;
		} catch (final SQLException e) {
			log("Failed to update database", e);
			return false;
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
		try (Connection connection = getConnection();
				PreparedStatement ps = connection.prepareStatement("DELETE FROM " + table.getName() + SQL_WHERE + index + "=?;")) {
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
			log("Failed to get row count", e);
		}
		return 0;
	}

	@Nullable
	public Table getTable(final String tableName) {
		return tableRegister.get(tableName);
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
			log("Error getting database connection", e);
		}
		return null;
	}

	/**
	 * Closes the database.
	 */
	public void close() {
		setLogLevel(Level.WARN);
		connectionPool.close();
		setLogLevel(Level.INFO);
	}

	public DatabaseType getDatabaseType() {
		return databaseType;
	}

	private void setLogLevel(final Level level) {
		@SuppressWarnings("resource")
		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.getConfiguration().getLoggerConfig("com.zaxxer.hikari.HikariDataSource").setLevel(level);
	}

	private StringBuilder getValueParameteres(final Collection<Object> values) {
		final StringBuilder valueParams = new StringBuilder();
		for (int i = 0; i < values.size(); i++) {
			valueParams.append("?");
			if (i < values.size() - 1) {
				valueParams.append(",");
			}
		}
		return valueParams;
	}

	private void log(final String message, final Throwable t) {
		plugin.getLogger().log(java.util.logging.Level.SEVERE, message, t);
	}

	public JavaPlugin getPlugin() {
		return plugin;
	}
}
