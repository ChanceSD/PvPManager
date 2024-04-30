package me.chancesd.pvpmanager.tasks;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Messages;
import me.chancesd.pvpmanager.utils.ScheduleUtils;

public class PvPToggleFeeTask implements Runnable {

	private final PlayerManager ph;

	public PvPToggleFeeTask(final PlayerManager ph) {
		this.ph = ph;
	}

	@Override
	public final void run() {
		for (final CombatPlayer combatPlayer : ph.getPlayers().values()) {
			if (!combatPlayer.hasPvPEnabled() && !combatPlayer.applyPvPDisabledFee()) {
				ScheduleUtils.ensureMainThread(() -> combatPlayer.setPvP(true), combatPlayer.getPlayer());
				combatPlayer.message(Messages.getPvpFeeNotEnough());
			}
		}
	}

}
