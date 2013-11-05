package me.NoChance.PvPManager.Managers;

import java.util.HashSet;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Utils;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatManager {

	private PvPManager plugin;
	private WorldTimerManager wtm;
	private HashSet<String> inCombat = new HashSet<String>();
	private HashSet<String> playersStatusOff = new HashSet<String>();
	private HashSet<String> newbies = new HashSet<String>();

	public CombatManager(PvPManager plugin) {
		this.plugin = plugin;
		this.wtm = plugin.getWtm();
	}

	public boolean isPvP(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			return true;
		} else if (event.getDamager() instanceof Projectile && event.getEntity() instanceof Player) {
			Projectile proj = (Projectile) event.getDamager();
			if (proj.getShooter() instanceof Player) {
				return true;
			}
		}
		return false;
	}

	public boolean isInCombat(Player p) {
		if (inCombat.contains(p.getName()))
			return true;

		return false;
	}

	public void tag(Player p) {
		if (p.hasPermission("pvpmanager.nocombat")) {
			return;
		}
		inCombat.add(p.getName());
		p.sendMessage(Messages.You_Are_InCombat);
		untagTimer(p);
		if (Variables.disableFly && p.isFlying() && p.getAllowFlight()) {
			disableFly(p);
		}
		if (Variables.disableGamemode) {
			if (!p.getGameMode().equals(GameMode.SURVIVAL)) {
				p.setGameMode(GameMode.SURVIVAL);
			}
		}
	}

	public void untag(Player p) {
		inCombat.remove(p.getName());
		if (Utils.isOnline(p))
			p.sendMessage(Messages.Out_Of_Combat);
	}

	public void disableFly(Player player) {
		player.setFlying(false);
		player.setAllowFlight(false);
	}

	public void inCombat(Player attacker, Player attacked) {
		if (Variables.onlyTagAttacker) {
			tag(attacker);
			return;
		} else {
			tag(attacker);
			tag(attacked);
		}
	}

	public boolean isNewbie(Player p) {
		if (newbies.contains(p.getName()))
			return true;
		else
			return false;
	}

	public boolean hasPvpEnabled(String name) {
		for (String n : playersStatusOff) {
			if (n.equalsIgnoreCase(name))
				return false;
		}
		return true;
	}

	public void untagTimer(final Player player) {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				untag(player);
			}
		}, Variables.timeInCombat * 20);
	}

	public void removeNewbieTimer(final String name) {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				removeNewbie(Utils.getPlayer(name));
			}
		}, Variables.newbieProtectionTime * 1200);

	}

	public void addNewbie(Player p) {
		newbies.add(p.getName());
		p.sendMessage(Messages.Newbie_Protection.replace("%", Integer.toString(Variables.newbieProtectionTime)));
	}

	public void removeNewbie(Player p) {
		newbies.remove(p.getName());
	}

	public void disablePvp(Player p) {
		playersStatusOff.add(p.getName());
	}

	public void enablePvp(Player p) {
		playersStatusOff.remove(p.getName());
	}

	public HashSet<String> getPlayersStatusOff() {
		return playersStatusOff;
	}

	public WorldTimerManager getWtm() {
		return wtm;
	}
}
