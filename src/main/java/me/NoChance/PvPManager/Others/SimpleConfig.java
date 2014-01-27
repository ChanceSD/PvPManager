package me.NoChance.PvPManager.Others;

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
import me.NoChance.PvPManager.PvPManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class SimpleConfig extends YamlConfiguration {
	private int comments;
	private File file;
	private FileConfiguration config;
	private PvPManager plugin;

	public SimpleConfig(PvPManager plugin, String name) {
		this.comments = this.getCommentsNum(file);
		this.plugin = plugin;
		File file = new File(plugin.getDataFolder(), name);
		if (!file.exists())
			this.prepareFile(file, name);
		this.file = file;
		this.config = YamlConfiguration.loadConfiguration(this.getConfigContent(file));
	}

	public void set(String path, Object value) {
		this.config.set(path, value);
	}

	public void set(String path, Object value, String comment) {
		if (!this.config.contains(path)) {
			this.config.set(getPluginName() + "_COMMENT_" + comments, " " + comment);
			comments++;
		}
		this.config.set(path, value);
	}

	public void set(String path, Object value, String[] comment) {
		for (String comm : comment) {
			if (!this.config.contains(path)) {
				this.config.set(getPluginName() + "_COMMENT_" + comments, " " + comm);
				comments++;
			}
		}
		this.config.set(path, value);
	}

	private int getCommentsNum(File file) {
		if (!file.exists()) {
			return 0;
		}
		try {
			int comments = 0;
			String currentLine;
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while ((currentLine = reader.readLine()) != null) {

				if (currentLine.startsWith("#")) {
					comments++;
				}
			}
			reader.close();
			return comments;
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public InputStream getConfigContent(File file) {
		if (!file.exists()) {
			return null;
		}
		try {
			int commentNum = 0;
			String addLine;
			String currentLine;
			String pluginName = this.getPluginName();
			StringBuilder whole = new StringBuilder("");
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while ((currentLine = reader.readLine()) != null) {
				if (currentLine.startsWith("#")) {
					addLine = currentLine.replaceFirst("#", pluginName + "_COMMENT_" + commentNum + ":");
					whole.append(addLine + "\n");
					commentNum++;

				} else {
					whole.append(currentLine + "\n");
				}
			}
			String config = whole.toString();
			InputStream configStream = new ByteArrayInputStream(config.getBytes(Charset.forName("UTF-8")));
			reader.close();
			return configStream;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void prepareFile(File file, String resource) {
		try {
			file.getParentFile().mkdirs();
			file.createNewFile();
			if (resource != null && !resource.isEmpty()) {
				this.copyResource(plugin.getResource(resource), file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void copyResource(InputStream resource, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			int lenght;
			byte[] buf = new byte[1024];

			while ((lenght = resource.read(buf)) > 0) {
				out.write(buf, 0, lenght);
			}
			out.close();
			resource.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveConfig() {
		String config = this.config.saveToString();
		saveConfig(config, this.file);
	}

	public void saveConfig(String configString, File file) {
		String configuration = this.prepareConfigString(configString);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(configuration);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String prepareConfigString(String configString) {
		int lastLine = 0;
		int headerLine = 0;
		String[] lines = configString.split("\n");
		StringBuilder config = new StringBuilder("");
		for (String line : lines) {
			if (line.startsWith(this.getPluginName() + "_COMMENT")) {
				String comment = "#" + line.trim().substring(line.indexOf(":") + 1);
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

	public String getPluginName() {
		return plugin.getDescription().getName();
	}

}