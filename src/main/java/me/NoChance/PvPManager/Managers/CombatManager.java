package me.NoChance.PvPManager.Managers;

import java.util.HashSet;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Utils;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;

import org.bukkit.World;
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

	public boolean isPvpTimerWorld(World w) {
		return wtm.contains(w);
	}

	public boolean isTimeForPvp(World w) {
		if (wtm.getPvpTimer(w).isPvpTime())
			return true;
		else
			return false;
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
		inCombat.add(p.getName());
	}

	public void untag(Player p) {
		inCombat.remove(p.getName());
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

	public void Timer(final Player player) {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (Utils.isOnline(player))
					player.sendMessage(Messages.Out_Of_Combat);

				untag(player);
			}
		}, Variables.timeInCombat * 20);
	}

	public void addNewbie(Player p) {
		newbies.add(p.getName());
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
}
