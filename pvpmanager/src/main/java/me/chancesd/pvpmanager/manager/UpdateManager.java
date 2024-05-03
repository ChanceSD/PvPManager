package me.chancesd.pvpmanager.manager;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.Settings;
import me.chancesd.pvpmanager.utils.ScheduleUtils;
import me.chancesd.sdutils.updater.BukkitUpdater;
import me.chancesd.sdutils.updater.SpigotUpdater;
import me.chancesd.sdutils.updater.Updater;
import me.chancesd.sdutils.updater.Updater.UpdateResult;
import me.chancesd.sdutils.updater.Updater.UpdateType;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.Utils;

public class UpdateManager {

	private final PvPManager plugin;
	private Updater updater;
	private String newVersion;
	private final String currentVersion;

	public UpdateManager(final PvPManager plugin) {
		this.plugin = plugin;
		this.currentVersion = plugin.getDescription().getVersion();
		setup();
	}

	private void setup() {
		if (Settings.isUpdateCheck()) {
			ScheduleUtils.runAsyncTimer(this::checkForUpdates, 3, 18000, TimeUnit.SECONDS);
		}
	}

	public void checkForUpdates() {
		Log.info("Checking for updates...");
		updater = new BukkitUpdater(plugin, 63773, UpdateType.VERSION_CHECK).check();
		if (updater.getResult() != UpdateResult.UPDATE_AVAILABLE) {
			updater = new SpigotUpdater(plugin, 845, UpdateType.VERSION_CHECK).check();
		}
		if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
			setNewVersion(updater.getLatestName());
			final String updateMsg = Lang.PREFIXMSG + " §aUpdate available: §c" + getNewVersion() + " §aCurrent version: §c"
					+ getCurrentversion();
			Lang.queueAdminMsg(updateMsg);
			Bukkit.broadcast(updateMsg, Permissions.ADMIN.getPermission());
			if (Settings.isAutoUpdate() && Utils.isVersionAtLeast(getNewVersion(), "4.0")
					&& !Utils.isVersionAtLeast(getNewVersion(), "4.1")) {
				final String v4message = Lang.PREFIXMSG
						+ " §aSince §b§lv4.0 is a huge update§a, it changes a lot of the config and messages file. "
						+ "This makes it unable to be automatically updated. "
						+ "Please §b§lmanually download the update§a from the link below and copy any settings you need from the old config.";
				Lang.queueAdminMsg(v4message);
				Bukkit.broadcast(v4message, Permissions.ADMIN.getPermission());
			} else if (Settings.isAutoUpdate()) {
				if (updater.downloadFile()) {
					Lang.queueAdminMsg(Lang.PREFIXMSG + " §aUpdate downloaded, it will be applied automatically on the next server restart");
					Bukkit.broadcast(
							Lang.PREFIXMSG + " §aUpdate downloaded to update folder, it will be applied automatically on the next restart",
							Permissions.ADMIN.getPermission());
					return;
				}
				Log.info("Could not download latest update. Please update manually from one of the links below.");
			}
			Settings.setUpdate(true);
			final String linkMsg = Lang.PREFIXMSG + " §aFollow the link to download: §7" + updater.getUpdateLink();
			Lang.queueAdminMsg(linkMsg);
			Bukkit.broadcast(linkMsg, Permissions.ADMIN.getPermission());
		} else {
			Log.info("No update found");
		}
	}

	public String getCurrentversion() {
		return currentVersion;
	}

	public String getNewVersion() {
		return newVersion;
	}

	public void setNewVersion(final String newVersion) {
		this.newVersion = newVersion;
	}

	public Updater getUpdater() {
		return updater;
	}

}
