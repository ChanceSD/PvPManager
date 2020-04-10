package me.NoChance.PvPManager.Settings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.scheduler.BukkitRunnable;

import me.NoChance.PvPManager.PvPManager;

public class LogFile {

	private final File file;
	private final SimpleDateFormat format = new SimpleDateFormat("[yyyy/MM/dd] [HH:mm:ss] ");

	public LogFile(final File file) {
		this.file = file;

		if (!file.exists()) {
			try {
				file.createNewFile();
				log("This file logs all players that disconnected during combat");
				log("You can disable the logging in the config\n");
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	public final void log(final String line) {
		new BukkitRunnable() {
			@Override
			public void run() {
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
					bw.write(format.format(new Date()) + line);
					bw.newLine();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(PvPManager.getInstance());
	}

}
