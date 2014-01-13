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
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatManager {

	private PvPManager plugin;
	private WorldTimerManager wtm;
	private HashMap<String, Integer> inCombat = new HashMap<String, Integer>();
	private HashSet<String> playersStatusOff = new HashSet<String>();
	private HashMap<String, Integer> newbieTimers = new HashMap<String, Integer>();
	private HashMap<String, Long> toggleTimers = new HashMap<String, Long>();
	private HashMap<String, String> killAbusers = new HashMap<String, String>();

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
				if (proj.getShooter() instanceof Player && !proj.getShooter().equals(event.getEntity())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isInCombat(Player p) {
		return getInCombat().containsKey(p.getName());
	}

	public void tag(Player p) {
		if (p.hasPermission("pvpmanager.nocombat")) {
			return;
		}
		getInCombat().put(p.getName(), untagTimer(p));
		p.sendMessage(Messages.You_Are_InCombat);
	}

	public void untag(Player p) {
		getInCombat().remove(p.getName());
		if (Utils.isOnline(p))
			p.sendMessage(Messages.Out_Of_Combat);
	}

	public void renewTag(Player p) {
		if (!isInCombat(p))
			return;
		plugin.getServer().getScheduler().cancelTask(getInCombat().get(p.getName()));
		getInCombat().put(p.getName(), untagTimer(p));
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
		return newbieTimers.containsKey(p.getName());
	}

	public boolean hasPvpEnabled(String name) {
		for (String n : playersStatusOff) {
			if (n.equalsIgnoreCase(name))
				return false;
		}
		return true;
	}

	public int untagTimer(final Player player) {
		return plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				untag(player);
			}
		}, Variables.timeInCombat * 20);
	}

	public void addNewbie(Player p) {
		p.sendMessage(Messages.Newbie_Protection.replace("%", Integer.toString(Variables.newbieProtectionTime)));
		final String name = p.getName();
		int i = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				removeNewbie(name);
			}
		}, Variables.newbieProtectionTime * 1200);
		newbieTimers.put(name, i);
	}

	private void removeNewbie(String name) {
		newbieTimers.remove(name);
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
		if (Utils.isOnline(name))
			Utils.getPlayer(name).sendMessage(Messages.PvP_Disabled);
	}

	public void enablePvp(Player p) {
		enablePvp(p.getName());
	}

	public void enablePvp(String name) {
		playersStatusOff.remove(name);
		if (Utils.isOnline(name))
			Utils.getPlayer(name).sendMessage(Messages.PvP_Enabled);
	}

	public void togglePvP(Player p) {
		String name = p.getName();
		if (checkToggleCooldown(p)) {
			if (hasPvpEnabled(name)) {
				disablePvp(name);
			} else {
				enablePvp(name);
			}
		}
	}

	public boolean checkToggleCooldown(Player p) {
		String name = p.getName();
		if (!toggleTimers.containsKey(name)) {
			toggleTimers.put(name, System.currentTimeMillis());
			return true;
		} else if (System.currentTimeMillis() - toggleTimers.get(name) < Variables.toggleCooldown * 1000) {
			p.sendMessage("§6[§8PvPManager§6] §cYou can't toggle it yet!");
			return false;
		} else {
			toggleTimers.put(name, System.currentTimeMillis());
			checkExpiredTimes();
			return true;
		}
	}

	public void checkExpiredTimes() {
		Iterator<Long> t = toggleTimers.values().iterator();
		while (t.hasNext()) {
			long s = t.next();
			if (System.currentTimeMillis() - s >= Variables.toggleCooldown * 1000)
				t.remove();
		}
	}

	public HashSet<String> getPlayersStatusOff() {
		return playersStatusOff;
	}

	public WorldTimerManager getWtm() {
		return wtm;
	}

	public HashMap<String, Integer> getInCombat() {
		return inCombat;
	}

	public HashMap<String, String> getKillAbusers() {
		return killAbusers;
	}
}
