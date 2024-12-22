package me.chancesd.pvpmanager.listener;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.RegionDependency;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;

public class MoveListener implements Listener {

	private final PlayerManager ph;
	private final RegionDependency wg;
	private final Cache<UUID, Player> cache = CacheBuilder.newBuilder().weakValues().expireAfterWrite(1, TimeUnit.SECONDS).build();
	private final double pushbackForce = Conf.PUSHBACK_FORCE.asDouble();

	public MoveListener(final PlayerManager ph) {
		this.ph = ph;
		wg = (RegionDependency) ph.getPlugin().getDependencyManager().getDependency(Hook.WORLDGUARD);
	}

	@EventHandler(ignoreCancelled = true)
	public final void onPlayerMove(final PlayerMoveEvent event) {
		final Location locTo = event.getTo();
		final Location locFrom = event.getFrom();
		if (locTo == null || locFrom.getBlockX() == locTo.getBlockX() && locFrom.getBlockZ() == locTo.getBlockZ()
				&& locFrom.getBlockY() == locTo.getBlockY())
			return;

		final Player player = event.getPlayer();
		final CombatPlayer pvplayer = ph.get(player);
		if (!pvplayer.isInCombat())
			return;

		if (!wg.canAttackAt(null, locTo) && wg.canAttackAt(null, locFrom)) {
			final Vector newVel = locFrom.toVector().subtract(locTo.toVector());
			newVel.setY(newVel.getY() + 0.1).normalize().multiply(pushbackForce);
			player.setVelocity(newVel);
			if (!cache.asMap().containsKey(event.getPlayer().getUniqueId())) {
				pvplayer.message(Lang.PUSHBACK_WARNING);
				cache.put(player.getUniqueId(), player);
			}
		}
	}

}
