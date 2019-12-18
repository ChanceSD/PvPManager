package me.NoChance.PvPManager.Tasks;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TimerTask;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;

public class TagTask extends TimerTask {

	private final long time = Settings.getTimeInCombat() * 1000;
	private final Set<PvPlayer> tagged = Collections.synchronizedSet(new HashSet<>());

	@Override
	public final void run() {
		synchronized (tagged) {
			final Iterator<PvPlayer> iterator = tagged.iterator();
			while (iterator.hasNext()) {
				final PvPlayer p = iterator.next();
				if (CombatUtils.hasTimePassedMs(p.getTaggedTime(), time)) {
					p.unTag();
					iterator.remove();
				}
			}
		}
	}

	@Override
	public final synchronized boolean cancel() throws IllegalStateException {
		synchronized (tagged) {
			for (final PvPlayer pvPlayer : tagged)
				if (pvPlayer.isInCombat()) {
					pvPlayer.unTag();
				}
		}
		tagged.clear();
		return super.cancel();
	}

	public final void addTagged(final PvPlayer p) {
		tagged.add(p);
	}

	public final void untag(final PvPlayer p) {
		tagged.remove(p);
		if (p.isInCombat()) {
			p.unTag();
		}
	}

}
