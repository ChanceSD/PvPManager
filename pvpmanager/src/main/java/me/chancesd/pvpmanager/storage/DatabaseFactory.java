package me.chancesd.pvpmanager.storage;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Preconditions;

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
	 * @return this
	 */
	public DatabaseFactory registerConverter(final Converter converter) {
		converters.add(converter);
		return this;
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
		boolean anyConversion = false;
		for (final Converter converter : converters) {
			if (converter.onDatabaseLoad(database))
				converter.onComplete();
			if (converter.needsConversion(database))
				anyConversion = true;
		}
		if (anyConversion) {
			database.getPlugin().reloadConfig();
			final Configuration defaults = database.getPlugin().getConfig().getDefaults();
			Preconditions.checkNotNull(defaults, "Error doing plugin conversion, config defaults is null");
			database.getPlugin().getConfig().set("Database Version", defaults.getInt("Database Version"));
			database.getPlugin().saveConfig();
		}
	}

	public JavaPlugin getPlugin() {
		return plugin;
	}
}
