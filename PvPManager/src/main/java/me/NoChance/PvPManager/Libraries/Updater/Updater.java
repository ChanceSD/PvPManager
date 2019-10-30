package me.NoChance.PvPManager.Libraries.Updater;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import me.NoChance.PvPManager.Utils.Log;

public abstract class Updater {

	public enum UpdateType {
		VERSION_CHECK, DOWNLOAD
	}

	public enum UpdateResult {
		SUCCESS, NO_UPDATE, FAIL_DOWNLOAD, FAIL_DBO, FAIL_NOVERSION, FAIL_BADID, UPDATE_AVAILABLE
	}

	protected String versionName;
	protected String versionLink;
	protected String versionType;
	protected String versionGameVersion;

	private static final String[] TAGS = { "-DEV", "-PRE", "-SNAPSHOT" };
	private UpdateResult result = UpdateResult.SUCCESS;
	private final Thread thread;
	private final Plugin plugin;
	private final int id;
	private final UpdateType type;
	private final File file;

	Updater(final Plugin plugin, final int id, final UpdateType type) {
		this.plugin = plugin;
		this.id = id;
		this.type = type;
		if (!Bukkit.getUpdateFolderFile().exists()) {
			Bukkit.getUpdateFolderFile().mkdirs();
		}
		this.file = new File(Bukkit.getUpdateFolderFile(), "PvPManager.jar");
		this.thread = new Thread() {
			@Override
			public void run() {
				runUpdater();
			}
		};
	}

	public final UpdateResult getResult() {
		this.waitForThread();
		return this.result;
	}

	protected final void runUpdater() {
		if (this.read()) {
			if (this.versionName.matches("^\\d.*")) {
				if (this.versionCheck(this.versionName)) {
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
			} else {
				this.setResult(UpdateResult.FAIL_NOVERSION);
				return;
			}
		}
	}

	protected final void waitForThread() {
		if (this.thread.isAlive()) {
			try {
				this.thread.join();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	protected final String[] getVersionArray(final String version) {
		String[] versionArray;
		if (hasTag(version)) {
			versionArray = version.replaceAll("(-.+)", "").split("\\.");
		} else {
			versionArray = version.split("\\.");
		}
		return versionArray;
	}

	protected boolean hasTag(final String version) {
		for (final String string : TAGS) {
			if (version.contains(string))
				return true;
		}
		return false;
	}

	protected final boolean versionCheck(final String remoteVersion) {
		final String version = this.getPlugin().getDescription().getVersion();

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

		return true;
	}

	final Thread getThread() {
		return thread;
	}

	final Plugin getPlugin() {
		return plugin;
	}

	final int getId() {
		return id;
	}

	final UpdateType getType() {
		return type;
	}

	final File getFile() {
		return file;
	}

	final void setResult(final UpdateResult result) {
		this.result = result;
	}

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

	protected abstract boolean read();

	public abstract boolean downloadFile();

}
