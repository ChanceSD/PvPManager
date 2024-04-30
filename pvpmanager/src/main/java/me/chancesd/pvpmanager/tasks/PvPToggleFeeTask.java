package me.chancesd.pvpmanager.tasks;

import me.NoChance.PvPManager.PvPlayer;
import me.chancesd.pvpmanager.manager.PlayerHandler;
import me.chancesd.pvpmanager.setting.Messages;

public class PvPToggleFeeTask implements Runnable {

	private final PlayerHandler ph;

	public PvPToggleFeeTask(final PlayerHandler ph) {
		this.ph = ph;
	}

	@Override
	public final void run() {
		for (final PvPlayer combatPlayer : ph.getPlayers().values()) {
			if (!combatPlayer.hasPvPEnabled() && !combatPlayer.applyPvPDisabledFee()) {
				combatPlayer.setPvP(true);
				combatPlayer.message(Messages.getPvpFeeNotEnough());
			}
		}
	}

}
