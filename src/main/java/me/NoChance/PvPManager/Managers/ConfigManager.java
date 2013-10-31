package me.NoChance.PvPManager.Managers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Utils;

import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
	
	private PvPManager plugin;
	private File usersFile;
	private YamlConfiguration users;

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
				plugin.getLogger().info("New Users File Created Successfully!");
				return;
			}
			users.load(usersFile);
			save();
		} catch (Exception e) {
		}
	}

	public void getDefaults() {
		InputStream defConfigStream = plugin.getResource("users.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			users.setDefaults(defConfig);
			users.options().copyHeader(true);
			users.options().copyDefaults(true);
		}
	}

	public void loadUsers() {
		List<String> store = new ArrayList<String>();
		store = users.getStringList("players");
		for (String a : store) {
			plugin.getCm().disablePvp(Utils.getPlayer(a));
		}
		store.clear();
	}

	public void save() {
		try {
			users.save(usersFile);
		} catch (Exception e) {
			System.out.println(("Config Failed to save, returned error: " + e.getMessage()));
		}
	}

	public void saveUsers() {
		List<String> store = new ArrayList<String>();
		for (String a : plugin.getCm().getPlayersStatusOff()) {
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
