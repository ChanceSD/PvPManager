package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.CombatManager;
import me.NoChance.PvPManager.GlobalManager;
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
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;

public class DamageListener implements Listener {

	private WorldGuardPlugin wg;
	private CombatManager cm;

	public DamageListener(GlobalManager globalManager) {
		this.gm = globalManager;
		this.cm = gm.getCM();
		PvPManager plugin = globalManager.getPlugin();
		wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		if (worldGuardEnabled())
			plugin.getLogger().info("WorldGuard Found! Detecting PvP regions...");
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void PlayerDamageListener(EntityDamageByEntityEvent event) {
		if (!cm.isPvP(event)) {
			return;
		}
		Player attacker = getAttacker(event);
		Player attacked = getAttacked(event);

		if (worldGuardEnabled()) {
			ApplicableRegionSet set = wg.getRegionManager(attacked.getWorld()).getApplicableRegions(attacked.getLocation());
			if (set.getFlag(DefaultFlag.PVP) != null) {
				if (set.getFlag(DefaultFlag.PVP).equals(State.ALLOW))
					return;
			}
		}
		if (Variables.pvpTimerEnabled) {
			if (plugin.schedulers.containsKey(attacker.getWorld().getName().toLowerCase())) {
				if (!plugin.schedulers.get(attacker.getWorld().getName().toLowerCase()).timeForPvp) {
					event.setCancelled(true);
					return;
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
			if (!cm.isInCombat(attacker) && !cm.isInCombat(attacked)) {
				inCombat(attacker, attacked, event);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void DamageListenerForOverride(EntityDamageByEntityEvent event) {
		if (!cm.isPvP(event)) {
			return;
		}
		if (getAttacker(event).hasPermission("pvpmanager.override") && event.isCancelled())
			event.setCancelled(false);
	}

	public Player getAttacker(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Projectile)
			return (Player) ((Projectile) event.getDamager()).getShooter();
		else
			return (Player) event.getDamager();
	}

	public Player getAttacked(EntityDamageByEntityEvent event) {
		return (Player) event.getEntity();
	}

	public void checkFly(Player player, EntityDamageByEntityEvent event) {
		if (player.isFlying() && player.getAllowFlight()) {
			player.setFlying(false);
			player.setAllowFlight(false);
			event.setCancelled(true);
		}
	}

	public void inCombat(Player player1, Player player2, EntityDamageByEntityEvent event) {
		if (Variables.onlyTagAttacker) {
			if (!player1.hasPermission("pvpmanager.nocombat")) {
				cm.tag(player1);
				player1.sendMessage(Messages.You_Are_InCombat);
				Timer(player1);
				if (Variables.disableFly) {
					checkFly(player1, event);
				}
			}
			return;
		} else {
			if (!player1.hasPermission("pvpmanager.nocombat")) {
				cm.tag(player1);
				player1.sendMessage(Messages.You_Are_InCombat);
				Timer(player1);
				if (Variables.disableFly) {
					checkFly(player1, event);
				}
			}
			if (!player2.hasPermission("pvpmanager.nocombat")) {
				cm.tag(player2);
				player2.sendMessage(Messages.You_Are_InCombat);
				Timer(player2);
				if (Variables.disableFly) {
					checkFly(player2, event);
				}
			}
		}
	}

	public void Timer(final Player player) {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (cm.isOnline(player))
					player.sendMessage(Messages.Out_Of_Combat);

				cm.untag(player);
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