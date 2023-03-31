package me.NoChance.PvPManager.Tasks;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Managers.DisplayManager;
import me.NoChance.PvPManager.Settings.Settings;

public class TagTask extends TimerTask {

	private final Timer timer;
	private final long time = Settings.getTimeInCombat() * 1000L;
	private final Set<PvPlayer> tagged = Collections.synchronizedSet(new HashSet<>());
	private final DisplayManager display;

	public TagTask(final DisplayManager display) {
		this.display = display;
		this.timer = new Timer();
		timer.scheduleAtFixedRate(this, 1000, 100);
	}

	@Override
	public final void run() {
		synchronized (tagged) {
			final Iterator<PvPlayer> iterator = tagged.iterator();
			while (iterator.hasNext()) {
				final PvPlayer p = iterator.next();
				final long timePassed = System.currentTimeMillis() - p.getTaggedTime();
				if (timePassed >= time) {
					Bukkit.getScheduler().runTask(display.getPlugin(), p::unTag);
					display.discardBossbar(p);
					iterator.remove();
					return;
				}
				if (!Settings.getActionBarMessage().isEmpty()) {
					display.showProgress(p, timePassed / 1000D);
				}
				display.updateBossbar(p, timePassed / 1000D);
			}
		}
	}

	@Override
	public final boolean cancel() {
		synchronized (tagged) {
			for (final PvPlayer pvPlayer : tagged)
				if (pvPlayer.isInCombat()) {
					display.discardBossbar(pvPlayer);
					pvPlayer.unTag();
				}
		}
		tagged.clear();
		super.cancel();
		timer.cancel();
		return false;
	}

	public final void addTagged(final PvPlayer p) {
		tagged.add(p);
	}

	public final void untag(final PvPlayer p) {
		display.discardBossbar(p);
		tagged.remove(p);
		if (p.isInCombat()) {
			p.unTag();
		}
	}

	public Set<PvPlayer> getTaggedPlayers() {
		return new HashSet<>(tagged);
	}

}
