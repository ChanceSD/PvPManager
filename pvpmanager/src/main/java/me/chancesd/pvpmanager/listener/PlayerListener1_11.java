package me.chancesd.pvpmanager.listener;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.chancesd.pvpmanager.manager.PlayerHandler;
import me.chancesd.pvpmanager.setting.Messages;
import me.chancesd.pvpmanager.setting.Settings;
import me.chancesd.pvpmanager.utils.ScheduleUtils;

public class PlayerListener1_11 implements Listener {

	private final PlayerHandler playerHandler;

	public PlayerListener1_11(final PlayerHandler ph) {
		this.playerHandler = ph;
	}

	@EventHandler
	public void onProjectileLaunchEvent(final ProjectileLaunchEvent event) {
		final Projectile entity = event.getEntity();
		final ProjectileSource shooter = entity.getShooter();
		if (Settings.getEnderPearlCooldown() <= 0 || entity.getType() != EntityType.ENDER_PEARL || !(shooter instanceof Player))
			return;

		final Player player = (Player) shooter;
		final PvPlayer pvPlayer = playerHandler.get(player);

		if (pvPlayer.isInCombat()) {
			// TODO add feedback while on cooldown
			ScheduleUtils.runPlatformTask(() -> player.setCooldown(Material.ENDER_PEARL, Settings.getEnderPearlCooldown() * 20), player);
		}
	}

	@EventHandler
	public void onEntityRessurrect(final EntityResurrectEvent event) {
		if (!Settings.isBlockTotemUndying() || event.isCancelled() || event.getEntityType() != EntityType.PLAYER)
			return;

		final Player player = (Player) event.getEntity();
		final PvPlayer pvPlayer = playerHandler.get(player);
		if (pvPlayer.isInCombat()) {
			event.setCancelled(true);
			pvPlayer.message(Messages.getTotemBlockedInCombat());
		}

	}

}
