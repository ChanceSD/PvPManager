package me.NoChance.PvPManager.Managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Others.Utils;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatManager {

	private PvPManager plugin;
	private WorldTimerManager wtm;
	private HashSet<String> inCombat = new HashSet<String>();
	private HashSet<String> playersStatusOff = new HashSet<String>();
	private HashSet<String> newbies = new HashSet<String>();
	private HashMap<String, Integer> newbieTimers = new HashMap<String, Integer>();
	private HashMap<String, Long> toggleTimers = new HashMap<String, Long>();

	public CombatManager(PvPManager plugin) {
		this.plugin = plugin;
		this.wtm = plugin.getWtm();
	}

	public boolean isPvP(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			return true;
		}
		if (event.getEntity() instanceof Player) {
			if (event.getDamager() instanceof Projectile) {
				Projectile proj = (Projectile) event.getDamager();
				if (proj.getShooter() instanceof Player) {
					return true;
				}
			} else if (event.getDamager() instanceof ThrownPotion) {
				ThrownPotion potion = (ThrownPotion) event.getDamager();
				if (potion.getShooter() instanceof Player) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isInCombat(Player p) {
		return inCombat.contains(p.getName());
	}

	public void tag(Player p) {
		if (p.hasPermission("pvpmanager.nocombat")) {
			return;
		}
		inCombat.add(p.getName());
		p.sendMessage(Messages.You_Are_InCombat);
		untagTimer(p);
	}

	public void untag(Player p) {
		if (isInCombat(p)) {
			inCombat.remove(p.getName());
			if (Utils.isOnline(p))
				p.sendMessage(Messages.Out_Of_Combat);
		}
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
		return newbies.contains(p.getName());
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
			@Override
			public void run() {
				untag(player);
			}
		}, Variables.timeInCombat * 20);
	}

	public void removeNewbieTimer(final String name) {
		int i = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				removeNewbie(name);
			}
		}, Variables.newbieProtectionTime * 1200);
		newbieTimers.put(name, i);
	}

	public void addNewbie(Player p) {
		newbies.add(p.getName());
		p.sendMessage(Messages.Newbie_Protection.replace("%", Integer.toString(Variables.newbieProtectionTime)));
	}

	private void removeNewbie(String name) {
		newbies.remove(name);
	}

	public void forceNewbieRemoval(Player p) {
		String name = p.getName();
		plugin.getServer().getScheduler().cancelTask(newbieTimers.get(name));
		removeNewbie(name);
	}

	public void disablePvp(Player p) {
		disablePvp(p.getName());
	}

	public void disablePvp(String name) {
		playersStatusOff.add(name);
	}

	public void enablePvp(Player p) {
		enablePvp(p.getName());
	}

	public void enablePvp(String name) {
		playersStatusOff.remove(name);
	}

	public boolean checkToggleCooldown(Player p) {
		String name = p.getName();
		if(!toggleTimers.containsKey(name)){
			toggleTimers.put(name, System.currentTimeMillis());
			return true;
		}
		else if (System.currentTimeMillis() - toggleTimers.get(name) < Variables.toggleCooldown * 1000){
			p.sendMessage("§6[§8PvPManager§6] §cYou can't toggle it yet!");
			return false;
		}
		else {
			toggleTimers.put(name, System.currentTimeMillis());
			checkExpiredTimes();
			return true;
		}
	}
	
	public void checkExpiredTimes(){
		Iterator<Long> t = toggleTimers.values().iterator();
		while(t.hasNext()){
			long s = t.next();
			if(System.currentTimeMillis() - s >= Variables.toggleCooldown * 1000)
				t.remove();
		}
	}

	public HashSet<String> getPlayersStatusOff() {
		return playersStatusOff;
	}

	public WorldTimerManager getWtm() {
		return wtm;
	}

	public HashSet<String> getInCombat() {
		return inCombat;
	}
}
