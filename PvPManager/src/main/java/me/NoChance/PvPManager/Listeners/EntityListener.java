package me.NoChance.PvPManager.Listeners;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.WorldGuardHook;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Player.CancelResult;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;

public class EntityListener implements Listener {

	private final PlayerHandler ph;
	private final WorldGuardHook wg;

	public EntityListener(final PlayerHandler ph) {
		this.ph = ph;
		this.wg = (WorldGuardHook) ph.getPlugin().getDependencyManager().getDependency(Hook.WORLDGUARD);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public final void onPlayerDamage(final EntityDamageByEntityEvent event) {
		if (CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName()))
			return;
		if (!CombatUtils.isPvP(event)) {
			if (event.getEntity() instanceof Player && ph.get((Player) event.getEntity()).isNewbie() && Settings.isNewbieGodMode()) {
				event.setCancelled(true);
			}
			return;
		}

		final Player attacker = getAttacker(event.getDamager());
		final Player attacked = (Player) event.getEntity();
		final CancelResult result = ph.tryCancel(attacker, attacked);

		if (result != CancelResult.FAIL && result != CancelResult.FAIL_OVERRIDE) {
			event.setCancelled(true);
			Messages.messageProtection(result, attacker, attacked);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerDamageOverride(final EntityDamageByEntityEvent event) {
		if (!CombatUtils.isPvP(event) || CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName()) || !event.isCancelled())
			return;

		if (ph.tryCancel(getAttacker(event.getDamager()), (Player) event.getEntity()).equals(CancelResult.FAIL_OVERRIDE)) {
			event.setCancelled(false);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public final void onPlayerDamageMonitor(final EntityDamageByEntityEvent event) {
		if (!CombatUtils.isPvP(event) || CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName()))
			return;
		final Player attacker = getAttacker(event.getDamager());
		final Player attacked = (Player) event.getEntity();

		onDamageActions(attacker, attacked);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public final void onEntityCombust(final EntityCombustByEntityEvent event) {
		if (CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName()))
			return;
		if (!CombatUtils.isPvP(event)) {
			if (event.getEntity() instanceof Player && ph.get((Player) event.getEntity()).isNewbie() && Settings.isNewbieGodMode()) {
				event.setCancelled(true);
			}
			return;
		}

		final Player attacker = getAttacker(event.getCombuster());
		final Player attacked = (Player) event.getEntity();

		if (!ph.canAttack(attacker, attacked)) {
			event.setCancelled(true);
		}
	}

	private void onDamageActions(final Player attacker, final Player defender) {
		final PvPlayer pvpAttacker = ph.get(attacker);
		final PvPlayer pvpDefender = ph.get(defender);

		if (Settings.isPvpBlood()) {
			defender.getWorld().playEffect(defender.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
		}
		if (!attacker.hasPermission("pvpmanager.nodisable")) {
			if (Settings.isDisableFly()) {
				if (CombatUtils.canFly(attacker)) {
					pvpAttacker.disableFly();
				}
				if (!defender.hasPermission("pvpmanager.nodisable") && CombatUtils.canFly(defender)) {
					pvpDefender.disableFly();
				}
			}
			if (Settings.isDisableGamemode() && !attacker.getGameMode().equals(GameMode.SURVIVAL)) {
				attacker.setGameMode(GameMode.SURVIVAL);
			}
			if (Settings.isDisableInvisibility() && attacker.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
				attacker.removePotionEffect(PotionEffectType.INVISIBILITY);
			}
			if (Settings.isDisableGodMode()) {
				ph.getPlugin().getDependencyManager().disableGodMode(attacker);
			}
		}
		if (Settings.isInCombatEnabled()) {
			if (Settings.borderHoppingVulnerable() && wg != null && !Settings.borderHoppingResetCombatTag()) {
				if (wg.hasDenyPvPFlag(attacker) && wg.hasDenyPvPFlag(defender))
					return;
			}
			pvpAttacker.setTagged(true, pvpDefender);
			pvpDefender.setTagged(false, pvpAttacker);
		}
	}

	@EventHandler
	public final void onPotionSplash(final PotionSplashEvent event) {
		final ThrownPotion potion = event.getPotion();
		if (event.getAffectedEntities().isEmpty() || !(potion.getShooter() instanceof Player))
			return;

		for (final PotionEffect effect : potion.getEffects())
			if (effect.getType().equals(PotionEffectType.POISON)) {
				for (final LivingEntity e : event.getAffectedEntities())
					if (e instanceof Player && !ph.get((Player) e).hasPvPEnabled()) {
						event.setIntensity(e, 0);
					}
				return;
			}
	}

	private Player getAttacker(final Entity damager) {
		if (damager instanceof Projectile)
			return (Player) ((Projectile) damager).getShooter();
		return (Player) damager;
	}

}
