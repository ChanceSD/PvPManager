package me.NoChance.PvPManager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

	private PvPManager plugin;

	public PlayerListener(PvPManager plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void onPlayerLogout(PlayerQuitEvent event) {
			if (plugin.playersStatusOff.contains(event.getPlayer().getName()))
				plugin.playersStatusOff.remove(event.getPlayer().getName());
	}
}
