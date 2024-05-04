package me.chancesd.pvpmanager.player.nametag;

import org.jetbrains.annotations.Nullable;

import me.chancesd.pvpmanager.integration.hook.TABHook;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Conf;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.api.tablist.TabListFormatManager;

public class TABNameTag extends NameTag {

	private boolean needsRestore;
	private final TABHook tab;

	public TABNameTag(final TABHook hook, final CombatPlayer p) {
		super(p);
		this.tab = hook;
	}

	private boolean changeTABPrefix(final String tabPrefix, final String tabSuffix) {
		final TabPlayer tabPlayer = getTABPlayer();
		if (tabPlayer == null)
			return false;
		final NameTagManager teamManager = TabAPI.getInstance().getNameTagManager();
		if (teamManager != null && tab.showAboveHead()) {
			if (!tabPrefix.isEmpty()) {
				teamManager.setPrefix(tabPlayer, tabPrefix);
			}
			if (!tabSuffix.isEmpty()) {
				teamManager.setSuffix(tabPlayer, tabSuffix);
			}
		}
		final TabListFormatManager tabManager = TabAPI.getInstance().getTabListFormatManager();
		if (tabManager != null && tab.showInPlayerlist()) {
			if (!tabPrefix.isEmpty()) {
				tabManager.setPrefix(tabPlayer, tabPrefix);
			}
			if (!tabSuffix.isEmpty()) {
				tabManager.setSuffix(tabPlayer, tabSuffix);
			}
		}
		this.needsRestore = true;
		return true;
	}

	private boolean restoreTABPrefix() {
		final TabPlayer tabPlayer = getTABPlayer();
		if (tabPlayer == null)
			return false;

		final NameTagManager teamManager = TabAPI.getInstance().getNameTagManager();
		if (teamManager != null && tab.showAboveHead()) {
			teamManager.setPrefix(tabPlayer, null);
			teamManager.setSuffix(tabPlayer, null);
		}
		final TabListFormatManager tabManager = TabAPI.getInstance().getTabListFormatManager();
		if (tabManager != null && tab.showInPlayerlist()) {
			tabManager.setPrefix(tabPlayer, null);
			tabManager.setSuffix(tabPlayer, null);
		}

		if (Conf.TOGGLE_NAMETAG_ENABLED.asBool()) {
			setPvP(pvPlayer.hasPvPEnabled());
		}

		this.needsRestore = false;
		return true;
	}

	@Nullable
	private TabPlayer getTABPlayer() {
		return TabAPI.getInstance().getPlayer(pvPlayer.getUUID());
	}

	@Override
	public final void setInCombat() {
		changeTABPrefix(combatPrefix, combatSuffix);
	}

	@Override
	public final void restoreNametag() {
		if (needsRestore)
			restoreTABPrefix();
	}

	@Override
	public final void setPvP(final boolean state) {
		if (getTABPlayer() == null)
			return;

		if (state) {
			if (pvpOnPrefix.isEmpty()) {
				restoreNametag();
			} else {
				changeTABPrefix(pvpOnPrefix, "");
			}
		} else if (pvpOffPrefix.isEmpty()) {
			restoreNametag();
		} else {
			changeTABPrefix(pvpOffPrefix, "");
		}
	}

	@Override
	public void cleanup() {
		// no cleanup needed for TAB
	}

}
