package me.NoChance.PvPManager.Tasks;

import org.bukkit.scheduler.BukkitRunnable;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Managers.PlayerHandler;

public class PvPToggleFeeTask extends BukkitRunnable {

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
