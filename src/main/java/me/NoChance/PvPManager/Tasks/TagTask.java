package me.NoChance.PvPManager.Tasks;

import me.NoChance.PvPManager.PvPlayer;

import org.bukkit.scheduler.BukkitRunnable;

public class TagTask extends BukkitRunnable{
	
	private PvPlayer player;
	
	public TagTask(PvPlayer player){
		this.player = player;
	}

	public void run() {
		player.setTagged(false);
	}

}
