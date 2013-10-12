package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class DamageListener implements Listener {

	private PvPManager plugin;
	private WorldGuardPlugin wg;

	public DamageListener(PvPManager plugin) {
		this.plugin = plugin;
		wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		if (worldGuardEnabled())
			plugin.getLogger().info("WorldGuard Found! Detecting PvP regions...");
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void PlayerDamageListener(EntityDamageByEntityEvent event) {
		Player attacker = null;
		Player attacked = null;
		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			attacker = (Player) event.getDamager();
			attacked = (Player) event.getEntity();
		} else if (event.getDamager() instanceof Projectile && event.getEntity() instanceof Player) {
			Projectile proj = (Projectile) event.getDamager();
			if (proj.getShooter() instanceof Player) {
				attacker = (Player) proj.getShooter();
				attacked = (Player) event.getEntity();
			}
		}

		if (attacker != null && attacked != null) {
			if (worldGuardEnabled()) {
				RegionManager set = wg.getRegionManager(attacked.getWorld());
				if (set.getApplicableRegions(attacked.getLocation()).allows(DefaultFlag.PVP)) {
					return;
				}
			}
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
			if (plugin.newbies.contains(attacked.getName()) || plugin.newbies.contains(attacker.getName())) {
				event.setCancelled(true);
				if (plugin.newbies.contains(attacked.getName()))
					attacker.sendMessage("§6[§8PvPManager§6]§4" + attacked.getName() + " has Newbie Protection!");
				else
					attacked.sendMessage("§6[§8PvPManager§6]§4" + "Please wait until your PvP protection expires");
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
					inCombat(attacker, attacked, event);
				}
			}
		}
	}

	public void checkFly(Player player, EntityDamageByEntityEvent event) {
		if (player.isFlying() && player.getAllowFlight()) {
			player.setFlying(false);
			player.setAllowFlight(false);
			event.setCancelled(true);
		}
	}

	public void inCombat(Player player1, Player player2, EntityDamageByEntityEvent event) {
		String pl1 = player1.getName();
		String pl2 = player2.getName();
		if (Variables.onlyTagAttacker) {
			if (!player1.hasPermission("pvpmanager.nocombat")) {
				plugin.inCombat.add(pl1);
				player1.sendMessage(Messages.You_Are_InCombat);
				Timer(pl1);
				if (Variables.disableFly) {
					checkFly(player1, event);
				}
			}
			return;
		} else {
			if (!player1.hasPermission("pvpmanager.nocombat")) {
				plugin.inCombat.add(pl1);
				player1.sendMessage(Messages.You_Are_InCombat);
				Timer(pl1);
				if (Variables.disableFly) {
					checkFly(player1, event);
				}
			}
			if (!player2.hasPermission("pvpmanager.nocombat")) {
				plugin.inCombat.add(pl2);
				player2.sendMessage(Messages.You_Are_InCombat);
				Timer(pl2);
				if (Variables.disableFly) {
					checkFly(player2, event);
				}
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

	public boolean worldGuardEnabled() {
		if (wg != null)
			return true;
		else
			return false;
	}
}