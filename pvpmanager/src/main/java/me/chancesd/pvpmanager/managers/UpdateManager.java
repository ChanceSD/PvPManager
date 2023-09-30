package me.chancesd.pvpmanager.managers;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.chancesd.pvpmanager.utils.ScheduleUtils;
import me.chancesd.sdutils.updater.BukkitUpdater;
import me.chancesd.sdutils.updater.SpigotUpdater;
import me.chancesd.sdutils.updater.Updater;
import me.chancesd.sdutils.updater.Updater.UpdateResult;
import me.chancesd.sdutils.updater.Updater.UpdateType;
import me.chancesd.sdutils.utils.Log;

public class UpdateManager {

	private final PvPManager plugin;
	private Updater updater;

	public UpdateManager(final PvPManager plugin) {
		this.plugin = plugin;
		setup();
	}

	private void setup() {
		if (Settings.isUpdateCheck()) {
			ScheduleUtils.runAsyncTimer(new BukkitRunnable() {
				@Override
				public void run() {
					checkForUpdates();
				}
			}, 1, 18000);
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
			Bukkit.broadcast(updateMsg, "pvpmanager.admin");
			if (Settings.isAutoUpdate()) {
				if (updater.downloadFile()) {
					Messages.queueAdminMsg(Messages.PREFIXMSG + " §aUpdate downloaded, it will be applied automatically on the next server restart");
					Bukkit.broadcast(Messages.PREFIXMSG + " §aUpdate downloaded to your update folder, it will be applied automatically on the next server restart",
					        "pvpmanager.admin");
					return;
				}
				Log.info("Could not download latest update. Please update manually from one of the links below.");
			}
			Settings.setUpdate(true);
			final String linkMsg = Messages.PREFIXMSG + " §aFollow the link to download: §8" + updater.getUpdateLink();
			Messages.queueAdminMsg(linkMsg);
			Bukkit.broadcast(linkMsg, "pvpmanager.admin");
		} else {
			Log.info("No update found");
		}
	}

	public Updater getUpdater() {
		return updater;
	}

}
