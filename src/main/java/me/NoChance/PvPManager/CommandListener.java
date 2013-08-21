package me.NoChance.PvPManager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener{
	
	private PvPManager plugin;
	
	public CommandListener(PvPManager plugin){
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event){
		if(plugin.inCombat.contains(event.getPlayer().getName()))
			event.setCancelled(true);	
	}

}
