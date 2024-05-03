package me.chancesd.pvpmanager.listener;

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
import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.WorldGuardDependency;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.ProtectionResult;
import me.chancesd.pvpmanager.player.ProtectionType;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.Settings;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.sdutils.utils.MCVersion;

public class EntityListener implements Listener {

	private final PlayerManager playerHandler;
	private final WorldGuardDependency wg;
	private final Cache<LightningStrike, Location> lightningCache = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.SECONDS).build();

	public EntityListener(final PlayerManager ph) {
		this.playerHandler = ph;
		this.wg = (WorldGuardDependency) ph.getPlugin().getDependencyManager().getDependency(Hook.WORLDGUARD);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public final void onPlayerDamage(final EntityDamageByEntityEvent event) {
		if (CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName()))
			return;
		if (!CombatUtils.isPvP(event)) {
			if (!(event.getEntity() instanceof Player))
				return;

			final CombatPlayer attacked = playerHandler.get((Player) event.getEntity());
			if (attacked.isNewbie() && Settings.isNewbieGodMode()) {
				event.setCancelled(true);
			} else if (event.getDamager() instanceof final LightningStrike lightning) {
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
		if (attacker == null)
			return;

		final ProtectionResult result = playerHandler.checkProtection(attacker, attacked);

		if (result.isProtected()) {
			event.setCancelled(true);
			Lang.messageProtection(result, attacker, attacked);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerDamageOverride(final EntityDamageByEntityEvent event) {
		if (!CombatUtils.isPvP(event) || CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName()) || !event.isCancelled())
			return;

		final Player attacker = getAttacker(event.getDamager());
		if (attacker == null)
			return;
		if (playerHandler.checkProtection(attacker, (Player) event.getEntity()).type() == ProtectionType.FAIL_OVERRIDE) {
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
			if (event.getEntity() instanceof final Player player && playerHandler.get(player).isNewbie() && Settings.isNewbieGodMode()) {
				event.setCancelled(true);
			}
			return;
		}

		final Player attacker = getAttacker(event.getCombuster());
		final Player attacked = (Player) event.getEntity();
		if (attacker == null)
			return;

		if (!playerHandler.canAttack(attacker, attacked)) {
			event.setCancelled(true);
		}
	}

	@SuppressWarnings("null") // defender.getLocation() never null
	public void processDamage(final Player attacker, final Player defender) {
		final CombatPlayer pvpAttacker = playerHandler.get(attacker);
		final CombatPlayer pvpDefender = playerHandler.get(defender);

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

	@SuppressWarnings("null") // PotionEffectType.INVISIBILITY is not null
	private void disableActions(final Player attacker, final Player defender, final CombatPlayer pvpAttacker, final CombatPlayer pvpDefender) {
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
			playerHandler.getPlugin().getDependencyManager().disableDisguise(attacker);
		}
		if (Settings.isDisableInvisibility() && attacker.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
			attacker.removePotionEffect(PotionEffectType.INVISIBILITY);
		}
		if (Settings.isDisableGodMode()) {
			playerHandler.getPlugin().getDependencyManager().disableGodMode(attacker);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public final void onPotionSplash(final PotionSplashEvent event) {
		if (!shouldCheckPotionEvent(event))
			return;

		final ThrownPotion potion = event.getPotion();
		final Player player = (Player) potion.getShooter();
		if (player == null)
			return;
		for (final LivingEntity e : event.getAffectedEntities()) {
			if (e.getType() != EntityType.PLAYER || e.equals(player)) {
				continue;
			}
			final Player attacked = (Player) e;
			final ProtectionResult result = playerHandler.checkProtection(player, attacked);

			if (result.isProtected()) {
				event.setIntensity(attacked, 0);
				Lang.messageProtection(result, player, attacked);
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
		if (!MCVersion.isAtLeast(MCVersion.V1_13_1))
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
		if (ignitingEntity instanceof final LightningStrike lightningStrike && lightningCache.asMap().containsKey(ignitingEntity)) {
			for (final Entity entity : lightningStrike.getNearbyEntities(2, 2, 2)) {
				if (entity instanceof final Player player) {
					final CombatPlayer attacked = playerHandler.get(player);
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
		if (!Settings.isEnderPearlRenewTag() || entity.getType() != EntityType.ENDER_PEARL || !(shooter instanceof final Player player))
			return;

		final CombatPlayer pvPlayer = playerHandler.get(player);

		if (pvPlayer.isInCombat()) {
			final PvPlayer enemy = pvPlayer.getEnemy();
			pvPlayer.setTagged(true, enemy != null ? enemy : pvPlayer);
		}
	}

	@Nullable
	private Player getAttacker(final Entity damager) {
		if (damager instanceof final Player player)
			return player;
		if (damager instanceof final Projectile projectile)
			return (Player) projectile.getShooter();
		if (damager instanceof final TNTPrimed tnt)
			return (Player) tnt.getSource();
		return (Player) ((AreaEffectCloud) damager).getSource();
	}

}
