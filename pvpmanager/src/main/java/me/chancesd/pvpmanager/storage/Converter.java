package me.chancesd.pvpmanager.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public interface Converter {

	default boolean onDatabaseLoad(final Database database) {
		try (Connection connection = database.getConnection()) {
			if (needsConversion(database)) {
				final Table oldTable = getOldTable();
				// Load entries
				try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + oldTable.getName());
						ResultSet entries = ps.executeQuery()) {
					// Convert
					convertAll(database, entries);
				}
				return true;
			}
		} catch (final SQLException e) {
			database.getPlugin().getLogger().log(Level.WARNING, "Failed to convert database", e);
		}
		return false;
	}

	public void onComplete();

	/**
	 * Does this database need conversion from this converter?
	 *
	 * @param database Selected database
	 * @return Needs conversion?
	 */
	public boolean needsConversion(Database database);

	/**
	 * Old table format.
	 *
	 * @return Table
	 */
	public Table getOldTable();

	/**
	 * New table format.
	 *
	 * @return Table
	 */
	public Table getNewTable();

	/**
	 * Convert all loaded entries
	 *
	 * @param database Selected database
	 * @param results  Loaded entries
	 * @throws SQLException
	 */
	public void convertAll(Database database, ResultSet results) throws SQLException;
}
