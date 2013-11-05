package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Managers.CombatManager;

import org.bukkit.Effect;
import org.bukkit.Material;
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

	public DamageListener(PvPManager pvPManager) {
		this.cm = pvPManager.getCm();
		wg = (WorldGuardPlugin) pvPManager.getServer().getPluginManager().getPlugin("WorldGuard");
		if (worldGuardEnabled())
			pvPManager.getLogger().info("WorldGuard Found! Using it to detect PvP Zones");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void PlayerDamageListener(EntityDamageByEntityEvent event) {
		if (!cm.isPvP(event))
			return;
		Player attacker = getAttacker(event);
		Player attacked = (Player) event.getEntity();

		if (attacker.hasPermission("pvpmanager.override")) {
			event.setCancelled(false);
			return;
		}
		if (worldGuardEnabled()) {
			ApplicableRegionSet set = wg.getRegionManager(attacked.getWorld()).getApplicableRegions(attacked.getLocation());
			if (set.getFlag(DefaultFlag.PVP) != null) {
				if (set.getFlag(DefaultFlag.PVP).equals(State.ALLOW))
					return;
			}
		}
		if (Variables.pvpTimerEnabled) {
			if (cm.getWtm().isPvpTimerWorld(attacker.getWorld())) {
				if (!cm.getWtm().isTimeForPvp(attacker.getWorld())) {
					event.setCancelled(true);
					return;
				}
			}
		}
		if (cm.isNewbie(attacked) || cm.isNewbie(attacker)) {
			event.setCancelled(true);
			if (cm.isNewbie(attacked))
				attacker.sendMessage("§6[§8PvPManager§6]§4" + attacked.getName() + " has Newbie Protection!");
			else
				attacked.sendMessage("§6[§8PvPManager§6]§4" + "Please wait until your PvP protection expires");
			return;
		}
		if (!cm.hasPvpEnabled(attacked.getName())) {
			event.setCancelled(true);
			attacker.sendMessage(Messages.Attack_Denied_Other.replace("%p", attacked.getName()));
			return;
		} else if (!cm.hasPvpEnabled(attacker.getName())) {
			event.setCancelled(true);
			attacker.sendMessage(Messages.Attack_Denied_You);
			return;
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void LastDamageListener(EntityDamageByEntityEvent event) {
		if (!cm.isPvP(event))
			return;
		Player attacker = getAttacker(event);
		Player attacked = (Player) event.getEntity();
		if (Variables.pvpBlood)
			attacked.getWorld().playEffect(attacked.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
		if (Variables.inCombatEnabled && !Variables.worldsExcluded.contains(event.getEntity().getWorld().getName())) {
			if (!cm.isInCombat(attacker) && !cm.isInCombat(attacked)) {
				cm.inCombat(attacker, attacked);
			}
		}
	}

	public Player getAttacker(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Projectile)
			return (Player) ((Projectile) event.getDamager()).getShooter();
		else
			return (Player) event.getDamager();
	}

	public boolean worldGuardEnabled() {
		if (wg != null)
			return true;
		else
			return false;
	}
}