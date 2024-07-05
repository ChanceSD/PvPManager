package me.chancesd.pvpmanager.tasks;

import org.bukkit.ChatColor;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.sdutils.scheduler.ScheduleUtils;

public class PvPToggleFeeTask implements Runnable {

	private final PlayerManager ph;

	public PvPToggleFeeTask(final PlayerManager ph) {
		this.ph = ph;
	}

	@Override
	public final void run() {
		int i = 0;
		for (final CombatPlayer combatPlayer : ph.getPlayers().values()) {
			if (!combatPlayer.hasPvPEnabled() && !combatPlayer.applyPvPDisabledFee()) {
				ScheduleUtils.ensureMainThread(() -> combatPlayer.setPvP(true), combatPlayer.getPlayer());
				combatPlayer.message(Lang.PVP_FEE_NOT_ENOUGH);
				i++;
			}
		}
		Log.infoColor(ChatColor.GREEN + "Applied the PvP disabled fee of " + Settings.getPvPDisabledFee() + " to " + i + " players.");
	}

}
