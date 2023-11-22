package me.NoChance.PvPManager.Listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.Hooks.CooldownsXHook;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Settings;
import me.chancesd.pvpmanager.utils.ScheduleUtils;

public class PlayerListener1_11 implements Listener {

	private final PlayerHandler playerHandler;
	private final CooldownsXHook cooldownsxHook;

	public PlayerListener1_11(final PlayerHandler ph) {
		this.playerHandler = ph;
		this.cooldownsxHook = (CooldownsXHook) ph.getPlugin().getDependencyManager().getDependency(Hook.COOLDOWNSX);
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
			if (cooldownsxHook != null) {
				cooldownsxHook.setEnderpearlCooldown(player, Settings.getEnderPearlCooldown());
			}
			ScheduleUtils.runPlatformTask(() -> player.setCooldown(Material.ENDER_PEARL, Settings.getEnderPearlCooldown() * 20), player);
		}
	}

}
