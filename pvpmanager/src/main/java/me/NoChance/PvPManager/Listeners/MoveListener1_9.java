package me.NoChance.PvPManager.Listeners;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.Interfaces.RegionDependency;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Utils.CombatUtils;

public class MoveListener1_9 implements Listener {

	private final PlayerHandler playerManager;
	private final DependencyManager depManager;
	private final Cache<UUID, Player> cache = CacheBuilder.newBuilder().weakValues().expireAfterWrite(1, TimeUnit.SECONDS).build();

	public MoveListener1_9(final PlayerHandler playerManager, final DependencyManager depManager) {
		this.playerManager = playerManager;
		this.depManager = depManager;
	}

	@EventHandler(ignoreCancelled = true)
	public final void onPlayerMove(final PlayerMoveEvent event) {
		if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()
				&& event.getFrom().getBlockY() == event.getTo().getBlockY())
			return;

		final Player player = event.getPlayer();
		final PvPlayer pvplayer = playerManager.get(player);
		if (!pvplayer.isInCombat())
			return;

		if (!depManager.canAttackAt(null, event.getTo()) && depManager.canAttackAt(null, event.getFrom())) {
			final Vector newVel = event.getFrom().toVector().subtract(event.getTo().toVector());
			newVel.setY(0).normalize().multiply(1.6).setY(0.5);
			CombatUtils.checkGlide(player);
			player.setVelocity(sanitizeVector(newVel));
			if (!cache.asMap().containsKey(event.getPlayer().getUniqueId())) {
				pvplayer.message(Messages.getPushbackWarning());
				event.getFrom().getWorld().playEffect(player.getEyeLocation(), Effect.SMOKE, 3);
				cache.put(player.getUniqueId(), player);
			}
		}
	}

	@NotNull
	private Vector sanitizeVector(@NotNull final Vector vel) {
		if (Double.isNaN(vel.getX()))
			vel.setX(0);
		if (Double.isNaN(vel.getZ()))
			vel.setZ(0);
		return vel;
	}

}
