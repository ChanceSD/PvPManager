package me.NoChance.PvPManager.Utils;

import me.NoChance.PvPManager.Config.Variables;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;

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
		return Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") != null;
	}

	public static boolean isVaultEnabled() {
		return Bukkit.getServer().getPluginManager().getPlugin("Vault") != null;
	}
	
	public static boolean isTownyEnabled() {
		return Bukkit.getServer().getPluginManager().getPlugin("Towny") != null;
	}

	public static DisguiseCraftAPI getDisguiseCraft() {
		if (Bukkit.getServer().getPluginManager().getPlugin("DisguiseCraft") == null) {
			Variables.disableDisguise = false;
			return null;
		} else
			return DisguiseCraft.getAPI();
	}

	public static boolean isLibsDisguisesEnabled() {
		if (Bukkit.getServer().getPluginManager().getPlugin("LibsDisguises") == null)
			Variables.disableDisguise = false;
		return Bukkit.getServer().getPluginManager().getPlugin("LibsDisguises") != null;
	}

	public static boolean PMAllowed(String worldName) {
		return !Variables.worldsExcluded.contains(worldName);
	}
}