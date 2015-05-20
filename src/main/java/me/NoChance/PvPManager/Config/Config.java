package me.NoChance.PvPManager.Config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

import me.NoChance.PvPManager.PvPManager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config extends YamlConfiguration {
	private int comments;
	private final File file;
	private final FileConfiguration config;
	private final PvPManager plugin;

	@SuppressWarnings("deprecation")
	public Config(final PvPManager plugin, final String name) {
		this.plugin = plugin;
		final File file = new File(plugin.getDataFolder(), name);
		if (!file.exists())
			this.prepareFile(file, name);
		this.file = file;
		this.comments = this.getCommentsNum(file);
		this.config = YamlConfiguration.loadConfiguration(this.getConfigContent(file));
	}

	@Override
	public final void set(final String path, final Object value) {
		this.config.set(path, value);
	}

	public final void set(final String path, final Object value, final String comment) {
		if (!this.config.contains(path)) {
			this.config.set(getPluginName() + "_COMMENT_" + comments, " " + comment);
			comments++;
		}
		this.config.set(path, value);
	}

	public final void set(final String path, final Object value, final String[] comment) {
		for (final String comm : comment) {
			if (!this.config.contains(path)) {
				this.config.set(getPluginName() + "_COMMENT_" + comments, " " + comm);
				comments++;
			}
		}
		this.config.set(path, value);
	}

	@Override
	public final String getString(final String path) {
		return this.config.getString(path);
	}

	@Override
	public final String getString(final String path, final String def) {
		return this.config.getString(path, def);
	}

	@Override
	public final int getInt(final String path) {
		return this.config.getInt(path);
	}

	@Override
	public final int getInt(final String path, final int def) {
		return this.config.getInt(path, def);
	}

	@Override
	public final boolean getBoolean(final String path) {
		return this.config.getBoolean(path);
	}

	@Override
	public final boolean getBoolean(final String path, final boolean def) {
		return this.config.getBoolean(path, def);
	}

	@Override
	public final ConfigurationSection getConfigurationSection(final String path) {
		return this.config.getConfigurationSection(path);
	}

	@Override
	public final double getDouble(final String path) {
		return this.config.getDouble(path);
	}

	@Override
	public final double getDouble(final String path, final double def) {
		return this.config.getDouble(path, def);
	}

	@Override
	public final List<?> getList(final String path) {
		return this.config.getList(path);
	}

	@Override
	public final List<?> getList(final String path, final List<?> def) {
		return this.config.getList(path, def);
	}

	private int getCommentsNum(final File file) {
		if (!file.exists()) {
			return 0;
		}
		try {
			int comments = 0;
			String currentLine;
			final BufferedReader reader = new BufferedReader(new FileReader(file));
			while ((currentLine = reader.readLine()) != null) {

				if (currentLine.startsWith("#")) {
					comments++;
				}
			}
			reader.close();
			return comments;
		} catch (final IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public final InputStream getConfigContent(final File file) {
		if (!file.exists()) {
			return null;
		}
		try {
			int commentNum = 0;
			String addLine;
			String currentLine;
			final String pluginName = this.getPluginName();
			final StringBuilder whole = new StringBuilder("");
			final BufferedReader reader = new BufferedReader(new FileReader(file));
			while ((currentLine = reader.readLine()) != null) {
				if (currentLine.startsWith("#")) {
					addLine = currentLine.replaceFirst("#", pluginName + "_COMMENT_" + commentNum + ":");
					whole.append(addLine + "\n");
					commentNum++;

				} else {
					whole.append(currentLine + "\n");
				}
			}
			final String config = whole.toString();
			final InputStream configStream = new ByteArrayInputStream(config.getBytes(Charset.forName("UTF-8")));
			reader.close();
			return configStream;
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public final void prepareFile(final File file, final String resource) {
		try {
			file.getParentFile().mkdirs();
			file.createNewFile();
			if (resource != null && !resource.isEmpty()) {
				this.copyResource(plugin.getResource(resource), file);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void copyResource(final InputStream resource, final File file) {
		try {
			final OutputStream out = new FileOutputStream(file);
			int lenght;
			final byte[] buf = new byte[1024];

			while ((lenght = resource.read(buf)) > 0) {
				out.write(buf, 0, lenght);
			}
			out.close();
			resource.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public final void saveConfig() {
		final String config = this.config.saveToString();
		saveConfig(config, this.file);
	}

	public final void saveConfig(final String configString, final File file) {
		final String configuration = this.prepareConfigString(configString);
		try {
			final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(configuration);
			writer.flush();
			writer.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private String prepareConfigString(final String configString) {
		int lastLine = 0;
		int headerLine = 0;
		final String[] lines = configString.split("\n");
		final StringBuilder config = new StringBuilder("");
		for (final String line : lines) {
			if (line.startsWith(this.getPluginName() + "_COMMENT")) {
				final String comment = "#" + line.trim().substring(line.indexOf(":") + 1);
				if (comment.startsWith("# +-")) {
					if (headerLine == 0) {
						config.append(comment + "\n");
						lastLine = 0;
						headerLine = 1;
					} else if (headerLine == 1) {
						config.append(comment + "\n\n");

						lastLine = 0;
						headerLine = 0;
					}
				} else {
					String normalComment;
					if (comment.startsWith("# ' ")) {
						normalComment = comment.substring(0, comment.length() - 1).replaceFirst("# ' ", "# ");
					} else {
						normalComment = comment;
					}
					if (lastLine == 0) {
						config.append(normalComment + "\n");
					} else if (lastLine == 1) {
						config.append("\n" + normalComment + "\n");
					}
					lastLine = 0;
				}
			} else {
				config.append(line + "\n");
				lastLine = 1;
			}
		}
		return config.toString();
	}

	public final String getPluginName() {
		return plugin.getDescription().getName();
	}

}
