package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.Messages;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Variables;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

	private PvPManager plugin;
	
	public DamageListener(PvPManager plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
	}

	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void PlayerDamageListener(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			Player attacker = (Player) event.getDamager();
			Player attacked = (Player) event.getEntity();
			if (!plugin.hasPvpEnabled(attacked.getName())) {
				event.setCancelled(true);
				attacker.sendMessage(Messages.Attack_Denied_Other.replace("%p", attacked.getName()));
				return;
			}
			else if (!plugin.hasPvpEnabled(attacker.getName())){
				event.setCancelled(true);
				attacker.sendMessage(Messages.Attack_Denied_You);
				return;
			}
			else if(Variables.inCombatEnabled && !Variables.worldsExcluded.contains(event.getEntity().getWorld().getName())){
			if(!plugin.inCombat.contains(attacker.getName()) && !plugin.inCombat.contains(attacked.getName())){
				inCombat(attacker, attacked);	
			}
			}
			if(Variables.disableFly)
			checkFly(attacker, attacked, event);
		}
		if(event.getDamager() instanceof Projectile && event.getEntity() instanceof Player){
			Projectile proj = (Projectile) event.getDamager();
			if(proj.getShooter() instanceof Player){
			Player attacker = (Player) proj.getShooter();
			Player attacked = (Player) event.getEntity();
			if (!plugin.hasPvpEnabled(attacked.getName())) {
				event.setCancelled(true);
				attacker.sendMessage(Messages.Attack_Denied_Other.replace("%p", attacked.getName()));
				return;
			}
			else if (!plugin.hasPvpEnabled(attacker.getName())){
				event.setCancelled(true);
				attacker.sendMessage(Messages.Attack_Denied_You);
				return;
			}
			else if(Variables.inCombatEnabled && !Variables.worldsExcluded.contains(event.getEntity().getWorld().getName())){
				if(!plugin.inCombat.contains(attacker.getName()) && !plugin.inCombat.contains(attacked.getName())){	
					inCombat(attacker, attacked);	
				}
			}
			if(Variables.disableFly)
			checkFly(attacker, attacked, event);
			}	
		}
	}
	
	public void checkFly(Player player1, Player player2, EntityDamageByEntityEvent event){
		if(player1.isFlying() && player1.getAllowFlight()){
			player1.setFlying(false);
			player1.setAllowFlight(false);
			event.setCancelled(true);
		}
		else if(player2.isFlying() && player2.getAllowFlight()){
			player2.setFlying(false);
			player2.setAllowFlight(false);
			event.setCancelled(true);
		}
	}

	public void inCombat(Player player1, Player player2){
		String pl1 = player1.getName();
		String pl2 = player2.getName();
		plugin.inCombat.add(pl1);
		player1.sendMessage(Messages.You_Are_InCombat);
		plugin.inCombat.add(pl2);
		player2.sendMessage(Messages.You_Are_InCombat);
		Timer(pl1, pl2);	
	}
	
	public void Timer(final String player1, final String player2) {
		int time = Variables.timeInCombat;
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
		public void run() {
		if(plugin.getServer().getPlayerExact(player1) != null)
		plugin.getServer().getPlayerExact(player1).sendMessage(Messages.Out_Of_Combat);
		
		if(plugin.getServer().getPlayerExact(player2) != null)
		plugin.getServer().getPlayerExact(player2).sendMessage(Messages.Out_Of_Combat);
		
		plugin.inCombat.remove(player1);
		plugin.inCombat.remove(player2);
		}
		},time * 20);
		}
	
}