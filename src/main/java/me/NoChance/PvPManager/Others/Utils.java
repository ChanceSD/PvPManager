package me.NoChance.PvPManager.Others;

import me.NoChance.PvPManager.Config.Variables;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Utils {

	public static Player getPlayer(String name) {
		return Bukkit.getServer().getPlayerExact(name);
	}

	public static boolean isOnline(Player player) {
		if (Bukkit.getServer().getPlayerExact(player.getName()) != null)
			return true;
		else
			return false;
	}

	public static boolean isOnline(String player) {
		if (Bukkit.getServer().getPlayerExact(player) != null)
			return true;
		else
			return false;
	}

	public static void register(Listener l, JavaPlugin jp) {
		Bukkit.getPluginManager().registerEvents(l, jp);
	}

	public static boolean isWGEnabled() {
		if (Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") != null)
			return true;
		else
			return false;
	}
	
	public static boolean isVaultEnabled() {
		if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null)
			return true;
		else
			return false;
	}
	
	public static boolean PMAllowed(String worldName){
		return !Variables.worldsExcluded.contains(worldName);
	}
}
