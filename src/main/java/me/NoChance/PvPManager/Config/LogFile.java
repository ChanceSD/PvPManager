package me.NoChance.PvPManager.Config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogFile {

	private File file;

	public LogFile(File file) {
		this.file = file;
		
		if (!file.exists())
			try {
				file.createNewFile();
				log("This file logs all players that disconnected during combat");
				log("You can disable the logging in the config\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
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
