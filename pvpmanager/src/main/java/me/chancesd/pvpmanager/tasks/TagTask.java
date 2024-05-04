package me.chancesd.pvpmanager.tasks;

import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import me.chancesd.pvpmanager.manager.DisplayManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.UntagReason;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.sdutils.utils.Log;

public class TagTask extends TimerTask {

	private final Timer timer;
	private final Set<CombatPlayer> tagged = ConcurrentHashMap.newKeySet();
	private final DisplayManager display;

	public TagTask(final DisplayManager display) {
		this.display = display;
		this.timer = new Timer("PvPManager Display Thread");
		timer.scheduleAtFixedRate(this, 1000, 100);
	}

	@Override
	public final void run() {
		try {
			final Iterator<CombatPlayer> iterator = tagged.iterator();
			while (iterator.hasNext()) {
				final CombatPlayer combatPlayer = iterator.next();
				final long currentTime = System.currentTimeMillis();
				if (currentTime >= combatPlayer.getUntagTime()) {
					combatPlayer.untag(UntagReason.TIME_EXPIRED);
					iterator.remove();
					continue;
				}
				final double timePassed = (currentTime - combatPlayer.getTaggedTime()) / 1000D;
				final int totalTagTimeInSeconds = (int) (combatPlayer.getTotalTagTime() / 1000);
				if (Conf.ACTION_BAR_ENABLED.asBool()) {
					display.showProgress(combatPlayer, timePassed, totalTagTimeInSeconds);
				}
				if (Conf.BOSS_BAR_ENABLED.asBool()) {
					display.updateBossbar(combatPlayer, timePassed, totalTagTimeInSeconds);
				}
			}
		} catch (final Exception e) {
			Log.severe("Error in tag task", e);
			throw e;
		}
	}

	@Override
	public final boolean cancel() {
		for (final CombatPlayer combatPlayer : tagged) {
			if (combatPlayer.isInCombat()) {
				display.discardPlayer(combatPlayer);
				combatPlayer.untag(UntagReason.PLUGIN_DISABLE);
			}
		}
		tagged.clear();
		super.cancel();
		timer.cancel();
		return false;
	}

	public final void startTracking(final CombatPlayer combatPlayer) {
		tagged.add(combatPlayer);
	}

	public final void stopTracking(final CombatPlayer combatPlayer) {
		display.discardPlayer(combatPlayer);
		tagged.remove(combatPlayer);
	}

	public Set<CombatPlayer> getTaggedPlayers() {
		return tagged;
	}

}
