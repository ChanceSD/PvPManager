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

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.ItemCooldown;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.sdutils.scheduler.ScheduleUtils;

public class PlayerListener1_11 implements Listener {

	private final PlayerManager playerHandler;

	public PlayerListener1_11(final PlayerManager ph) {
		this.playerHandler = ph;
	}

	@EventHandler
	public void onProjectileLaunchEvent(final ProjectileLaunchEvent event) {
		final Projectile entity = event.getEntity();
		final ProjectileSource shooter = entity.getShooter();
		final ItemCooldown itemCooldown = Conf.ITEM_COOLDOWNS.asMap().get(Material.ENDER_PEARL);
		if (itemCooldown == null || entity.getType() != EntityType.ENDER_PEARL || !(shooter instanceof Player))
			return;

		final Player player = (Player) shooter;
		final CombatPlayer pvPlayer = playerHandler.get(player);

		// Run in next tick otherwise doesn't work
		ScheduleUtils.runPlatformTask(() -> pvPlayer.setItemCooldown(Material.ENDER_PEARL, itemCooldown), player);
	}

	@EventHandler
	public void onEntityRessurrect(final EntityResurrectEvent event) {
		if (!Conf.BLOCK_TOTEM_UNDYING.asBool() || event.isCancelled() || event.getEntityType() != EntityType.PLAYER)
			return;

		final Player player = (Player) event.getEntity();
		final CombatPlayer pvPlayer = playerHandler.get(player);
		if (pvPlayer.isInCombat()) {
			event.setCancelled(true);
			pvPlayer.message(Lang.TOTEM_BLOCKED_IN_COMBAT);
		}

	}

}
