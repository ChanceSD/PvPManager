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

public class SimpleConfigManager {

	private PvPManager plugin;

	/*
	 * Manage custom configurations and files
	 */
	public SimpleConfigManager(PvPManager plugin) {
		this.plugin = plugin;
	}

	/*
	 * Get new configuration with header
	 * 
	 * @param filePath - Path to file
	 * 
	 * @return - New SimpleConfig
	 */
	public SimpleConfig getNewConfig(String filePath, String[] header) {

		File file = this.getConfigFile(filePath);

		if (!file.exists()) {
			this.prepareFile(filePath, filePath);

			if (header != null && header.length != 0) {
				this.setHeader(file, header);
			}

		}

		SimpleConfig config = new SimpleConfig(this.getConfigContent(filePath), file, this.getCommentsNum(file), plugin);
		return config;

	}

	/*
	 * Get new configuration
	 * 
	 * @param filePath - Path to file
	 * 
	 * @return - New SimpleConfig
	 */
	public SimpleConfig getNewConfig(String filePath) {
		return this.getNewConfig(filePath, null);
	}

	/*
	 * Get configuration file from string
	 * 
	 * @param file - File path
	 * 
	 * @return - New file object
	 */
	private File getConfigFile(String file) {

		if (file.isEmpty() || file == null) {
			return null;
		}

		File configFile;

		if (file.contains("/")) {

			if (file.startsWith("/")) {
				configFile = new File(plugin.getDataFolder() + file.replace("/", File.separator));
			} else {
				configFile = new File(plugin.getDataFolder() + File.separator + file.replace("/", File.separator));
			}

		} else {
			configFile = new File(plugin.getDataFolder(), file);
		}

		return configFile;

	}

	/*
	 * Create new file for config and copy resource into it
	 * 
	 * @param file - Path to file
	 * 
	 * @param resource - Resource to copy
	 */
	public void prepareFile(String filePath, String resource) {

		File file = this.getConfigFile(filePath);

		if (file.exists()) {
			return;
		}

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

	/*
	 * Create new file for config without resource
	 * 
	 * @param file - File to create
	 */
	public void prepareFile(String filePath) {
		this.prepareFile(filePath, null);
	}

	/*
	 * Adds header block to config
	 * 
	 * @param file - Config file
	 * 
	 * @param header - Header lines
	 */
	public void setHeader(File file, String[] header) {

		if (!file.exists()) {
			return;
		}

		try {
			String currentLine;
			StringBuilder config = new StringBuilder("");
			BufferedReader reader = new BufferedReader(new FileReader(file));

			while ((currentLine = reader.readLine()) != null) {
				config.append(currentLine + "\n");
			}

			reader.close();
			config.append("# +----------------------------------------------------+ #\n");

			for (String line : header) {

				if (line.length() > 50) {
					continue;
				}

				int lenght = (50 - line.length()) / 2;
				StringBuilder finalLine = new StringBuilder(line);

				for (int i = 0; i < lenght; i++) {
					finalLine.append(" ");
					finalLine.reverse();
					finalLine.append(" ");
					finalLine.reverse();
				}

				if (line.length() % 2 != 0) {
					finalLine.append(" ");
				}

				config.append("# < " + finalLine.toString() + " > #\n");

			}

			config.append("# +----------------------------------------------------+ #");

			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(this.prepareConfigString(config.toString()));
			writer.flush();
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/*
	 * Read file and make comments SnakeYAML friendly
	 * 
	 * @param filePath - Path to file
	 * 
	 * @return - File as Input Stream
	 */
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

	/*
	 * Get comments from file
	 * 
	 * @param file - File
	 * 
	 * @return - Comments number
	 */
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

	/*
	 * Get config content from file
	 * 
	 * @param filePath - Path to file
	 * 
	 * @return - readied file
	 */
	public InputStream getConfigContent(String filePath) {
		return this.getConfigContent(this.getConfigFile(filePath));
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

					/*
					 * If header line = 0 then it is header start, if it's equal
					 * to 1 it's the end of header
					 */

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

					/*
					 * Last line = 0 - Comment Last line = 1 - Normal path
					 */

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

	/*
	 * Saves configuration to file
	 * 
	 * @param configString - Config string
	 * 
	 * @param file - Config file
	 */
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

	public String getPluginName() {
		return plugin.getDescription().getName();
	}

	/*
	 * Copy resource from Input Stream to file
	 * 
	 * @param resource - Resource from .jar
	 * 
	 * @param file - File to write
	 */
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

}
