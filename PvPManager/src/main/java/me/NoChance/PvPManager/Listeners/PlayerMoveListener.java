package me.NoChance.PvPManager.Listeners;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import me.NoChance.PvPManager.Managers.DependencyManager;
import me.NoChance.PvPManager.Managers.PlayerHandler;

public class PlayerMoveListener implements Listener {

	private final PlayerHandler ph;
	private final DependencyManager dm;
	private final Cache<UUID, Player> cache = CacheBuilder.newBuilder().weakValues().expireAfterWrite(250, TimeUnit.MILLISECONDS).build();

	public PlayerMoveListener(final PlayerHandler ph) {
		this.ph = ph;
		this.dm = ph.getPlugin().getDependencyManager();
	}

	@EventHandler
	public final void onPlayerMove(final PlayerMoveEvent event) {
		if (!ph.get(event.getPlayer()).isInCombat())
			return;
		if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ() && event.getFrom().getBlockY() == event.getTo().getBlockY())
			return;

		if (!dm.worldguardCanAttack(event.getPlayer())) {
			if (!cache.asMap().containsKey(event.getPlayer().getUniqueId())) {
				final Vector newVel = event.getFrom().toVector().subtract(event.getTo().toVector());
				newVel.setY(newVel.getY() + 0.1).normalize().multiply(1.5);

				event.getPlayer().setVelocity(newVel);
				event.getPlayer().sendMessage("§cYou can't run away to safezones while in PvP!");

				cache.put(event.getPlayer().getUniqueId(), event.getPlayer());
			}
		}
	}

}
