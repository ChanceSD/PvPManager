package me.NoChance.PvPManager.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Settings;

public class PlayerListener1_11 implements Listener {

	private final PlayerHandler playerHandler;

	public PlayerListener1_11(final PlayerHandler ph) {
		this.playerHandler = ph;
	}

	@EventHandler
	public void onProjectileLaunchEvent(final ProjectileLaunchEvent event) {
		final Projectile entity = event.getEntity();
		final ProjectileSource shooter = entity.getShooter();
		if (Settings.getEnderPearlCooldown() < 0 || entity.getType() != EntityType.ENDER_PEARL || !(shooter instanceof Player))
			return;

		final Player player = (Player) shooter;
		final PvPlayer pvPlayer = playerHandler.get(player);

		if (pvPlayer.isInCombat()) {
			Bukkit.getScheduler().runTask(playerHandler.getPlugin(), () -> player.setCooldown(Material.ENDER_PEARL, Settings.getEnderPearlCooldown() * 20));
		}
	}

}
