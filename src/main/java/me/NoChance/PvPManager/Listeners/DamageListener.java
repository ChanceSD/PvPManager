package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

	private PvPManager plugin;

	public DamageListener(PvPManager plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void PlayerDamageListener(EntityDamageByEntityEvent event) {
		Player attacker = null;
		Player attacked = null;
		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			attacker = (Player) event.getDamager();
			attacked = (Player) event.getEntity();
		}
		if (event.getDamager() instanceof Projectile && event.getEntity() instanceof Player) {
			Projectile proj = (Projectile) event.getDamager();
			if (proj.getShooter() instanceof Player) {
				attacker = (Player) proj.getShooter();
				attacked = (Player) event.getEntity();
			}
		}

		if (attacker != null && attacked != null) {
			if (Variables.pvpTimerEnabled) {
				if (plugin.schedulers.containsKey(attacker.getWorld().getName().toLowerCase())) {
					if (!plugin.schedulers.get(attacker.getWorld().getName().toLowerCase()).timeForPvp) {
						if (!attacker.hasPermission("pvpmanager.override")) {
							event.setCancelled(true);
							return;
						}
					}
				}
			}
			if(plugin.newbies.contains(attacked.getName())){
				event.setCancelled(true);
				attacker.sendMessage(ChatColor.DARK_RED + attacked.getName() + " has Newbie Protection!");
				return;
			}
			if (!plugin.hasPvpEnabled(attacked.getName())) {
				event.setCancelled(true);
				attacker.sendMessage(Messages.Attack_Denied_Other.replace("%p", attacked.getName()));
				return;
			} else if (!plugin.hasPvpEnabled(attacker.getName())) {
				event.setCancelled(true);
				attacker.sendMessage(Messages.Attack_Denied_You);
				return;
			}

			if (Variables.inCombatEnabled && !Variables.worldsExcluded.contains(event.getEntity().getWorld().getName())) {
				if (!plugin.inCombat.contains(attacker.getName()) && !plugin.inCombat.contains(attacked.getName())) {
					inCombat(attacker, attacked);
				}
			}
			if (Variables.disableFly)
				checkFly(attacker, attacked, event);
		}
	}

	public void checkFly(Player player1, Player player2, EntityDamageByEntityEvent event) {
		if (player1.isFlying() && player1.getAllowFlight()) {
			player1.setFlying(false);
			player1.setAllowFlight(false);
			event.setCancelled(true);
		} else if (player2.isFlying() && player2.getAllowFlight()) {
			player2.setFlying(false);
			player2.setAllowFlight(false);
			event.setCancelled(true);
		}
	}

	public void inCombat(Player player1, Player player2) {
		String pl1 = player1.getName();
		String pl2 = player2.getName();
		if (Variables.onlyTagAttacker) {
			if (!player1.hasPermission("pvpmanager.nocombat")) {
				plugin.inCombat.add(pl1);
				player1.sendMessage(Messages.You_Are_InCombat);
				Timer(pl1);
			}
			return;
		} else {
			if (!player1.hasPermission("pvpmanager.nocombat")) {
				plugin.inCombat.add(pl1);
				player1.sendMessage(Messages.You_Are_InCombat);
				Timer(pl1);
			}
			if (!player2.hasPermission("pvpmanager.nocombat")) {
				plugin.inCombat.add(pl2);
				player2.sendMessage(Messages.You_Are_InCombat);
				Timer(pl2);
			}
		}
	}

	public void Timer(final String player) {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (plugin.getServer().getPlayerExact(player) != null)
					plugin.getServer().getPlayerExact(player).sendMessage(Messages.Out_Of_Combat);

				plugin.inCombat.remove(player);
			}
		}, Variables.timeInCombat * 20);
	}
}