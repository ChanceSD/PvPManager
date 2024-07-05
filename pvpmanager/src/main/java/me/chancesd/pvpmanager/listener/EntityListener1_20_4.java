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
		if (entityDefender instanceof Player) {
			attacked = (Player) entityDefender;
			if (entityAttacker instanceof Player)
				attacker = (Player) entityAttacker;
			if (entityAttacker instanceof Projectile) {
				final ProjectileSource projSource = ((Projectile) entityAttacker).getShooter();
				if (projSource instanceof Player && !projSource.equals(entityDefender)) {
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
