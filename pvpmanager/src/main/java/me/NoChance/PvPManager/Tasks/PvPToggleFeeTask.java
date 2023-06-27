package me.NoChance.PvPManager.Tasks;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Managers.PlayerHandler;

public class PvPToggleFeeTask implements Runnable {

	private final PlayerHandler ph;

	public PvPToggleFeeTask(final PlayerHandler ph) {
		this.ph = ph;
	}

	@Override
	public final void run() {
		for (final PvPlayer p : ph.getPlayers().values()) {
			if (!p.hasPvPEnabled()) {
				p.applyPvPDisabledFee();
			}
		}
	}

}
