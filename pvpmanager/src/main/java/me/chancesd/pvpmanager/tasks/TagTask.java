package me.chancesd.pvpmanager.tasks;

import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import me.chancesd.pvpmanager.manager.DisplayManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Settings;
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
				final CombatPlayer p = iterator.next();
				final long currentTime = System.currentTimeMillis();
				if (currentTime >= p.getUntagTime()) {
					p.unTag();
					display.discardPlayer(p);
					iterator.remove();
					continue;
				}
				final double timePassed = (currentTime - p.getTaggedTime()) / 1000D;
				final int totalTagTimeInSeconds = (int) (p.getTotalTagTime() / 1000);
				if (Settings.isActionBarEnabled()) {
					display.showProgress(p, timePassed, totalTagTimeInSeconds);
				}
				if (Settings.isBossBarEnabled()) {
					display.updateBossbar(p, timePassed, totalTagTimeInSeconds);
				}
			}
		} catch (final Exception e) {
			Log.severe("Error in tag task", e);
			throw e;
		}
	}

	@Override
	public final boolean cancel() {
		for (final CombatPlayer pvPlayer : tagged)
			if (pvPlayer.isInCombat()) {
				display.discardPlayer(pvPlayer);
				pvPlayer.unTag();
			}
		tagged.clear();
		super.cancel();
		timer.cancel();
		return false;
	}

	public final void addTagged(final CombatPlayer p) {
		tagged.add(p);
	}

	public final void untag(final CombatPlayer p) {
		display.discardPlayer(p);
		tagged.remove(p);
	}

	public Set<CombatPlayer> getTaggedPlayers() {
		return tagged;
	}

}
