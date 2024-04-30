package me.chancesd.pvpmanager.tasks;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Messages;

public class PvPToggleFeeTask implements Runnable {

	private final PlayerManager ph;

	public PvPToggleFeeTask(final PlayerManager ph) {
		this.ph = ph;
	}

	@Override
	public final void run() {
		for (final CombatPlayer combatPlayer : ph.getPlayers().values()) {
			if (!combatPlayer.hasPvPEnabled() && !combatPlayer.applyPvPDisabledFee()) {
				combatPlayer.setPvP(true);
				combatPlayer.message(Messages.getPvpFeeNotEnough());
			}
		}
	}

}
