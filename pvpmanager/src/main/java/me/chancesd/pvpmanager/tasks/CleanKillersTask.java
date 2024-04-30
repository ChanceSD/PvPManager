package me.chancesd.pvpmanager.tasks;

import me.NoChance.PvPManager.PvPlayer;
import me.chancesd.pvpmanager.manager.PlayerHandler;

public class CleanKillersTask implements Runnable {

	private final PlayerHandler ph;

	public CleanKillersTask(final PlayerHandler ph) {
		this.ph = ph;
	}

	@Override
	public final void run() {
		for (final PvPlayer p : ph.getPlayers().values()) {
			p.clearVictims();
		}
	}

}
