package me.NoChance.PvPManager.Updater;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

import org.bukkit.plugin.Plugin;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import me.NoChance.PvPManager.Utils.Log;

public class SpigotUpdater extends Updater {

	private String versionName;
	private Document doc;

	public SpigotUpdater(final Plugin plugin, final UpdateType type) {
		super(plugin, type);
		this.getThread().start();
	}

	@Override
	public final void runUpdater() {
		try {
			final Response response = Jsoup.connect("http://www.spigotmc.org/resources/pvpmanager.845/history").userAgent("Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0").execute();
			doc = response.parse();
			final Element versionEntry = doc.select("td.version").first();
			if (versionCheck(versionEntry.text())) {
				setResult(UpdateResult.UPDATE_AVAILABLE);
				if (getType() == UpdateType.VERSION_CHECK)
					return;
				downloadFile();
			}
		} catch (final IOException e) {
			Log.severe("Spigot might be down or have it's protection up! This error can be safely ignored");
			Log.severe("If this keeps happening try setting the updater to Bukkit in the config");
		}
	}

	@Override
	public final boolean downloadFile() {
		try {
			final Element download = doc.select("td.dataOptions.download").first();
			final String link = download.childNode(0).attr("abs:href");
			final URLConnection url = new URL(link).openConnection();
			url.setRequestProperty("User-Agent", "Mozilla");
			Files.copy(url.getInputStream(), getFile().toPath());
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	protected final boolean versionCheck(final String title) {
		final String version = this.getPlugin().getDescription().getVersion();
		this.versionName = title;
		if (title.matches("^\\d.*")) {
			final String[] remote = getVersionArray(title);
			final String[] local = getVersionArray(version);
			final int length = Math.max(local.length, remote.length);
			try {
				for (int i = 0; i < length; i++) {
					final int localNumber = i < local.length ? Integer.parseInt(local[i]) : 0;
					final int remoteNumber = i < remote.length ? Integer.parseInt(remote[i]) : 0;
					if (remoteNumber > localNumber)
						return true;
					if (remoteNumber < localNumber || title.equalsIgnoreCase(version)) {
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

	@Override
	public final String getLatestName() {
		this.waitForThread();
		return versionName;
	}

}
