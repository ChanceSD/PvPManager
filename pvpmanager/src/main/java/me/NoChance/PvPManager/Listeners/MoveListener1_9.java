package me.NoChance.PvPManager.Listeners;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.RegionDependency;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Utils.CombatUtils;

public class MoveListener1_9 implements Listener {

	private final PlayerHandler ph;
	private final RegionDependency wg;
	private final Cache<UUID, Player> cache = CacheBuilder.newBuilder().weakValues().expireAfterWrite(1, TimeUnit.SECONDS).build();

	public MoveListener1_9(final PlayerHandler ph) {
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
			newVel.setY(0).normalize().multiply(1.6).setY(0.5);
			CombatUtils.checkGlide(player);
			player.setVelocity(newVel);
			if (!cache.asMap().containsKey(event.getPlayer().getUniqueId())) {
				pvplayer.message(Messages.getPushbackWarning());
				event.getFrom().getWorld().playEffect(player.getEyeLocation(), Effect.SMOKE, 3);
				cache.put(player.getUniqueId(), player);
			}
		}
	}

}
