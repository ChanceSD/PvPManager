package me.NoChance.PvPManager.Config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogFile {

	private File file;

	public LogFile(File file) {
		if (!file.exists())
			try {
				file.createNewFile();
				log("This file logs all players that disconnected during combat");
			} catch (IOException e) {
				e.printStackTrace();
			}

		this.file = file;
	}

	public void log(String line) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
			bw.write(line);
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
