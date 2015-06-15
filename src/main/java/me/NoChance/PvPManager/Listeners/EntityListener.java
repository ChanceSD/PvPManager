package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Utils.CancelResult;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.libraryaddict.disguise.DisguiseAPI;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

import com.earth2me.essentials.Essentials;
import com.sk89q.commandbook.CommandBook;
import com.sk89q.commandbook.GodComponent;

public class EntityListener implements Listener {

	private final PlayerHandler ph;
	private GodComponent gc;
	private Essentials ess;

	public EntityListener(final PlayerHandler ph) {
		this.ph = ph;
		if (Bukkit.getPluginManager().isPluginEnabled("CommandBook"))
			this.gc = (GodComponent) CommandBook.inst().getComponentManager().getComponent("god");
		if (Bukkit.getPluginManager().isPluginEnabled("Essentials"))
			this.ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public final void onPlayerDamage(final EntityDamageByEntityEvent event) { // NO_UCD
		if (!CombatUtils.isWorldAllowed(event.getEntity().getWorld().getName()))
			return;
		if (Variables.isNewbieGodMode() && event.getEntity() instanceof Player && ph.get((Player) event.getEntity()).isNewbie()) {
			event.setCancelled(true);
			return;
		}
		if (!CombatUtils.isPvP(event))
			return;
		final Player attacker = getAttacker(event);
		final Player attacked = (Player) event.getEntity();
		final CancelResult result = ph.tryCancel(attacker, attacked);

		if (result != CancelResult.FAIL && result != CancelResult.FAIL_OVERRIDE)
			event.setCancelled(true);

		switch (result) {
		case FAIL_OVERRIDE:
		case FAIL:
			break;
		case NEWBIE:
			attacker.sendMessage(result.attackerCaused() ? Messages.newbieBlocked() : Messages.newbieBlockedOther(attacked.getName()));
			break;
		case PVPDISABLED:
			attacker.sendMessage(result.attackerCaused() ? Messages.pvpDisabled() : Messages.pvpDisabledOther(attacked.getName()));
			break;
		default:
			break;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerDamageOverride(final EntityDamageByEntityEvent event) { // NO_UCD
		if (!CombatUtils.isPvP(event) || !CombatUtils.isWorldAllowed(event.getEntity().getWorld().getName()) || !event.isCancelled())
			return;

		if (ph.tryCancel(getAttacker(event), (Player) event.getEntity()).equals(CancelResult.FAIL_OVERRIDE))
			event.setCancelled(false);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public final void onPlayerDamageMonitor(final EntityDamageByEntityEvent event) { // NO_UCD
		if (!CombatUtils.isPvP(event) || !CombatUtils.isWorldAllowed(event.getEntity().getWorld().getName()))
			return;
		final Player attacker = getAttacker(event);
		final Player attacked = (Player) event.getEntity();

		final CancelResult result = ph.tryCancel(attacker, attacked);

		if (result == CancelResult.FAIL || result == CancelResult.FAIL_OVERRIDE)
			onDamageActions(attacker, attacked);
	}

	private void onDamageActions(final Player attacker, final Player defender) {
		final PvPlayer pvpAttacker = ph.get(attacker);
		final PvPlayer pvpDefender = ph.get(defender);
		if (pvpAttacker == null || pvpDefender == null)
			return;
		if (Variables.isPvpBlood())
			defender.getWorld().playEffect(defender.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
		if (!attacker.hasPermission("pvpmanager.nodisable")) {
			if (Variables.isDisableFly()) {
				if (attacker.isFlying() || attacker.getAllowFlight())
					pvpAttacker.disableFly();
				if (defender.isFlying() || defender.getAllowFlight())
					pvpDefender.disableFly();
			}
			if (Variables.isDisableGamemode() && !attacker.getGameMode().equals(GameMode.SURVIVAL))
				attacker.setGameMode(GameMode.SURVIVAL);
			if (Variables.isDisableDisguise()) {
				if (Bukkit.getPluginManager().isPluginEnabled("DisguiseCraft") && DisguiseCraft.getAPI().isDisguised(attacker))
					DisguiseCraft.getAPI().undisguisePlayer(attacker);
				if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises") && DisguiseAPI.isDisguised(attacker))
					DisguiseAPI.undisguiseToAll(attacker);
			}
			if (Variables.isDisableInvisibility() && attacker.hasPotionEffect(PotionEffectType.INVISIBILITY))
				attacker.removePotionEffect(PotionEffectType.INVISIBILITY);
			if (gc != null && gc.hasGodMode(attacker))
				gc.disableGodMode(attacker);
			if (ess != null && ess.getUser(attacker).isGodModeEnabled())
				ess.getUser(attacker).setGodModeEnabled(false);
		}
		if (Variables.isInCombatEnabled()) {
			if (Variables.isOnlyTagAttacker()) {
				pvpAttacker.setTagged(true, pvpDefender.getName());
				return;
			}
			pvpAttacker.setTagged(true, pvpDefender.getName());
			pvpDefender.setTagged(false, pvpAttacker.getName());
		}
	}

	@EventHandler
	public final void onPotionSplash(final PotionSplashEvent event) { // NO_UCD (unused code)
		final ThrownPotion potion = event.getPotion();
		if (event.getAffectedEntities().isEmpty() || !(potion.getShooter() instanceof Player))
			return;

		for (final PotionEffect effect : potion.getEffects()) {
			if (effect.getType().equals(PotionEffectType.POISON)) {
				for (final LivingEntity e : event.getAffectedEntities()) {
					if (e instanceof Player && !ph.get((Player) e).hasPvPEnabled()) {
						event.setIntensity(e, 0);
					}
				}
				return;
			}
		}
	}

	private Player getAttacker(final EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Projectile)
			return (Player) ((Projectile) event.getDamager()).getShooter();
		return (Player) event.getDamager();
	}
}
