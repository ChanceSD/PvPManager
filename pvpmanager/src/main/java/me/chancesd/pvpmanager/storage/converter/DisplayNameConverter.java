package me.chancesd.pvpmanager.storage.converter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.ChatColor;

import me.chancesd.sdutils.database.Converter;
import me.chancesd.sdutils.database.Database;
import me.chancesd.sdutils.database.DatabaseConfigBuilder.DatabaseType;
import me.chancesd.sdutils.database.Table;
import me.chancesd.sdutils.utils.Log;

public class DisplayNameConverter implements Converter {

	@Override
	public void onComplete() {
		Log.infoColor(ChatColor.GOLD + ">> Finished database conversion to allow saving bigger display names");
	}

	@Override
	public boolean needsConversion(final Database database) {
		return database.getDatabaseType() == DatabaseType.MYSQL && database.getPlugin().getConfig().getInt("Database Version", 0) < 1;
	}

	@Override
	public Table getOldTable() {
		return new Table("pmr_users", "");
	}

	@Override
	public Table getNewTable() {
		return null;
	}

	@Override
	public void convertAll(final Database database, final ResultSet results) throws SQLException {
		Log.infoColor(ChatColor.GOLD + ">> Starting database conversion to allow saving bigger display names");
		final String columnName = "displayname";

		try (Connection connection = database.getConnection();
				Statement statement = connection.createStatement()) {
			final String alterQuery = "ALTER TABLE " + getOldTable().getName() + " MODIFY " + columnName + " VARCHAR(255)";
			statement.executeUpdate(alterQuery);
		} catch (final SQLException e) {
			Log.severe("Failed to run database conversion: " + getClass().getSimpleName(), e);
		}

	}

}