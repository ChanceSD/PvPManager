package me.chancesd.pvpmanager.integration.hook;

import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.DependencyException;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.utils.ScheduleUtils;
import me.chancesd.sdutils.utils.Utils;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.EventBus;
import me.neznamy.tab.api.event.player.PlayerLoadEvent;

public class TABHook extends BaseDependency {

	private final boolean showInPlayerlist;
	private final boolean showAboveHead;

	public TABHook(final Hook hook) {
		super(hook);
		if (!Utils.isVersionAtLeast(Utils.stripTags(hook.getVersion()), "4.0.0")) {
			throw new DependencyException("Can't hook into TAB, update TAB to 4.0.0 or above", hook);
		}
		this.showInPlayerlist = getConfigBoolean("Show In Tab", true);
		this.showAboveHead =getConfigBoolean("Show Above Player", true);

		registerLoadEvent();
	}

	private void registerLoadEvent() {
		final EventBus eventBus = TabAPI.getInstance().getEventBus();
		if (eventBus == null || !Conf.TOGGLE_NAMETAG_ENABLED.asBool())
			return;
		eventBus.register(PlayerLoadEvent.class, event -> {
			final TabPlayer tabPlayer = event.getPlayer();
			if (event.isJoin()) {
				final CombatPlayer pvPlayer = CombatPlayer.get((Player) tabPlayer.getPlayer());
				// wait 1 second because TAB was overwriting it
				ScheduleUtils.runAsyncLater(() -> pvPlayer.getNameTag().setPvP(pvPlayer.hasPvPEnabled()), 1, TimeUnit.SECONDS);
			}
		});
	}

	@Override
	public String onEnableMessage() {
		return getName() + " Found! Using TAB to change nametags instead of scoreboards";
	}

	public boolean showAboveHead() {
		return showAboveHead;
	}

	public boolean showInPlayerlist() {
		return showInPlayerlist;
	}

}
