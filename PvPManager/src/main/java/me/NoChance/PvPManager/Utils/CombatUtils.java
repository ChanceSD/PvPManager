package me.NoChance.PvPManager.Utils;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import me.NoChance.PvPManager.Settings.Settings;

public final class CombatUtils {

	private CombatUtils() {
	}

	public static boolean hasTimePassed(final long toggleTime, final int cooldown) {
		return hasTimePassedMs(toggleTime, cooldown * 1000);
	}

	public static boolean hasTimePassedMs(final long toggleTime, final long cooldown) {
		return System.currentTimeMillis() - toggleTime >= cooldown;
	}

	public static int getTimeLeft(final long startTime, final int time) {
		return (int) (getTimeLeftMs(startTime, time * 1000) / 1000);
	}

	public static long getTimeLeftMs(final long startTime, final long time) {
		return startTime + time - System.currentTimeMillis();
	}

	public static boolean isPvP(final EntityDamageByEntityEvent event) {
		final Entity attacker = event.getDamager();
		final Entity defender = event.getEntity();

		if (defender instanceof Player && !defender.hasMetadata("NPC")) {
			if (attacker instanceof Player && !attacker.hasMetadata("NPC"))
				return true;
			if (attacker instanceof Projectile) {
				final ProjectileSource projSource = ((Projectile) attacker).getShooter();
				if (projSource instanceof Player) {
					final Entity shooter = (Entity) projSource;
					if (!shooter.equals(defender) && !shooter.hasMetadata("NPC"))
						return !(Settings.isIgnoreNoDamageHits() && event.getDamage() == 0);
				}
			}
		}

		return false;
	}

	public static boolean isPvP(final EntityCombustByEntityEvent event) {
		final Entity attacker = event.getCombuster();
		final Entity defender = event.getEntity();

		if (defender instanceof Player && !defender.hasMetadata("NPC")) {
			if (attacker instanceof Player && !attacker.hasMetadata("NPC"))
				return true;
			if (attacker instanceof Projectile) {
				final ProjectileSource projSource = ((Projectile) attacker).getShooter();
				if (projSource instanceof Player) {
					final Entity shooter = (Entity) projSource;
					return !shooter.equals(defender) && !shooter.hasMetadata("NPC");
				}
			}
		}

		return false;
	}

	public static boolean canFly(final Player p) {
		return p.isFlying() || p.getAllowFlight();
	}

	public static void fakeItemStackDrop(final Player player, final ItemStack[] inventory) {
		final Location playerLocation = player.getLocation();
		final World playerWorld = player.getWorld();
		for (final ItemStack itemstack : inventory) {
			if (itemstack != null && !itemstack.getType().equals(Material.AIR)) {
				playerWorld.dropItemNaturally(playerLocation, itemstack);
			}
		}
	}

	public static boolean isOnline(final String name) {
		return Bukkit.getPlayer(name) != null;
	}

	public static boolean isOnline(final UUID id) {
		return Bukkit.getPlayer(id) != null;
	}

	public static boolean isReal(final UUID id) {
		return Bukkit.getPlayer(id) != null;
	}

	public static boolean isWorldExcluded(final String worldName) {
		return Settings.getWorldsExcluded().contains(worldName);
	}

	public static boolean recursiveContainsCommand(final String[] givenCommand, final List<String> list) {
		boolean contains = false;
		for (int i = 0; i < givenCommand.length; i++) {
			String args = givenCommand[0];
			for (int j = 1; j <= i; j++) {
				args += " " + givenCommand[j];
			}
			if (list.contains(args.toLowerCase())) {
				contains = true;
				break;
			}
		}
		return contains;
	}

	public static final boolean isVersionAtLeast(final String v1, final String v2) {
		if (v1.equals(v2))
			return true;

		final String[] v1Array = v1.split("\\.");
		final String[] v2Array = v2.split("\\.");
		final int length = Math.max(v2Array.length, v1Array.length);
		try {
			for (int i = 0; i < length; i++) {
				final int x = i < v2Array.length ? Integer.parseInt(v2Array[i]) : 0;
				final int y = i < v1Array.length ? Integer.parseInt(v1Array[i]) : 0;
				if (y > x)
					return true;
				if (y < x)
					return false;
			}
		} catch (final NumberFormatException ex) {
			Log.severe("Error reading version number! Comparing " + v1 + " to " + v2);
		}
		return true;
	}

	public static String stripTags(final String version) {
		return version.replaceAll("(-|;).+", "");
	}

}
