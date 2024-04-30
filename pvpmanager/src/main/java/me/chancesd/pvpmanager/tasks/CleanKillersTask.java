package me.chancesd.pvpmanager.tasks;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;

public class CleanKillersTask implements Runnable {

	private final PlayerManager ph;

	public CleanKillersTask(final PlayerManager ph) {
		this.ph = ph;
	}

	@Override
	public final void run() {
		for (final CombatPlayer p : ph.getPlayers().values()) {
			p.clearVictims();
		}
	}

}
