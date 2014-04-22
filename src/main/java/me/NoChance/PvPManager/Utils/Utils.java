package me.NoChance.PvPManager.Utils;

import me.NoChance.PvPManager.Config.Variables;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("deprecation")
public class Utils {

	public static Player getPlayer(String name) {
		return Bukkit.getPlayer(name);
	}

	public static boolean isOnline(String player) {
		return getPlayer(player) != null;
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

	public static boolean isDisguiseCraftEnabled() {
		return Bukkit.getServer().getPluginManager().getPlugin("DisguiseCraft") != null;
	}

	public static boolean isLibsDisguisesEnabled() {
		return Bukkit.getServer().getPluginManager().getPlugin("LibsDisguises") != null;
	}

	public static boolean PMAllowed(String worldName) {
		return !Variables.worldsExcluded.contains(worldName);
	}
}
