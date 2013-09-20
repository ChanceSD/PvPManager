package me.NoChance.PvPManager;

import java.io.*;
import java.lang.Runnable;
import java.lang.Thread;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import org.bukkit.plugin.Plugin;

public class Updater {
	private Plugin plugin;
	private UpdateType type;
	private String versionTitle;
	private long totalSize;
	private URL url;
	private Thread thread;
	private static final String DBOUrl = "http://dev.bukkit.org/bukkit-plugins/";
	private String[] noUpdateTag = { "-DEV", "-PRE", "-SNAPSHOT" };
	private Updater.UpdateResult result = Updater.UpdateResult.SUCCESS;
	private static final String TITLE = "title";
	private static final String LINK = "link";
	private static final String ITEM = "item";

	public enum UpdateResult {
		SUCCESS, NO_UPDATE, FAIL_DOWNLOAD, FAIL_DBO, FAIL_NOVERSION, FAIL_BADSLUG, UPDATE_AVAILABLE
	}

	public enum UpdateType {
		DEFAULT, NO_VERSION_CHECK, NO_DOWNLOAD
	}

	public Updater(Plugin plugin, String slug, File file, UpdateType type, boolean announce) {
		this.plugin = plugin;
		this.type = type;
		try {
			url = new URL(DBOUrl + slug + "/files.rss");
		} catch (MalformedURLException ex) {
			plugin.getLogger().warning(
					"The author of this plugin (" + plugin.getDescription().getAuthors().get(0)
							+ ") has misconfigured their Auto Update system");
			plugin.getLogger().warning(
					"The project slug given ('" + slug + "') is invalid. Please nag the author about this.");
			result = Updater.UpdateResult.FAIL_BADSLUG;
		}
		thread = new Thread(new UpdateRunnable());
		thread.start();
	}

	public Updater.UpdateResult getResult() {
		waitForThread();
		return result;
	}

	public long getFileSize() {
		waitForThread();
		return totalSize;
	}

	public String getLatestVersionString() {
		waitForThread();
		return versionTitle;
	}

	public void waitForThread() {
		if (thread.isAlive()) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean pluginFile(String name) {
		for (File file : new File("plugins").listFiles()) {
			if (file.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	private boolean versionCheck(String title) {
		if (type != UpdateType.NO_VERSION_CHECK) {
			String version = plugin.getDescription().getVersion();
			if (title.split(" v").length == 2) {
				String remoteVersion = title.split(" v")[1].split(" ")[0];
				int remVer = -1, curVer = 0;
				try {
					remVer = calVer(remoteVersion);
					curVer = calVer(version);
				} catch (NumberFormatException nfe) {
					remVer = -1;
				}
				if (hasTag(version) || version.equalsIgnoreCase(remoteVersion) || curVer >= remVer) {
					result = Updater.UpdateResult.NO_UPDATE;
					return false;
				}
			} else {
				plugin.getLogger().warning(
						"The author of this plugin (" + plugin.getDescription().getAuthors().get(0)
								+ ") has misconfigured their Auto Update system");
				plugin.getLogger()
						.warning(
								"Files uploaded to BukkitDev should contain the version number, seperated from the name by a 'v', such as PluginName v1.0");
				plugin.getLogger().warning("Please notify the author of this error.");
				result = Updater.UpdateResult.FAIL_NOVERSION;
				return false;
			}
		}
		return true;
	}

	private Integer calVer(String s) throws NumberFormatException {
		if (s.contains(".")) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < s.length(); i++) {
				Character c = s.charAt(i);
				if (Character.isLetterOrDigit(c)) {
					sb.append(c);
				}
			}
			return Integer.parseInt(sb.toString());
		}
		return Integer.parseInt(s);
	}

	private boolean hasTag(String version) {
		for (String string : noUpdateTag) {
			if (version.contains(string)) {
				return true;
			}
		}
		return false;
	}

	private boolean readFeed() {
		try {
			String title = "";
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			InputStream in = read();
			if (in != null) {
				XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
				while (eventReader.hasNext()) {
					XMLEvent event = eventReader.nextEvent();
					if (event.isStartElement()) {
						if (event.asStartElement().getName().getLocalPart().equals(TITLE)) {
							event = eventReader.nextEvent();
							title = event.asCharacters().getData();
							continue;
						}
						if (event.asStartElement().getName().getLocalPart().equals(LINK)) {
							event = eventReader.nextEvent();
							event.asCharacters().getData();
							continue;
						}
					} else if (event.isEndElement()) {
						if (event.asEndElement().getName().getLocalPart().equals(ITEM)) {
							versionTitle = title;
							break;
						}
					}
				}
				return true;
			} else {
				return false;
			}
		} catch (XMLStreamException e) {
			plugin.getLogger().warning("Could not reach dev.bukkit.org for update checking. Is it offline?");
			return false;
		}
	}

	private InputStream read() {
		try {
			return url.openStream();
		} catch (IOException e) {
			plugin.getLogger().warning(
					"Could not reach BukkitDev file stream for update checking. Is dev.bukkit.org offline?");
			return null;
		}
	}

	private class UpdateRunnable implements Runnable {

		public void run() {
			if (url != null) {
				if (readFeed()) {
					if (versionCheck(versionTitle)) {
						if (type == UpdateType.NO_DOWNLOAD) {
							result = UpdateResult.UPDATE_AVAILABLE;
						}
					}
				}
			}
		}
	}
}
