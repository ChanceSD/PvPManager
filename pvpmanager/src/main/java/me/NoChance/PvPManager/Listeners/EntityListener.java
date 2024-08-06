package me.NoChance.PvPManager.Listeners;

import java.util.concurrent.TimeUnit;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.LightningStrikeEvent.Cause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.WorldGuardHook;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Player.CancelResult;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.chancesd.pvpmanager.setting.Permissions;

public class EntityListener implements Listener {

	private final PlayerHandler ph;
	private final WorldGuardHook wg;
	private final Cache<LightningStrike, Location> lightningCache = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.SECONDS).build();

	public EntityListener(final PlayerHandler ph) {
		this.ph = ph;
		this.wg = (WorldGuardHook) ph.getPlugin().getDependencyManager().getDependency(Hook.WORLDGUARD);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public final void onPlayerDamage(final EntityDamageByEntityEvent event) {
		if (CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName()))
			return;
		if (!CombatUtils.isPvP(event)) {
			if (!(event.getEntity() instanceof Player))
				return;

			final PvPlayer attacked = ph.get((Player) event.getEntity());
			if (attacked.isNewbie() && Settings.isNewbieGodMode()) {
				event.setCancelled(true);
			} else if (event.getDamager() instanceof LightningStrike) {
				final LightningStrike lightning = (LightningStrike) event.getDamager();
				if (!lightningCache.asMap().containsKey(lightning))
					return;
				if (!attacked.hasPvPEnabled() || attacked.isNewbie() || attacked.hasRespawnProtection()) {
					event.setCancelled(true);
				}
			}
			return;
		}

		final Player attacker = getAttacker(event.getDamager());
		final Player attacked = (Player) event.getEntity();
		final CancelResult result = ph.tryCancel(attacker, attacked);

		if (result.isProtected()) {
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

		processDamage(attacker, attacked);
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

	public void processDamage(final Player attacker, final Player defender) {
		final PvPlayer pvpAttacker = ph.get(attacker);
		final PvPlayer pvpDefender = ph.get(defender);

		if (Settings.isPvpBlood()) {
			defender.getWorld().playEffect(defender.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
		}
		disableActions(attacker, defender, pvpAttacker, pvpDefender);
		if (Settings.isInCombatEnabled()) {
			if (Settings.borderHoppingVulnerable() && wg != null && !Settings.borderHoppingResetCombatTag() && wg.hasDenyPvPFlag(attacker)
					&& wg.hasDenyPvPFlag(defender)) {
				return;
			}
			pvpAttacker.setTagged(true, pvpDefender);
			pvpDefender.setTagged(false, pvpAttacker);
		}
	}

	private void disableActions(final Player attacker, final Player defender, final PvPlayer pvpAttacker, final PvPlayer pvpDefender) {
		final boolean hasExemptPerm = pvpAttacker.hasPerm(Permissions.EXEMPT_DISABLE_ACTIONS);
		if (Settings.isDisableFly()) {
			if (CombatUtils.canFly(attacker) && !hasExemptPerm) {
				pvpAttacker.disableFly();
			}
			if (CombatUtils.canFly(defender) && !pvpDefender.hasPerm(Permissions.EXEMPT_DISABLE_ACTIONS)) {
				pvpDefender.disableFly();
			}
		}
		if (Settings.isDisableElytra()) {
			if (!hasExemptPerm)
				CombatUtils.checkGlide(attacker);
			if (!pvpDefender.hasPerm(Permissions.EXEMPT_DISABLE_ACTIONS))
				CombatUtils.checkGlide(defender);
		}

		if (hasExemptPerm)
			return;
		if (Settings.isDisableGamemode() && attacker.getGameMode() != GameMode.SURVIVAL) {
			attacker.setGameMode(GameMode.SURVIVAL);
		}
		if (Settings.isDisableDisguise()) {
			ph.getPlugin().getDependencyManager().disableDisguise(attacker);
		}
		if (Settings.isDisableInvisibility() && attacker.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
			attacker.removePotionEffect(PotionEffectType.INVISIBILITY);
		}
		if (Settings.isDisableGodMode()) {
			ph.getPlugin().getDependencyManager().disableGodMode(attacker);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public final void onPotionSplash(final PotionSplashEvent event) {
		if (!shouldCheckPotionEvent(event))
			return;

		final ThrownPotion potion = event.getPotion();
		final Player player = (Player) potion.getShooter();
		for (final LivingEntity e : event.getAffectedEntities()) {
			if (e.getType() != EntityType.PLAYER || e.equals(player)) {
				continue;
			}
			final Player attacked = (Player) e;
			final CancelResult result = ph.tryCancel(player, attacked);

			if (result.isProtected()) {
				event.setIntensity(attacked, 0);
				Messages.messageProtection(result, player, attacked);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public final void onPotionSplashMonitor(final PotionSplashEvent event) {
		if (!shouldCheckPotionEvent(event))
			return;

		final ThrownPotion potion = event.getPotion();
		final Player player = (Player) potion.getShooter();
		for (final LivingEntity e : event.getAffectedEntities()) {
			if (e.getType() != EntityType.PLAYER || e.equals(player)) {
				continue;
			}
			final Player attacked = (Player) e;
			processDamage(player, attacked);
		}
	}

	private boolean shouldCheckPotionEvent(final PotionSplashEvent event) {
		if (CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName()))
			return false;

		final ThrownPotion potion = event.getPotion();
		if (event.getAffectedEntities().isEmpty() || potion.getEffects().isEmpty() || !(potion.getShooter() instanceof Player))
			return false;

		for (final PotionEffect effect : potion.getEffects()) {
			if (!CombatUtils.isHarmfulPotion(effect.getType()))
				return false;
		}
		return true;
	}

	@EventHandler(ignoreCancelled = true)
	public void onLightningStrike(final LightningStrikeEvent event) {
		if (CombatUtils.isWorldExcluded(event.getLightning().getWorld().getName()))
			return;
		if (!CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.13.1"))
			return;
		if (event.getCause() != Cause.TRIDENT)
			return;

		lightningCache.put(event.getLightning(), event.getLightning().getLocation());
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockIgnite(final BlockIgniteEvent event) {
		if (event.getCause() != IgniteCause.LIGHTNING)
			return;
		if (CombatUtils.isWorldExcluded(event.getBlock().getWorld().getName()))
			return;

		final Entity ignitingEntity = event.getIgnitingEntity();
		if (ignitingEntity instanceof LightningStrike && lightningCache.asMap().containsKey(ignitingEntity)) {
			final LightningStrike lightningStrike = (LightningStrike) ignitingEntity;
			for (final Entity entity : lightningStrike.getNearbyEntities(2, 2, 2)) {
				if (entity instanceof Player) {
					final PvPlayer attacked = ph.get((Player) entity);
					if (!attacked.hasPvPEnabled() || attacked.isNewbie() || attacked.hasRespawnProtection()) {
						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onProjectileHitEvent(final ProjectileHitEvent event) {
		final Projectile entity = event.getEntity();
		final ProjectileSource shooter = entity.getShooter();
		if (!Settings.isEnderPearlRenewTag() || entity.getType() != EntityType.ENDER_PEARL || !(shooter instanceof Player))
			return;

		final Player player = (Player) shooter;
		final PvPlayer pvPlayer = ph.get(player);

		if (pvPlayer.isInCombat()) {
			final PvPlayer enemy = pvPlayer.getEnemy();
			pvPlayer.setTagged(true, enemy != null ? enemy : pvPlayer);
		}
	}

	private Player getAttacker(final Entity damager) {
		if (damager instanceof Player)
			return (Player) damager;
		if (damager instanceof Projectile)
			return (Player) ((Projectile) damager).getShooter();
		if (damager instanceof TNTPrimed)
			return (Player) ((TNTPrimed) damager).getSource();
		return (Player) ((AreaEffectCloud) damager).getSource();
	}

}
