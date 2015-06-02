package me.NoChance.PvPManager.Lib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import me.NoChance.PvPManager.Utils.Log;

import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Updater {

	private final Plugin plugin;
	private final UpdateType type;
	private String versionName;
	private String versionLink;
	private String versionType;
	private String versionGameVersion;

	private final boolean announce;

	private URL url; // NO_UCD (use final)
	private final File file;
	private final Thread thread;

	private final int id;
	private static final String TITLE_VALUE = "name";
	private static final String LINK_VALUE = "downloadUrl";
	private static final String TYPE_VALUE = "releaseType";
	private static final String VERSION_VALUE = "gameVersion";
	private static final String QUERY = "/servermods/files?projectIds=";
	private static final String HOST = "https://api.curseforge.com";

	private static final String[] NO_UPDATE_TAG = { "-DEV", "-PRE", "-SNAPSHOT" };
	private static final int BYTE_SIZE = 1024;
	private final String updateFolder;
	private Updater.UpdateResult result = Updater.UpdateResult.SUCCESS;

	public enum UpdateResult {
		SUCCESS, NO_UPDATE, FAIL_DOWNLOAD, FAIL_DBO, FAIL_NOVERSION, FAIL_BADID, FAIL_APIKEY, UPDATE_AVAILABLE
	}

	public enum UpdateType {
		NO_VERSION_CHECK, NO_DOWNLOAD
	}

	public Updater(final Plugin plugin, final int id, final File file, final UpdateType type, final boolean announce) {
		this.plugin = plugin;
		this.type = type;
		this.announce = announce;
		this.file = file;
		this.id = id;
		this.updateFolder = plugin.getServer().getUpdateFolder();

		try {
			this.url = new URL(Updater.HOST + Updater.QUERY + id);
		} catch (final MalformedURLException e) {
			Log.severe("The project ID provided for updating, " + id + " is invalid.");
			this.result = UpdateResult.FAIL_BADID;
			e.printStackTrace();
		}

		this.thread = new Thread(new UpdateRunnable());
		this.thread.start();
	}

	public final Updater.UpdateResult getResult() {
		this.waitForThread();
		return this.result;
	}

	public final String getLatestType() {
		this.waitForThread();
		return this.versionType;
	}

	public final String getLatestGameVersion() {
		this.waitForThread();
		return this.versionGameVersion;
	}

	public final String getLatestName() {
		this.waitForThread();
		return this.versionName;
	}

	public final String getLatestFileLink() {
		this.waitForThread();
		return this.versionLink;
	}

	private void waitForThread() {
		if ((this.thread != null) && this.thread.isAlive()) {
			try {
				this.thread.join();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("resource")
	private void saveFile(final File folder, final String file1, final String u) {
		if (!folder.exists()) {
			folder.mkdir();
		}
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try {
			final URL url1 = new URL(u);
			final int fileLength = url1.openConnection().getContentLength();
			in = new BufferedInputStream(url1.openStream());
			fout = new FileOutputStream(folder.getAbsolutePath() + "/" + file1);

			final byte[] data = new byte[Updater.BYTE_SIZE];
			int count;
			if (this.announce) {
				Log.info("About to download a new update: " + this.versionName);
			}
			long downloaded = 0;
			while ((count = in.read(data, 0, Updater.BYTE_SIZE)) != -1) {
				downloaded += count;
				fout.write(data, 0, count);
				final int percent = (int) ((downloaded * 100) / fileLength);
				if (this.announce && ((percent % 10) == 0)) {
					Log.info("Downloading update: " + percent + "% of " + fileLength + " bytes.");
				}
			}
			File[] listFiles = new File(this.plugin.getDataFolder().getParent(), this.updateFolder).listFiles();
			if (listFiles != null)
				for (final File xFile : listFiles) {
					if (xFile.getName().endsWith(".zip")) {
						xFile.delete();
					}
				}
			final File dFile = new File(folder.getAbsolutePath() + "/" + file1);
			if (dFile.getName().endsWith(".zip")) {
				this.unzip(dFile.getCanonicalPath());
			}
			if (this.announce) {
				Log.info("Finished updating.");
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			Log.warning("The auto-updater tried to download a new update, but was unsuccessful.");
			this.result = Updater.UpdateResult.FAIL_DOWNLOAD;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (fout != null) {
					fout.close();
				}
			} catch (final Exception ex) {
			}
		}
	}

	private void unzip(final String file1) {
		final File fSourceZip = new File(file1);
		try (ZipFile zipFile = new ZipFile(fSourceZip)) {
			final String zipPath = file1.substring(0, file1.length() - 4);
			Enumeration<? extends ZipEntry> e = zipFile.entries();
			while (e.hasMoreElements()) {
				ZipEntry entry = e.nextElement();
				File destinationFilePath = new File(zipPath, entry.getName());
				destinationFilePath.getParentFile().mkdirs();
				if (entry.isDirectory()) {
					continue;
				}
				try (BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
						FileOutputStream fos = new FileOutputStream(destinationFilePath);
						BufferedOutputStream bos = new BufferedOutputStream(fos, Updater.BYTE_SIZE)) {
					int b;
					final byte[] buffer = new byte[Updater.BYTE_SIZE];
					while ((b = bis.read(buffer, 0, Updater.BYTE_SIZE)) != -1) {
						bos.write(buffer, 0, b);
					}
					bos.flush();
					final String name = destinationFilePath.getName();
					if (name.endsWith(".jar") && this.pluginFile(name)) {
						destinationFilePath.renameTo(new File(this.plugin.getDataFolder().getParent(), this.updateFolder + "/" + name));
					}
					entry = null;
					destinationFilePath = null;
				}
			}
			e = null;

			File[] listFiles = new File(zipPath).listFiles();
			if (listFiles != null)
				for (final File dFile : listFiles) {
					if (dFile.isDirectory()) {
						if (this.pluginFile(dFile.getName())) {
							final File oFile = new File(this.plugin.getDataFolder().getParent(), dFile.getName());
							final File[] contents = oFile.listFiles();
							File[] listFiles2 = dFile.listFiles();
							if (listFiles2 != null)
								for (final File cFile : listFiles2) {
									boolean found = false;
									for (final File xFile : contents) {
										if (xFile.getName().equals(cFile.getName())) {
											found = true;
											break;
										}
									}
									if (!found) {
										cFile.renameTo(new File(oFile.getCanonicalFile() + "/" + cFile.getName()));
									} else {
										cFile.delete();
									}
								}
						}
					}
					dFile.delete();
				}
			new File(zipPath).delete();
			fSourceZip.delete();
		} catch (final IOException ex) {
			Log.warning("The auto-updater tried to unzip a new update file, but was unsuccessful.");
			this.result = Updater.UpdateResult.FAIL_DOWNLOAD;
			ex.printStackTrace();
		}
		new File(file1).delete();
	}

	private boolean pluginFile(final String name) {
		File[] listFiles = new File("plugins").listFiles();
		if (listFiles != null)
			for (final File file1 : listFiles) {
				if (file1.getName().equals(name)) {
					return true;
				}
			}
		return false;
	}

	private boolean versionCheck(final String title) {
		if (this.type != UpdateType.NO_VERSION_CHECK) {
			final String version = this.plugin.getDescription().getVersion();
			if (title.split(" v").length == 2) {
				final String remoteVersion = title.split(" v")[1].split(" ")[0]; // Get the newest
				// file's
				// version
				// number
				final String[] remote = remoteVersion.split("\\.");
				final String[] local = version.split("\\.");

				final int length = Math.max(local.length, remote.length);
				for (int i = 0; i < length; i++) {
					final int localNumber = i < local.length ? Integer.parseInt(local[i]) : 0;
					final int remoteNumber = i < remote.length ? Integer.parseInt(remote[i]) : 0;
					if (remoteNumber > localNumber)
						return true;
					if (remoteNumber < localNumber || remoteVersion.equalsIgnoreCase(version) || hasTag(version)) {
						this.result = Updater.UpdateResult.NO_UPDATE;
						return false;
					}

				}
			} else {
				// The file's name did not contain the string 'vVersion'
				final String authorInfo = this.plugin.getDescription().getAuthors().size() == 0 ? "" : " (" + this.plugin.getDescription().getAuthors().get(0)
						+ ")";
				Log.warning("The author of this plugin" + authorInfo + " has misconfigured their Auto Update system");
				Log.warning("Files uploaded to BukkitDev should contain the version number, seperated from the name by a 'v', such as PluginName v1.0");
				Log.warning("Please notify the author of this error.");
				this.result = Updater.UpdateResult.FAIL_NOVERSION;
				return false;
			}
		}
		return true;
	}

	private boolean hasTag(final String version) {
		for (final String string : Updater.NO_UPDATE_TAG) {
			if (version.contains(string)) {
				return true;
			}
		}
		return false;
	}

	private boolean read() {
		BufferedReader reader = null;
		try {
			final URLConnection conn = this.url.openConnection();
			conn.setConnectTimeout(5000);

			conn.addRequestProperty("User-Agent", "Updater (by Gravity)");

			conn.setDoOutput(true);

			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			final String response = reader.readLine();

			final JSONArray array = (JSONArray) JSONValue.parse(response);

			if (array.size() == 0) {
				Log.warning("The updater could not find any files for the project id " + this.id);
				this.result = UpdateResult.FAIL_BADID;
				return false;
			}

			this.versionName = (String) ((JSONObject) array.get(array.size() - 1)).get(Updater.TITLE_VALUE);
			this.versionLink = (String) ((JSONObject) array.get(array.size() - 1)).get(Updater.LINK_VALUE);
			this.versionType = (String) ((JSONObject) array.get(array.size() - 1)).get(Updater.TYPE_VALUE);
			this.versionGameVersion = (String) ((JSONObject) array.get(array.size() - 1)).get(Updater.VERSION_VALUE);

			return true;
		} catch (final IOException e) {
			if (e.getMessage().contains("HTTP response code: 403")) {
				Log.warning("dev.bukkit.org rejected the API key provided in plugins/Updater/config.yml");
				Log.warning("Please double-check your configuration to ensure it is correct.");
				this.result = UpdateResult.FAIL_APIKEY;
			} else {
				Log.warning("The updater could not contact dev.bukkit.org for updating.");
				Log.warning("If you have not recently modified your configuration and this is the first time you are seeing this message, the site may be experiencing temporary downtime.");
				this.result = UpdateResult.FAIL_DBO;
			}
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class UpdateRunnable implements Runnable {
		@Override
		public void run() {
			if (Updater.this.url != null) {
				if (Updater.this.read()) {
					if (Updater.this.versionCheck(Updater.this.versionName)) {
						if ((Updater.this.versionLink != null) && (Updater.this.type != UpdateType.NO_DOWNLOAD)) {
							String name = Updater.this.file.getName();
							if (Updater.this.versionLink.endsWith(".zip")) {
								final String[] split = Updater.this.versionLink.split("/");
								name = split[split.length - 1];
							}
							Updater.this.saveFile(new File(Updater.this.plugin.getDataFolder().getParent(), Updater.this.updateFolder), name,
									Updater.this.versionLink);
						} else {
							Updater.this.result = UpdateResult.UPDATE_AVAILABLE;
						}
					}
				}
			}
		}
	}
}
