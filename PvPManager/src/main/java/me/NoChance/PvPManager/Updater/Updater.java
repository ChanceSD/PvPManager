package me.NoChance.PvPManager.Updater;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public abstract class Updater {

	public enum UpdateType {
		VERSION_CHECK, DOWNLOAD
	}

	public enum UpdateResult {
		SUCCESS, NO_UPDATE, FAIL_DOWNLOAD, FAIL_DBO, FAIL_NOVERSION, FAIL_BADID, UPDATE_AVAILABLE
	}

	private static final String[] TAGS = { "-DEV", "-PRE", "-SNAPSHOT" };
	private UpdateResult result = UpdateResult.SUCCESS;
	private final Thread thread;
	private final Plugin plugin;
	private final UpdateType type;
	private final File file = new File(Bukkit.getUpdateFolderFile(), "PvPManager.jar");

	protected Updater(final Plugin plugin, final UpdateType type) {
		this.plugin = plugin;
		this.type = type;
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

	protected final void waitForThread() {
		if ((this.thread != null) && this.thread.isAlive()) {
			try {
				this.thread.join();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	protected final String[] getVersionArray(final String version) {
		String[] versionArray = null;
		if (hasTag(version)) {
			versionArray = version.replaceFirst("(-.+?)(?=\\d)", "\\.").split("\\.");
		} else {
			versionArray = version.split("\\.");
		}
		return versionArray;
	}

	private boolean hasTag(final String version) {
		for (final String string : TAGS) {
			if (version.contains(string)) {
				return true;
			}
		}
		return false;
	}

	public final Thread getThread() {
		return thread;
	}

	public final Plugin getPlugin() {
		return plugin;
	}

	public final UpdateType getType() {
		return type;
	}

	public final File getFile() {
		return file;
	}

	public final void setResult(final UpdateResult result) {
		this.result = result;
	}

	protected abstract boolean versionCheck(final String title);

	public abstract boolean downloadFile();

	protected abstract void runUpdater();

	public abstract String getLatestName();

}
