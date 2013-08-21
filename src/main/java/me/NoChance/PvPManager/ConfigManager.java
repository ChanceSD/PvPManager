package me.NoChance.PvPManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
	private PvPManager plugin;
	public File usersFile;
	public YamlConfiguration users;

	public ConfigManager(PvPManager plugin) {
		this.plugin = plugin;
		this.users = new YamlConfiguration();
		this.usersFile = new File(plugin.getDataFolder(), "users.yml");
	}

	public void load() {
		try {
			if (!usersFile.exists()) {
				plugin.saveResource("users.yml", false);
				getDefaults();
				plugin.logger.info("[PvPManager] New Users File Created Successfully!");
				return;
			}
			users.load(usersFile);
			save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getDefaults() {
		InputStream defConfigStream = plugin.getResource("users.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration
					.loadConfiguration(defConfigStream);
			users.setDefaults(defConfig);
			users.options().copyHeader(true);
			users.options().copyDefaults(true);
		}
	}

	public void loadUsers() {
		List<String> store = new ArrayList<String>();
		store = users.getStringList("players");
		for (String a : store) {
			plugin.playersStatusOff.add(a);
			System.out.println(a);
		}
		store.clear();
	}

	public void save() {
		try {
			users.save(usersFile);
		} catch (Exception e) {
			System.out.println(("Config Failed to save, returned error: " + e
					.getMessage()));
		}
	}

	public void saveUsers() {
		List<String> store = new ArrayList<String>();
		for (String a : plugin.playersStatusOff) {
			store.add(a);
		}
		try {
			users.set("players", store);
			users.save(usersFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
