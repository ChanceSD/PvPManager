package me.chancesd.pvpmanager.manager;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.sdutils.scheduler.ScheduleUtils;
import me.chancesd.sdutils.updater.BukkitUpdater;
import me.chancesd.sdutils.updater.SpigotUpdater;
import me.chancesd.sdutils.updater.Updater;
import me.chancesd.sdutils.updater.Updater.UpdateResult;
import me.chancesd.sdutils.updater.Updater.UpdateType;
import me.chancesd.sdutils.utils.Log;

public class UpdateManager {

	private final PvPManager plugin;
	private Updater updater;
	private String newVersion;
	private final String currentVersion;
	private boolean updateAvailable;

	public UpdateManager(final PvPManager plugin) {
		this.plugin = plugin;
		this.currentVersion = plugin.getDescription().getVersion();
		setup();
	}

	private void setup() {
		if (Conf.CHECK_UPDATES.asBool()) {
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
			final String updateMsg = Lang.PREFIX + " §aUpdate available: §c" + getNewVersion() + " §aCurrent version: §c"
					+ getCurrentversion();
			Lang.queueAdminMsg(updateMsg);
			Bukkit.broadcast(updateMsg, Permissions.ADMIN.getPermission());
			if (Conf.AUTO_UPDATE.asBool()) {
				if (updater.downloadFile()) {
					Lang.queueAdminMsg(Lang.PREFIX + " §aUpdate downloaded, it will be applied automatically on the next server restart");
					Bukkit.broadcast(
							Lang.PREFIX + " §aUpdate downloaded to update folder, it will be applied automatically on the next restart",
							Permissions.ADMIN.getPermission());
					return;
				}
				Log.info("Could not download latest update. Please update manually from one of the links below.");
			}
			updateAvailable = true;
			final String linkMsg = Lang.PREFIX + " §aFollow one of the links to download: \n§7- https://modrinth.com/plugin/pvpmanager"
					+ "\n§7- https://dev.bukkit.org/projects/pvpmanager";
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

	public boolean hasUpdateAvailable() {
		return updateAvailable;
	}

	public void setUpdateAvailable(final boolean updateAvailable) {
		this.updateAvailable = updateAvailable;
	}

	public Updater getUpdater() {
		return updater;
	}

}
