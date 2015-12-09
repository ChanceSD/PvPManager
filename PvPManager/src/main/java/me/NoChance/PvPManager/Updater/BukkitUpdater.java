package me.NoChance.PvPManager.Updater;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import me.NoChance.PvPManager.Utils.Log;

public class BukkitUpdater extends Updater {

	private String versionName;
	private String versionLink;
	private String versionType;
	private String versionGameVersion;

	private URL url; // NO_UCD (use final)

	private final int id;
	private static final String TITLE_VALUE = "name";
	private static final String LINK_VALUE = "downloadUrl";
	private static final String TYPE_VALUE = "releaseType";
	private static final String VERSION_VALUE = "gameVersion";
	private static final String QUERY = "/servermods/files?projectIds=";
	private static final String HOST = "https://api.curseforge.com";

	private static final int BYTE_SIZE = 1024;

	public BukkitUpdater(final Plugin plugin, final int id, final UpdateType type) {
		super(plugin, type);
		this.id = id;
		try {
			this.url = new URL(HOST + QUERY + id);
		} catch (final MalformedURLException e) {
			Log.severe("The project ID provided for updating, " + id + " is invalid.");
			this.setResult(UpdateResult.FAIL_BADID);
			e.printStackTrace();
		}
		this.getThread().start();
	}

	@Override
	protected final void runUpdater() {
		if (this.url != null)
			if (this.read())
				if (this.versionCheck(this.versionName))
					if (this.versionLink != null && this.getType() == UpdateType.DOWNLOAD) {
						try {
							this.downloadFile();
						} catch (final Exception e) {
							Log.warning("The auto-updater tried to download a new update, but was unsuccessful.");
							this.setResult(UpdateResult.FAIL_DOWNLOAD);
						}
					} else {
						this.setResult(UpdateResult.UPDATE_AVAILABLE);
					}
	}

	@Override
	public final String getLatestName() {
		this.waitForThread();
		return this.versionName;
	}

	public final String getLatestFileLink() {
		this.waitForThread();
		return this.versionLink;
	}

	public final String getLatestType() {
		this.waitForThread();
		return this.versionType;
	}

	public final String getLatestGameVersion() {
		this.waitForThread();
		return this.versionGameVersion;
	}

	@Override
	public final boolean downloadFile() {
		URL url1;
		try {
			url1 = new URL(this.versionLink);
		} catch (final MalformedURLException e1) {
			e1.printStackTrace();
			return false;
		}
		try (FileOutputStream fout = new FileOutputStream(getFile()); BufferedInputStream in = new BufferedInputStream(url1.openStream())) {
			final byte[] data = new byte[BYTE_SIZE];
			int count;
			while ((count = in.read(data, 0, BYTE_SIZE)) != -1) {
				fout.write(data, 0, count);
			}
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	protected final boolean versionCheck(final String title) {
		final String version = this.getPlugin().getDescription().getVersion();
		if (title.split(" v").length == 2) {
			final String remoteVersion = title.split(" v")[1].split(" ")[0];
			if (hasTag(version) != hasTag(remoteVersion))
				return handleDifferentReleases(remoteVersion, version);

			final String[] remote = getVersionArray(remoteVersion);
			final String[] local = getVersionArray(version);
			final int length = Math.max(local.length, remote.length);
			try {
				for (int i = 0; i < length; i++) {
					final int localNumber = i < local.length ? Integer.parseInt(local[i]) : 0;
					final int remoteNumber = i < remote.length ? Integer.parseInt(remote[i]) : 0;
					if (remoteNumber > localNumber)
						return true;
					if (remoteNumber < localNumber || remoteVersion.equalsIgnoreCase(version)) {
						this.setResult(UpdateResult.NO_UPDATE);
						return false;
					}
				}
			} catch (final NumberFormatException ex) {
				Log.warning("Error reading version number!");
			}
		} else {
			this.setResult(UpdateResult.FAIL_NOVERSION);
			return false;
		}
		return true;
	}

	private boolean handleDifferentReleases(final String remote, final String local) {
		final String remoteCopy = remote.replaceAll("(-.+?\\d+)", "");
		final String localCopy = local.replaceAll("(-.+?\\d+)", "");
		if (remoteCopy.equalsIgnoreCase(localCopy))
			return hasTag(local);
		return true;
	}

	private boolean read() {
		BufferedReader reader = null;
		try {
			final URLConnection conn = this.url.openConnection();
			conn.setConnectTimeout(5000);
			conn.addRequestProperty("User-Agent", "Updater");
			conn.setDoOutput(true);
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			final String response = reader.readLine();
			final JSONArray array = (JSONArray) JSONValue.parse(response);
			if (array.size() == 0) {
				Log.warning("The updater could not find any files for the project id " + this.id);
				this.setResult(UpdateResult.FAIL_BADID);
				return false;
			}
			this.versionName = (String) ((JSONObject) array.get(array.size() - 1)).get(TITLE_VALUE);
			this.versionLink = (String) ((JSONObject) array.get(array.size() - 1)).get(LINK_VALUE);
			this.versionType = (String) ((JSONObject) array.get(array.size() - 1)).get(TYPE_VALUE);
			this.versionGameVersion = (String) ((JSONObject) array.get(array.size() - 1)).get(VERSION_VALUE);
			return true;
		} catch (final IOException e) {
			Log.warning("The updater could not contact dev.bukkit.org for updating.");
			Log.warning("If you have not recently modified your configuration and this is the first time you are seeing this message, the site may be experiencing temporary downtime.");
			this.setResult(UpdateResult.FAIL_DBO);
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

}
