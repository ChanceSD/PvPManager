package me.NoChance.PvPManager;

import org.bukkit.ChatColor;
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
		if(plugin.inCombat.contains(event.getPlayer().getName())){
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "Command denied! You are still in combat!");
		}
	}

}
