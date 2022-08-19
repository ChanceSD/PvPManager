package me.NoChance.PvPManager.Managers;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.MySQL.Database;
import me.NoChance.PvPManager.MySQL.DatabaseConfigBuilder;
import me.NoChance.PvPManager.MySQL.DatabaseFactory;
import me.NoChance.PvPManager.MySQL.Table;

public class DatabaseManager {

	private final PvPManager plugin;
	private final Database database;

	public DatabaseManager(final PvPManager plugin) {
		this.plugin = plugin;
		this.database = setupDatabase();
	}

	private Database setupDatabase() {
		final ConfigurationSection section = plugin.getConfig().getConfigurationSection("Database");
		final File sqliteFile = new File(plugin.getDataFolder(), "database.db");
		final DatabaseConfigBuilder config = new DatabaseConfigBuilder(section, sqliteFile);
		final Database db = new DatabaseFactory(plugin).getDatabase(config);
		database.registerTable(new Table("users", "uuid CHAR(36) NOT NULL PRIMARY KEY, kills INT UNSIGNED DEFAULT 0, deaths INT UNSIGNED DEFAULT 0"));
		return db;
	}

	public void addPlayerEntry(final String id) {
		new BukkitRunnable() {
			@Override
			public void run() {
				final Connection connection = getConnection();
				try {
					final PreparedStatement ps = connection.prepareStatement("INSERT INTO users (uuid) VALUES(?) ON DUPLICATE KEY UPDATE uuid=uuid;");
					ps.setString(1, id);
					ps.executeUpdate();
				} catch (final SQLException e) {
					log("Failed to insert data to database", e);
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	public void increment(final String toUpdate, final String id) {
		new BukkitRunnable() {
			@Override
			public void run() {
				final Connection connection = getConnection();
				try {
					final PreparedStatement ps = connection.prepareStatement("UPDATE users SET " + toUpdate + "=" + toUpdate + "+ 1 WHERE uuid = ?;");
					ps.setObject(1, id);
					ps.executeUpdate();
				} catch (final SQLException e) {
					log("Failed to update database", e);
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	public void printTopThree() {
		final Connection connection = getConnection();
		try {
			final PreparedStatement ps = connection.prepareStatement("select kills,uuid from users order by kills desc limit 3");
			final ResultSet r = ps.executeQuery();
			while (r.next()) {
				System.out.println(r.getInt("kills"));
				System.out.println(r.getString("uuid"));
			}
		} catch (final SQLException e) {
			log("Failed to insert data to database", e);
		}
	}

}
