package me.chancesd.pvpmanager.listener;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import me.chancesd.pvpmanager.manager.DependencyManager;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.tasks.RegionCheckTask;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.sdutils.scheduler.ScheduleUtils;
import me.chancesd.sdutils.utils.Log;

public class MoveListener1_9 implements Listener {

	private final PlayerManager playerManager;
	private final DependencyManager depManager;
	private final Cache<UUID, Player> cache = CacheBuilder.newBuilder().weakValues().expireAfterWrite(1, TimeUnit.SECONDS).build();
	private final double pushbackForce;

	public MoveListener1_9(final PlayerManager playerManager, final DependencyManager depManager) {
		this.playerManager = playerManager;
		this.depManager = depManager;
		final double force = Conf.PUSHBACK_FORCE.asDouble();
		if (force > 4) {
			Log.infoColor(ChatColor.RED + "Pushback force too high, setting it to 4");
			pushbackForce = 4;
		} else if (force < 0.1) {
			Log.infoColor(ChatColor.RED + "Pushback force too low, setting it to 0.1");
			pushbackForce = 0.1;
		} else {
			pushbackForce = force;
		}
		final RegionCheckTask regionCheckTask = new RegionCheckTask(playerManager, depManager);
		Bukkit.getPluginManager().registerEvents(regionCheckTask, playerManager.getPlugin());
		ScheduleUtils.runPlatformTaskTimer(regionCheckTask, 20, 20);
	}

	@EventHandler(ignoreCancelled = true)
	public final void onPlayerMove(final PlayerMoveEvent event) {
		final Location locTo = event.getTo();
		final Location locFrom = event.getFrom();
		if (locTo == null || locFrom.getBlockX() == locTo.getBlockX() && locFrom.getBlockZ() == locTo.getBlockZ() && locFrom.getBlockY() == locTo.getBlockY())
			return;

		final Player player = event.getPlayer();
		final CombatPlayer pvplayer = playerManager.get(player);
		if (!pvplayer.isInCombat())
			return;

		if (!depManager.canAttackAt(null, locTo) && depManager.canAttackAt(null, locFrom)) {
			final Vector newVel = locFrom.toVector().subtract(locTo.toVector());
			newVel.setY(0).normalize().multiply(pushbackForce).setY(0.5);
			CombatUtils.checkGlide(player);
			player.setVelocity(sanitizeVector(newVel));
			if (!cache.asMap().containsKey(player.getUniqueId())) {
				pvplayer.message(Lang.PUSHBACK_WARNING);
				player.getWorld().playEffect(player.getEyeLocation(), Effect.SMOKE, 3);
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
