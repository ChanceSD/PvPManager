package me.chancesd.pvpmanager.listener;

import java.util.concurrent.TimeUnit;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.LightningStrikeEvent.Cause;
import org.bukkit.potion.PotionEffectType;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.WorldGuardDependency;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.ProtectionResult;
import me.chancesd.pvpmanager.player.ProtectionType;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.Settings;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.MCVersion;

public class DebugEntityListener implements Listener {

	private final PlayerManager ph;
	private final WorldGuardDependency wg;
	private final Cache<LightningStrike, Location> lightningCache = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.SECONDS).build();

	public DebugEntityListener(final PlayerManager ph) {
		this.ph = ph;
		this.wg = (WorldGuardDependency) ph.getPlugin().getDependencyManager().getDependency(Hook.WORLDGUARD);
	}

	private String getEntityNames(final EntityDamageByEntityEvent event) {
		return event.getDamager().getName() + " and " + event.getEntity().getName();
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public final void onPlayerDamage(final EntityDamageByEntityEvent event) {
		Log.debug("Processing damage between " + getEntityNames(event) + ". Cause: " + event.getCause());
		final String worldName = event.getEntity().getWorld().getName();
		if (CombatUtils.isWorldExcluded(worldName)) {
			Log.debug("Ignoring damage between " + getEntityNames(event) + " because world " + worldName + " is excluded");
			return;
		}

		if (!CombatUtils.isPvP(event)) {
			if (!(event.getEntity() instanceof Player)) {
				Log.debug("Ignoring damage because it wasn't considered PvP");
				return;
			}
			final CombatPlayer attacked = ph.get((Player) event.getEntity());
			if (attacked.isNewbie() && Settings.isNewbieGodMode()) {
				event.setCancelled(true);
				Log.debug("Blocking damage to newbie because newbie god mode is enabled");
				return;
			} else if (event.getDamager() instanceof final LightningStrike lightning) {
				if (!lightningCache.asMap().containsKey(lightning)) {
					Log.debug("Ignoring damage because it wasn't considered PvP");
					return;
				}
				if (!attacked.hasPvPEnabled() || attacked.isNewbie() || attacked.hasRespawnProtection()) {
					event.setCancelled(true);
					Log.debug("Blocking lightning damage because it came from a player");
					return;
				}
			}
			Log.debug("Ignoring damage because it wasn't considered PvP");
			return;
		}

		final Player attacker = getAttacker(event.getDamager());
		final Player attacked = (Player) event.getEntity();
		final ProtectionResult result = ph.checkProtection(attacker, attacked);

		if (result.isProtected()) {
			event.setCancelled(true);
			Log.debug("Blocking PvP damage. Protection result: " + result);
		} else {
			Log.debug("Allowing PvP because neither player had protection. Protection result: " + result);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerDamageOverride(final EntityDamageByEntityEvent event) {
		if (!CombatUtils.isPvP(event) || CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName()) || !event.isCancelled())
			return;

		if (ph.checkProtection(getAttacker(event.getDamager()), (Player) event.getEntity()).type() == ProtectionType.FAIL_OVERRIDE) {
			event.setCancelled(false);
			Log.debug("Force allowing PvP even though a plugin blocked it because a player has override or Vulnerable is enabled");
		} else {
			Log.debug("Damage was cancelled by another plugin, doing nothing");
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public final void onPlayerDamageMonitor(final EntityDamageByEntityEvent event) {
		if (!CombatUtils.isPvP(event) || CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName()))
			return;
		final Player attacker = getAttacker(event.getDamager());
		final Player attacked = (Player) event.getEntity();

		onDamageActions(attacker, attacked);
		Log.debug("Finished processing damage.");
	}

	@SuppressWarnings("null") // defender.getLocation() never null
	public void onDamageActions(final Player attacker, final Player defender) {
		final CombatPlayer pvpAttacker = ph.get(attacker);
		final CombatPlayer pvpDefender = ph.get(defender);

		if (Settings.isPvpBlood()) {
			defender.getWorld().playEffect(defender.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
		}
		if (!pvpAttacker.hasPerm(Permissions.EXEMPT_DISABLE_ACTIONS)) {
			if (Settings.isDisableFly()) {
				if (CombatUtils.canFly(attacker)) {
					pvpAttacker.disableFly();
				}
				if (!pvpDefender.hasPerm(Permissions.EXEMPT_DISABLE_ACTIONS) && CombatUtils.canFly(defender)) {
					pvpDefender.disableFly();
				}
			}
			if (Settings.isDisableGamemode() && !attacker.getGameMode().equals(GameMode.SURVIVAL)) {
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
		} else {
			Log.debug("Didn't disable fly/gamemode/etc because attacker has permission " + Permissions.EXEMPT_DISABLE_ACTIONS);
		}
		if (Settings.isInCombatEnabled()) {
			if (Settings.borderHoppingVulnerable() && wg != null && !Settings.borderHoppingResetCombatTag() && wg.hasDenyPvPFlag(attacker)
					&& wg.hasDenyPvPFlag(defender)) {
					return;
			}
			Log.debug("Tagging players " + pvpAttacker.getName() + " and " + pvpDefender.getName());
			pvpAttacker.setTagged(true, pvpDefender);
			pvpDefender.setTagged(false, pvpAttacker);
		}
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
