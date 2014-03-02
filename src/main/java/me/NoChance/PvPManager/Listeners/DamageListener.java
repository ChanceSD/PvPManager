package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Managers.WorldTimerManager;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.NoChance.PvPManager.Utils.Utils;
import me.NoChance.PvPManager.Utils.WGDependency;
import me.libraryaddict.disguise.DisguiseAPI;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.utils.CombatUtil;

public class DamageListener implements Listener {

	public enum CancelResult {
		NEWBIE, NEWBIE_OTHER, PVPDISABLED, PVPDISABLED_OTHER, PVPTIMER, FAIL, FAIL_OVERRIDE
	}

	private WGDependency wg;
	private WorldTimerManager wm;
	private PlayerHandler ph;
	private Towny towny;

	public DamageListener(PvPManager pvpManager) {
		this.wm = pvpManager.getWtm();
		this.ph = pvpManager.getPlayerHandler();
		if (Utils.isWGEnabled())
			this.wg = new WGDependency(pvpManager);
		if (Utils.isTownyEnabled()) {
			this.towny = (Towny) Bukkit.getPluginManager().getPlugin("Towny");
			pvpManager.getLogger().info("Towny Found! Enabling Towny Support");
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void playerDamageListener(EntityDamageByEntityEvent event) {
		if (!CombatUtils.isPvP(event) || !Utils.PMAllowed(event.getEntity().getWorld().getName()))
			return;
		PvPlayer attacker = ph.get(getAttacker(event));
		PvPlayer attacked = ph.get((Player) event.getEntity());
		CancelResult result = tryCancel(attacker, attacked);

		if (result != CancelResult.FAIL && result != CancelResult.FAIL_OVERRIDE)
			event.setCancelled(true);

		switch (result) {
		case FAIL_OVERRIDE:
			if (event.isCancelled())
				event.setCancelled(false);
		case FAIL:
			onDamageActions(event);
			break;
		case NEWBIE:
			attacker.message(Messages.Newbie_Protection_On_Hit);
			break;
		case NEWBIE_OTHER:
			attacker.message(Messages.Newbie_Protection_Atacker.replace("%p", attacked.getName()));
			break;
		case PVPDISABLED:
			attacker.message(Messages.Attack_Denied_You);
			break;
		case PVPDISABLED_OTHER:
			attacker.message(Messages.Attack_Denied_Other.replace("%p", attacked.getName()));
			break;
		case PVPTIMER:
			break;
		}
	}

	private void onDamageActions(EntityDamageByEntityEvent event) {
		Player attacker = getAttacker(event);
		Player attacked = (Player) event.getEntity();
		PvPlayer pvpAttacker = ph.get(attacker);
		PvPlayer pvpAttacked = ph.get(attacked);
		if (Variables.pvpBlood)
			attacked.getWorld().playEffect(attacked.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
		if (!attacker.hasPermission("pvpmanager.nodisable")) {
			if (Variables.disableFly && (attacker.isFlying() || attacker.getAllowFlight()))
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

	public CancelResult tryCancel(PvPlayer attacker, PvPlayer attacked) {
		if (attacker.hasOverride())
			return CancelResult.FAIL_OVERRIDE;

		if (Utils.isWGEnabled())
			if (wg.hasWGPvPFlag(attacked.getPlayer().getWorld(), attacked.getPlayer().getLocation()))
				return CancelResult.FAIL;

		if (Utils.isTownyEnabled() && Variables.townySupport)
			if (!CombatUtil.preventDamageCall(towny, attacker.getPlayer(), attacked.getPlayer()))
				return CancelResult.FAIL;

		if (Variables.pvpTimerEnabled)
			if (wm.isPvpTimerWorld(attacker.getWorldName()))
				if (!wm.isTimeForPvp(attacker.getWorldName()))
					return CancelResult.PVPTIMER;

		if (attacked.isNewbie())
			return CancelResult.NEWBIE_OTHER;
		if (attacker.isNewbie())
			return CancelResult.NEWBIE;
		if (!attacked.hasPvPEnabled())
			return CancelResult.PVPDISABLED_OTHER;
		if (!attacker.hasPvPEnabled())
			return CancelResult.PVPDISABLED;

		return CancelResult.FAIL;
	}

	private Player getAttacker(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Projectile)
			return (Player) ((Projectile) event.getDamager()).getShooter();
		else
			return (Player) event.getDamager();
	}

}