package me.NoChance.PvPManager.Settings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.scheduler.BukkitRunnable;

import me.NoChance.PvPManager.PvPManager;
import me.chancesd.pvpmanager.utils.ScheduleUtils;

public class LogFile {

	private final File file;
	private final SimpleDateFormat format = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss] ");

	public LogFile(final File file) {
		this.file = file;
		try {
			if (!file.exists() && file.createNewFile()) {
				write("This file logs every player that disconnected during combat");
				write("You can disable the logging in the config file\n");
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void write(final String line) {
		ScheduleUtils.runAsync(PvPManager.getInstance(), new BukkitRunnable() {
			@Override
			public void run() {
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
					bw.write(line);
					bw.newLine();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public final void log(final String line) {
		write(format.format(new Date()) + line);
	}

}
