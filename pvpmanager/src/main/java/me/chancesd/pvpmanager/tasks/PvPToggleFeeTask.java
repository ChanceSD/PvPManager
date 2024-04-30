package me.chancesd.pvpmanager.tasks;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Messages;
import me.chancesd.pvpmanager.utils.ScheduleUtils;

public class PvPToggleFeeTask implements Runnable {

	private final PlayerHandler ph;

	public PvPToggleFeeTask(final PlayerHandler ph) {
		this.ph = ph;
	}

	@Override
	public final void run() {
		for (final PvPlayer combatPlayer : ph.getPlayers().values()) {
			if (!combatPlayer.hasPvPEnabled() && !combatPlayer.applyPvPDisabledFee()) {
				ScheduleUtils.ensureMainThread(() -> combatPlayer.setPvP(true), combatPlayer.getPlayer());
				combatPlayer.message(Messages.getPvpFeeNotEnough());
			}
		}
	}

}
