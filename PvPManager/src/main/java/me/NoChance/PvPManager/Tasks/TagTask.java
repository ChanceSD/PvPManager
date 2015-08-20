package me.NoChance.PvPManager.Tasks;

import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Utils.CombatUtils;

import java.util.*;

public class TagTask extends TimerTask {

	private final int time = Variables.getTimeInCombat();
	private final Set<PvPlayer> tagged = Collections.synchronizedSet(new HashSet<PvPlayer>());

	@Override
	public final void run() {
		synchronized (tagged) {
			final Iterator<PvPlayer> iterator = tagged.iterator();
			while (iterator.hasNext()) {
				final PvPlayer p = iterator.next();
				if (CombatUtils.hasTimePassed(p.getTaggedTime(), time)) {
					p.unTag();
					iterator.remove();
				}
			}
		}
	}

	@Override
	public synchronized boolean cancel() throws IllegalStateException {
		tagged.clear();
		return super.cancel();
	}

	public void addTagged(final PvPlayer p) {
		tagged.add(p);
	}

	public void untag(final PvPlayer p) {
		tagged.remove(p);
		if (p.isInCombat())
			p.unTag();
	}

}
