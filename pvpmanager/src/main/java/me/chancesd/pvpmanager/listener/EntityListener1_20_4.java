package me.chancesd.pvpmanager.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityKnockbackByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.ProtectionResult;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.sdutils.utils.Utils;

public class EntityListener1_20_4 implements Listener {

	private final PlayerManager ph;

	public EntityListener1_20_4(final PlayerManager ph) {
		this.ph = ph;
		registerKnockBackEvent();
	}

	private void registerKnockBackEvent() {
		if (Utils.isPaper()) {
			Utils.registerEvent(ph.getPlugin(), this, com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent.class,
					event -> onPlayerKnockback(event, event.getHitBy(), event.getEntity()));
		} else {
			Utils.registerEvent(ph.getPlugin(), this, EntityKnockbackByEntityEvent.class,
					event -> onPlayerKnockback(event, event.getSourceEntity(), event.getEntity()));
		}
	}

	private void onPlayerKnockback(final Cancellable event, final Entity entityAttacker, final Entity entityDefender) {
		if (CombatUtils.isWorldExcluded(entityDefender.getWorld().getName()))
			return;

		Player attacker = null;
		Player attacked = null;
		if (entityDefender instanceof final Player defender) {
			attacked = defender;
			if (entityAttacker instanceof final Player playerAttacker)
				attacker = playerAttacker;
			if (entityAttacker instanceof final Projectile proj) {
				final ProjectileSource projSource = proj.getShooter();
				if (projSource instanceof Player && !projSource.equals(attacked)) {
					attacker = (Player) projSource;
				}
			}
		}
		if (attacked == null || attacker == null)
			return;
		final ProtectionResult result = ph.checkProtection(attacker, attacked);

		if (result.isProtected()) {
			event.setCancelled(true);
		}
	}

}
