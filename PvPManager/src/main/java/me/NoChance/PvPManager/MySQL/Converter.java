package me.NoChance.PvPManager.MySQL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public abstract class Converter {

	void onDatabaseLoad(final Database database) {
		try {
			if (needsConversion(database)) {
				final Table oldTable = getOldTable();

				//Load entries
				final PreparedStatement ps = database.getConnection().prepareStatement("SELECT * FROM " + oldTable.getName());
				final ResultSet entries = ps.executeQuery();

				//Convert
				convertAll(database, entries);
			}
		} catch (final SQLException e) {
			database.getPlugin().getLogger().log(Level.WARNING, "Failed to convert database", e);
		}
	}

	public void onComplete() {
	}

	/**
	 * Does this database need conversion from this converter?
	 *
	 * @param database Selected database
	 * @return Needs conversion?
	 * @throws SQLException
	 */
	public abstract boolean needsConversion(Database database) throws SQLException;

	/**
	 * Old table format.
	 *
	 * @return Table
	 */
	public abstract Table getOldTable();

	/**
	 * New table format.
	 *
	 * @return Table
	 */
	public abstract Table getNewTable();

	/**
	 * Convert all loaded entries
	 *
	 * @param database Selected database
	 * @param results Loaded entries
	 * @throws SQLException
	 */
	public abstract void convertAll(Database database, ResultSet results) throws SQLException;
}
