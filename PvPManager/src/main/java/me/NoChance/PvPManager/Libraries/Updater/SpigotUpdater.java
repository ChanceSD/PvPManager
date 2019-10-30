package me.NoChance.PvPManager.Libraries.Updater;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import org.bukkit.plugin.Plugin;

import me.NoChance.PvPManager.Utils.Log;

public class SpigotUpdater extends Updater {

	public SpigotUpdater(final Plugin plugin, final int id, final UpdateType type) {
		super(plugin, id, type);
		this.getThread().start();
	}

	@Override
	protected boolean read() {
		try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.getId()).openStream();
		        Scanner scanner = new Scanner(inputStream)) {
			if (scanner.hasNext()) {
				this.versionName = scanner.next();
			}
		} catch (final IOException e) {
			Log.warning("Spigot might be down or have it's protection up! This error can be safely ignored");
			this.setResult(UpdateResult.FAIL_DBO);
			return false;
		}
		return true;
	}

	@Override
	public final boolean downloadFile() {
		return false;
	}

}
