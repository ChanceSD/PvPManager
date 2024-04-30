package me.chancesd.pvpmanager.manager;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.setting.Messages;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.Settings;
import me.chancesd.pvpmanager.utils.CombatUtils;
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

	public UpdateManager(final PvPManager plugin) {
		this.plugin = plugin;
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
			Messages.setNewVersion(updater.getLatestName());
			final String updateMsg = Messages.PREFIXMSG + " §aUpdate available: §c" + Messages.getNewVersion() + " §aCurrent version: §c"
					+ Messages.getCurrentversion();
			Messages.queueAdminMsg(updateMsg);
			Bukkit.broadcast(updateMsg, Permissions.ADMIN.getPermission());
			if (Settings.isAutoUpdate() && Utils.isVersionAtLeast(Messages.getNewVersion(), "4.0")
					&& !Utils.isVersionAtLeast(Messages.getNewVersion(), "4.1")) {
				final String v4message = Messages.PREFIXMSG
						+ " §aSince §b§lv4.0 is a huge update§a, it changes a lot of the config and messages file. "
						+ "This makes it unable to be automatically updated. "
						+ "Please §b§lmanually download the update§a from the link below and copy any settings you need from the old config.";
				Messages.queueAdminMsg(v4message);
				Bukkit.broadcast(v4message, Permissions.ADMIN.getPermission());
			} else if (Settings.isAutoUpdate()) {
				if (updater.downloadFile()) {
					Messages.queueAdminMsg(Messages.PREFIXMSG + " §aUpdate downloaded, it will be applied automatically on the next server restart");
					Bukkit.broadcast(
							Messages.PREFIXMSG + " §aUpdate downloaded to update folder, it will be applied automatically on the next restart",
							Permissions.ADMIN.getPermission());
					return;
				}
				Log.info("Could not download latest update. Please update manually from one of the links below.");
			}
			Settings.setUpdate(true);
			final String linkMsg = Messages.PREFIXMSG + " §aFollow the link to download: §7" + updater.getUpdateLink();
			Messages.queueAdminMsg(linkMsg);
			Bukkit.broadcast(linkMsg, Permissions.ADMIN.getPermission());
		} else {
			Log.info("No update found");
		}
	}

	public Updater getUpdater() {
		return updater;
	}

}
