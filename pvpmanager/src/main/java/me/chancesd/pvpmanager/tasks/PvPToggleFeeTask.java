package me.chancesd.pvpmanager.tasks;

import org.bukkit.ChatColor;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.chancesd.pvpmanager.utils.ScheduleUtils;
import me.chancesd.sdutils.utils.Log;

public class PvPToggleFeeTask implements Runnable {

	private final PlayerHandler ph;

	public PvPToggleFeeTask(final PlayerHandler ph) {
		this.ph = ph;
	}

	@Override
	public final void run() {
		int i = 0;
		for (final PvPlayer combatPlayer : ph.getPlayers().values()) {
			if (!combatPlayer.hasPvPEnabled() && !combatPlayer.applyPvPDisabledFee()) {
				ScheduleUtils.ensureMainThread(() -> combatPlayer.setPvP(true), combatPlayer.getPlayer());
				combatPlayer.message(Messages.getPvpFeeNotEnough());
				i++;
			}
		}
		Log.infoColor(ChatColor.GREEN + "Applied the PvP disabled fee of " + Settings.getPvPDisabledFee() + " to " + i + " players.");
	}

}
