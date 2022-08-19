package me.NoChance.PvPManager.Storage;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

public class DatabaseFactory {
	private final JavaPlugin plugin;
	private final List<Converter> converters = new ArrayList<>();

	public DatabaseFactory(final JavaPlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Register a new converter.
	 *
	 * @param converter Converter to register
	 */
	public void registerConverter(final Converter converter) {
		converters.add(converter);
	}

	/**
	 * Construct a database
	 *
	 * @param builder Database configuration
	 * @return New database
	 */
	public Database getDatabase(final DatabaseConfigBuilder builder) {
		return new Database(this, builder);
	}

	protected void doConversion(final Database database) {
		for (final Converter converter : converters) {
			converter.onDatabaseLoad(database);
		}
	}

	public JavaPlugin getPlugin() {
		return plugin;
	}
}
