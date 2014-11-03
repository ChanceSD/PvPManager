package me.NoChance.PvPManager.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;

import com.massivecraft.factions.entity.UPlayer;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Listeners.WGListener;
import me.NoChance.PvPManager.Managers.PlayerHandler;

public class CombatUtils {

	public enum CancelResult {
		NEWBIE, NEWBIE_OTHER, PVPDISABLED, PVPDISABLED_OTHER, RESPAWN_PROTECTION, FAIL, FAIL_OVERRIDE
	}

	private static PlayerHandler ph;
	private static boolean useFactions;

	public CombatUtils(PvPManager plugin) {
		ph = plugin.getPlayerHandler();
		if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
			plugin.registerListener(new WGListener());
			plugin.getLogger().info("WorldGuard Found! Enabling WorldGuard Support");
		}
		Plugin factions = Bukkit.getPluginManager().getPlugin("Factions");

		try {
			useFactions = factions != null && Integer.valueOf(factions.getDescription().getVersion().replace(".", "")) > 182;
		} catch (NumberFormatException e) {
			plugin.getLogger().warning("Couldn't read Factions version, maybe it's a fork?");
			useFactions = false;
		}
	}

	public static boolean hasTimePassed(long toggleTime, int cooldown) {
		return System.currentTimeMillis() - toggleTime < cooldown * 1000 ? false : true;
	}

	public static boolean isPvP(EntityDamageByEntityEvent event) {
		Entity attacker = event.getDamager();
		Entity defender = event.getEntity();

		if (defender instanceof Player && !defender.hasMetadata("NPC")) {
			if (attacker instanceof Player && !attacker.hasMetadata("NPC"))
				return true;
			if (attacker instanceof Projectile) {
				Entity shooter = (Entity) ((Projectile) attacker).getShooter();
				if (shooter instanceof Player && !shooter.equals(defender) && !shooter.hasMetadata("NPC")) {
					if (Variables.ignoreNoDamageHits && event.getDamage() == 0)
						return false;
					return true;
				}
			}
		}

		return false;
	}

	public static CancelResult tryCancel(PvPlayer attacker, PvPlayer attacked) {
		if (attacker.hasOverride() || Variables.stopBorderHopping && canAttack(attacker, attacked))
			return CancelResult.FAIL_OVERRIDE;
		if (attacked.hasRespawnProtection() || attacker.hasRespawnProtection())
			return CancelResult.RESPAWN_PROTECTION;
		if (attacked.isNewbie())
			return CancelResult.NEWBIE_OTHER;
		if (attacker.isNewbie())
			return CancelResult.NEWBIE;
		if (!attacked.hasPvPEnabled())
			return CancelResult.PVPDISABLED_OTHER;
		if (!attacker.hasPvPEnabled())
			return CancelResult.PVPDISABLED;

		return CancelResult.FAIL;
	}

	// When stopping border hopping let's check if players are from different
	// Factions
	private static boolean canAttack(PvPlayer attacker, PvPlayer attacked) {
		if (!(attacker.isInCombat() && attacked.isInCombat()))
			return false;
		else if (useFactions) {
			UPlayer fAttacker = UPlayer.get(attacker.getPlayer());
			UPlayer fAttacked = UPlayer.get(attacked.getPlayer());
			return !fAttacker.getFactionId().equalsIgnoreCase(fAttacked.getFactionId());
		} else
			return true;
	}

	public static CancelResult tryCancel(Player attacker, Player attacked) {
		return tryCancel(ph.get(attacker), ph.get(attacked));
	}

	public static boolean PMAllowed(String worldName) {
		return !Variables.worldsExcluded.contains(worldName);
	}
}
