package me.NoChance.PvPManager.Tasks;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.scheduler.BukkitRunnable;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Utils.CombatUtils;

public class TagTask extends BukkitRunnable {

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
	public synchronized void cancel() throws IllegalStateException {
		super.cancel();
		tagged.clear();
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
