package me.NoChance.PvPManager.Tasks;

import java.util.HashSet;
import java.util.Iterator;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Utils.CombatUtils;

import org.bukkit.scheduler.BukkitRunnable;

public class TagTask extends BukkitRunnable {

	private int time = Variables.timeInCombat;
	private final HashSet<PvPlayer> tagged = new HashSet<PvPlayer>();

	public void run() {
		Iterator<PvPlayer> iterator = getTagged().iterator();
		while (iterator.hasNext()) {
			PvPlayer p = iterator.next();
			if (CombatUtils.hasTimePassed(p.getTaggedTime(), time)) {
				p.unTag();
				iterator.remove();
			}
		}
	}

	public synchronized HashSet<PvPlayer> getTagged() {
		return tagged;
	}
	
}
