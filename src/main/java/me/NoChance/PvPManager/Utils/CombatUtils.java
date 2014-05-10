package me.NoChance.PvPManager.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Listeners.WGDependency;
import me.NoChance.PvPManager.Managers.PlayerHandler;

public class CombatUtils {

	public enum CancelResult {
		NEWBIE, NEWBIE_OTHER, PVPDISABLED, PVPDISABLED_OTHER, FAIL, FAIL_OVERRIDE
	}
	public static boolean useWG;
	public static boolean useTowny;
	private static WGDependency wg;
	private static Towny towny;
	private static PlayerHandler ph;

	public CombatUtils(PvPManager pvpManager) {
		ph = pvpManager.getPlayerHandler();
		if (Utils.isWGEnabled()) {
			Utils.register(wg = new WGDependency(), pvpManager);
			pvpManager.getLogger().info("WorldGuard Found! Enabling WorldGuard Support");
			useWG = true;
		}
		if (Utils.isTownyEnabled()) {
			towny = (Towny) Bukkit.getPluginManager().getPlugin("Towny");
			pvpManager.getLogger().info("Towny Found! Enabling Towny Support");
			useTowny = true;
		}
	}

	public static boolean checkToggleCooldown(long toggleTime) {
		if (toggleTime == 0)
			return true;
		else if (System.currentTimeMillis() - toggleTime < Variables.toggleCooldown * 1000)
			return false;
		else
			return true;
	}

	public static boolean isPvP(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			if (event.getDamager().hasMetadata("NPC") || event.getEntity().hasMetadata("NPC"))
				return false;
			return true;
		}
		if (event.getEntity() instanceof Player) {
			if (event.getDamager() instanceof Projectile) {
				Projectile proj = (Projectile) event.getDamager();
				if (proj.getShooter() instanceof Player && !proj.getShooter().equals(event.getEntity())) {
					if (event.getEntity().hasMetadata("NPC"))
						return false;
					return true;
				}
			}
		}
		return false;
	}

	public static CancelResult tryCancel(PvPlayer attacker, PvPlayer attacked) {
		if (attacker.hasOverride() || Variables.stopBorderHopping && attacker.isInCombat() && attacked.isInCombat())
			return CancelResult.FAIL_OVERRIDE;

		if (useWG)
			if (wg.hasWGPvPFlag(attacked.getPlayer().getWorld(), attacked.getPlayer().getLocation()))
				return CancelResult.FAIL;

		if (useTowny && Variables.townySupport)
			if (!CombatUtil.preventDamageCall(towny, attacker.getPlayer(), attacked.getPlayer()))
				return CancelResult.FAIL;

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

	public static CancelResult tryCancel(Player attacker, Player attacked) {
		return tryCancel(ph.get(attacker), ph.get(attacked));
	}
}
