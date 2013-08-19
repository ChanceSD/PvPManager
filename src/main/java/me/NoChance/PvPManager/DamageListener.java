package me.NoChance.PvPManager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

	private PvPManager plugin; //alterado - verificar se funciona
	
	public DamageListener(PvPManager plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
	}

	@EventHandler
	public void PlayerDamageListener(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			Player attacker = (Player) event.getDamager();
			Player attacked = (Player) event.getEntity();
			if (!plugin.hasPvpEnabled(attacked.getName())) {
				event.setCancelled(true);
				attacker.sendMessage(ChatColor.DARK_RED + attacked.getName() + " Has PvP Disabled!");
			}
			else if (!plugin.hasPvpEnabled(attacker.getName())){
				event.setCancelled(true);
				attacker.sendMessage(ChatColor.DARK_RED + "Your PvP is Disabled!");
			}
		}
	}
}
