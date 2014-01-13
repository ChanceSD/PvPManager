package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.WGDependency;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Managers.CombatManager;
import me.NoChance.PvPManager.Others.Utils;
import me.libraryaddict.disguise.DisguiseAPI;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

	private WGDependency wg;
	private CombatManager cm;

	public DamageListener(PvPManager pvpManager) {
		this.cm = pvpManager.getCm();
		if (Utils.isWGEnabled()) {
			this.wg = new WGDependency(pvpManager);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void playerDamageListener(EntityDamageByEntityEvent event) {
		if (!cm.isPvP(event) || !Utils.PMAllowed(event.getEntity().getWorld().getName()))
			return;
		Player attacker = getAttacker(event);
		Player attacked = (Player) event.getEntity();

		if (Utils.isWGEnabled())
			if (wg.hasWGPvPFlag(attacked.getWorld(), attacked.getLocation()))
				return;

		if (Variables.pvpTimerEnabled) {
			if (cm.getWtm().isPvpTimerWorld(attacker.getWorld())) {
				if (!cm.getWtm().isTimeForPvp(attacker.getWorld())) {
					event.setCancelled(true);
					return;
				}
			}
		}
		if (Variables.killAbuseEnabled) {
			if (cm.getKillAbusers().containsKey(attacker.getName())) {
				if (cm.getKillAbusers().get(attacker.getName()).equals(attacked.getName())) {
					attacker.sendMessage("§6[§8PvPManager§6]§4 Attack blocked due to kill abuse!");
					event.setCancelled(true);
					return;
				}
			}
		}
		if (cm.isNewbie(attacked) || cm.isNewbie(attacker)) {
			event.setCancelled(true);
			if (cm.isNewbie(attacked))
				attacker.sendMessage(Messages.Newbie_Protection_Atacker.replace("%p", attacked.getName()));
			else
				attacker.sendMessage(Messages.Newbie_Protection_On_Hit);
			return;
		}
		if (!cm.hasPvpEnabled(attacked.getName())) {
			event.setCancelled(true);
			attacker.sendMessage(Messages.Attack_Denied_Other.replace("%p", attacked.getName()));
			return;
		} else if (!cm.hasPvpEnabled(attacker.getName()) && !attacker.hasPermission("pvpmanager.override")) {
			event.setCancelled(true);
			attacker.sendMessage(Messages.Attack_Denied_You);
			return;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void damageListenerHighest(EntityDamageByEntityEvent event) {
		if (!cm.isPvP(event) || !Utils.PMAllowed(event.getEntity().getWorld().getName()))
			return;
		if (getAttacker(event).hasPermission("pvpmanager.override") && event.isCancelled()) {
			event.setCancelled(false);
			return;
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void damageListenerMonitor(EntityDamageByEntityEvent event) {
		if (!cm.isPvP(event) || !Utils.PMAllowed(event.getEntity().getWorld().getName()))
			return;
		Player attacker = getAttacker(event);
		Player attacked = (Player) event.getEntity();
		if (Variables.pvpBlood)
			attacked.getWorld().playEffect(attacked.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
		if (!attacker.hasPermission("pvpmanager.nodisable")) {
			if (Variables.disableFly && attacker.isFlying())
				cm.disableFly(attacker);
			if (Variables.disableGamemode && !attacker.getGameMode().equals(GameMode.SURVIVAL))
				attacker.setGameMode(GameMode.SURVIVAL);
			if (Variables.disableDisguise) {
				if (Utils.getDisguiseCraft() != null && Utils.getDisguiseCraft().isDisguised(attacker))
					Utils.getDisguiseCraft().undisguisePlayer(attacker);
				if (Utils.isLibsDisguisesEnabled() && DisguiseAPI.isDisguised(attacker))
					DisguiseAPI.undisguiseToAll(attacker);
			}
		}
		if (Variables.inCombatEnabled) {
			if (!cm.isInCombat(attacker) && !cm.isInCombat(attacked)) {
				cm.inCombat(attacker, attacked);
			} else {
				cm.renewTag(attacker);
				cm.renewTag(attacked);
			}
		}
	}

	public Player getAttacker(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Projectile)
			return (Player) ((Projectile) event.getDamager()).getShooter();
		else
			return (Player) event.getDamager();
	}

}