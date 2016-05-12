package me.NoChance.PvPManager.Settings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogFile {

	private final File file;

	public LogFile(final File file) {
		this.file = file;

		if (!file.exists())
			try {
				file.createNewFile();
				log("This file logs all players that disconnected during combat");
				log("You can disable the logging in the config\n");
			} catch (final IOException e) {
				e.printStackTrace();
			}
	}

	public final void log(final String line) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
			bw.write(line);
			bw.newLine();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
