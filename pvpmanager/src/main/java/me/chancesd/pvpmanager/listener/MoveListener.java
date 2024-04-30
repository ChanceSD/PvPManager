package me.chancesd.pvpmanager.listener;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.Interfaces.RegionDependency;
import me.chancesd.pvpmanager.manager.PlayerHandler;
import me.chancesd.pvpmanager.setting.Messages;

public class MoveListener implements Listener {

	private final PlayerHandler ph;
	private final RegionDependency wg;
	private final Cache<UUID, Player> cache = CacheBuilder.newBuilder().weakValues().expireAfterWrite(1, TimeUnit.SECONDS).build();

	public MoveListener(final PlayerHandler ph) {
		this.ph = ph;
		wg = (RegionDependency) ph.getPlugin().getDependencyManager().getDependency(Hook.WORLDGUARD);
	}

	@EventHandler(ignoreCancelled = true)
	public final void onPlayerMove(final PlayerMoveEvent event) {
		if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()
				&& event.getFrom().getBlockY() == event.getTo().getBlockY())
			return;

		final Player player = event.getPlayer();
		final PvPlayer pvplayer = ph.get(player);
		if (!pvplayer.isInCombat())
			return;

		if (!wg.canAttackAt(null, event.getTo()) && wg.canAttackAt(null, event.getFrom())) {
			final Vector newVel = event.getFrom().toVector().subtract(event.getTo().toVector());
			newVel.setY(newVel.getY() + 0.1).normalize().multiply(1.5);
			player.setVelocity(newVel);
			if (!cache.asMap().containsKey(event.getPlayer().getUniqueId())) {
				pvplayer.message(Messages.getPushbackWarning());
				cache.put(player.getUniqueId(), player);
			}
		}
	}

}
