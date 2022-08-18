package me.NoChance.PvPManager.Libraries.Updater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.bukkit.plugin.Plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.NoChance.PvPManager.Utils.Log;

public class BukkitUpdater extends Updater {

	private URL url;

	private static final String TITLE_VALUE = "name";
	private static final String LINK_VALUE = "downloadUrl";
	private static final String TYPE_VALUE = "releaseType";
	private static final String VERSION_VALUE = "gameVersion";
	private static final String QUERY = "/servermods/files?projectIds=";
	private static final String HOST = "https://api.curseforge.com";

	public BukkitUpdater(final Plugin plugin, final int id, final UpdateType type) {
		super(plugin, id, type);
		try {
			this.url = new URL(HOST + QUERY + id);
		} catch (final MalformedURLException e) {
			Log.severe("The project ID provided for updating, " + id + " is invalid.");
			this.setResult(UpdateResult.FAIL_BADID);
			e.printStackTrace();
		}
	}

	@SuppressWarnings("resource")
	@Override
	public final boolean downloadFile() {
		try {
			HttpURLConnection httpConn = (HttpURLConnection) new URL(versionLink).openConnection();
			// curse has a redirect
			switch (httpConn.getResponseCode()) {
			case HttpURLConnection.HTTP_MOVED_PERM:
			case HttpURLConnection.HTTP_MOVED_TEMP:
				final String location = httpConn.getHeaderField("Location");
				httpConn = (HttpURLConnection) new URL(location).openConnection();
			}

			Files.copy(httpConn.getInputStream(), getFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	protected boolean read() {
		try {
			final URLConnection conn = this.url.openConnection();
			conn.setConnectTimeout(5000);
			conn.addRequestProperty("User-Agent", "Updater");
			conn.setDoOutput(true);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				final String response = reader.readLine();
				@SuppressWarnings("deprecation") // 1.8.8 compatibility..
				final JsonArray array = new JsonParser().parse(response).getAsJsonArray();
				if (array.size() == 0) {
					Log.warning("The updater could not find any files for the project id " + this.getId());
					this.setResult(UpdateResult.FAIL_BADID);
					return false;
				}

				final JsonObject jsonObject = array.get(array.size() - 1).getAsJsonObject();
				this.versionName = jsonObject.get(TITLE_VALUE).getAsString();
				this.versionLink = jsonObject.get(LINK_VALUE).getAsString();
				this.versionType = jsonObject.get(TYPE_VALUE).getAsString();
				this.versionGameVersion = jsonObject.get(VERSION_VALUE).getAsString();
				if (this.versionName.split(" v").length == 2) {
					this.versionName = this.versionName.split(" v")[1].split(" ")[0];
				}
				return true;
			}
		} catch (final IOException e) {
			Log.warning("The updater could not contact dev.bukkit.org for updating.");
			Log.warning(
			        "If you have not recently modified your configuration and this is the first time you are seeing this message, the site may be experiencing temporary downtime.");
			this.setResult(UpdateResult.FAIL_DBO);
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String getUpdateLink() {
		return "https://dev.bukkit.org/projects/pvpmanager";
	}

}
