package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.NoChance.PvPManager.Utils.CombatUtils.CancelResult;
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

public class DamageListener implements Listener {

	private PlayerHandler ph;

	public DamageListener(PvPManager pvpManager) {
		this.ph = pvpManager.getPlayerHandler();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void playerDamageListener(EntityDamageByEntityEvent event) {
		if (!CombatUtils.isPvP(event) || !Utils.PMAllowed(event.getEntity().getWorld().getName()))
			return;
		Player attacker = getAttacker(event);
		Player attacked = (Player) event.getEntity();
		CancelResult result = CombatUtils.tryCancel(attacker, attacked);

		if (result != CancelResult.FAIL && result != CancelResult.FAIL_OVERRIDE)
			event.setCancelled(true);

		switch (result) {
		case FAIL_OVERRIDE:
			if (event.isCancelled())
				event.setCancelled(false);
		case FAIL:
			if (!event.isCancelled())
				onDamageActions(attacker, attacked);
			break;
		case NEWBIE:
			attacker.sendMessage(Messages.Newbie_Protection_On_Hit);
			break;
		case NEWBIE_OTHER:
			attacker.sendMessage(Messages.Newbie_Protection_Atacker.replace("%p", attacked.getName()));
			break;
		case PVPDISABLED:
			attacker.sendMessage(Messages.Attack_Denied_You);
			break;
		case PVPDISABLED_OTHER:
			attacker.sendMessage(Messages.Attack_Denied_Other.replace("%p", attacked.getName()));
			break;
		case PVPTIMER:
			break;
		}
	}

	private void onDamageActions(Player attacker, Player attacked) {
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
			if (Variables.onlyTagAttacker) {
				pvpAttacker.setTagged(true);
				return;
			} else {
				pvpAttacker.setTagged(true);
				pvpAttacked.setTagged(true);
			}
		}
	}

	private Player getAttacker(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Projectile)
			return (Player) ((Projectile) event.getDamager()).getShooter();
		else
			return (Player) event.getDamager();
	}

}