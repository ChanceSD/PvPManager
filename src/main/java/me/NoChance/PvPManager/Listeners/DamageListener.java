package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Managers.WorldTimerManager;
import me.NoChance.PvPManager.Others.WGDependency;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.NoChance.PvPManager.Utils.Utils;
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
import com.palmergames.bukkit.towny.utils.CombatUtil;

public class DamageListener implements Listener {

	private WGDependency wg;
	private WorldTimerManager wm;

	public DamageListener(PvPManager pvpManager) {
		this.wm = pvpManager.getWtm();
		if (Utils.isWGEnabled()) {
			this.wg = new WGDependency(pvpManager);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void playerDamageListener(EntityDamageByEntityEvent event) {
		if (!CombatUtils.isPvP(event) || !Utils.PMAllowed(event.getEntity().getWorld().getName()))
			return;
		PvPlayer attacker = PlayerHandler.get(getAttacker(event));
		PvPlayer attacked = PlayerHandler.get((Player) event.getEntity());

		if (Utils.isWGEnabled())
			if (wg.hasWGPvPFlag(attacked.getPlayer().getWorld(), attacked.getPlayer().getLocation()))
				return;

		if (Utils.isTownyEnabled())
			if (CombatUtil.canAttackEnemy(attacker.getName(), attacked.getName()))
				return;

		if (Variables.pvpTimerEnabled) {
			if (wm.isPvpTimerWorld(attacker.getWorldName())) {
				if (!wm.isTimeForPvp(attacker.getWorldName())) {
					event.setCancelled(true);
					return;
				}
			}
		}
		if (attacked.isNewbie() || attacker.isNewbie()) {
			event.setCancelled(true);
			if (attacked.isNewbie())
				attacker.message(Messages.Newbie_Protection_Atacker.replace("%p", attacked.getName()));
			else
				attacker.message(Messages.Newbie_Protection_On_Hit);
			return;
		}
		if (!attacked.hasPvPEnabled()) {
			event.setCancelled(true);
			attacker.message(Messages.Attack_Denied_Other.replace("%p", attacked.getName()));
			return;
		} else if (!attacker.hasPvPEnabled() && !attacker.overrideAll()) {
			event.setCancelled(true);
			attacker.message(Messages.Attack_Denied_You);
			return;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void damageListenerHighest(EntityDamageByEntityEvent event) {
		if (!CombatUtils.isPvP(event) || !Utils.PMAllowed(event.getEntity().getWorld().getName()))
			return;
		if (getAttacker(event).hasPermission("pvpmanager.override") && event.isCancelled()) {
			event.setCancelled(false);
			return;
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void damageListenerMonitor(EntityDamageByEntityEvent event) {
		if (!CombatUtils.isPvP(event) || !Utils.PMAllowed(event.getEntity().getWorld().getName()))
			return;
		Player attacker = getAttacker(event);
		Player attacked = (Player) event.getEntity();
		PvPlayer pvpAttacker = PlayerHandler.get(attacker);
		PvPlayer pvpAttacked = PlayerHandler.get(attacked);
		if (Variables.pvpBlood)
			attacked.getWorld().playEffect(attacked.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
		if (!attacker.hasPermission("pvpmanager.nodisable")) {
			if (Variables.disableFly && attacker.isFlying())
				pvpAttacker.disableFly();
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
			if (!pvpAttacker.isInCombat() && !pvpAttacked.isInCombat()) {
				if (Variables.onlyTagAttacker) {
					pvpAttacker.setTagged(true);
					return;
				} else {
					pvpAttacker.setTagged(true);
					pvpAttacked.setTagged(true);
				}
			} else {
				pvpAttacker.renewTag();
				pvpAttacked.renewTag();
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