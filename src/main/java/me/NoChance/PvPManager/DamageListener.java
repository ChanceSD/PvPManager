package me.NoChance.PvPManager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

	private PvPManager plugin;
	
	public DamageListener(PvPManager plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
	}

	@EventHandler
	public void PlayerDamageListener(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			Player attacker = (Player) event.getDamager();
			Player attacked = (Player) event.getEntity();
			inCombat(attacker, attacked);
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

	public void inCombat(Player player1, Player player2){
		String pl1 = player1.getName();
		String pl2 = player2.getName();
		plugin.inCombat.add(pl1);
		plugin.inCombat.add(pl2);
		Timer(pl1, pl2);	
	}
	
	public void Timer(final String player1, final String player2) {
		int time = plugin.getConfig().getInt("PvPManager Settings.In Combat.Time(seconds)");
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
		public void run() {
		plugin.inCombat.remove(player1);
		plugin.inCombat.remove(player2);
		}
		},time * 20);
		}
	
}