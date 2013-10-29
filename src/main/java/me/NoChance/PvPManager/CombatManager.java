package me.NoChance.PvPManager;

import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatManager {

	private HashSet<String> inCombat = new HashSet<String>();

	public CombatManager(PvPManager plugin) {

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

	public void untag(Player p){
		inCombat.remove(p.getName());
	}
	
}
