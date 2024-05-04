package me.chancesd.pvpmanager.player.nametag;

import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.utils.ChatUtils;

public abstract class NameTag {

	protected final CombatPlayer pvPlayer;
	protected final String combatPrefix;
	protected final String combatSuffix;
	protected final String pvpOnPrefix;
	protected final String pvpOffPrefix;

	protected NameTag(final CombatPlayer p) {
		this.pvPlayer = p;
		this.combatPrefix = ChatUtils.colorize(Conf.NAMETAG_PREFIX.asString());
		this.combatSuffix = ChatUtils.colorize(Conf.NAMETAG_SUFFIX.asString());
		this.pvpOnPrefix = ChatUtils.colorize(Conf.TOGGLE_PREFIX_ON.asString());
		this.pvpOffPrefix = ChatUtils.colorize(Conf.TOGGLE_PREFIX_OFF.asString());
	}

	public abstract void setInCombat();

	public abstract void restoreNametag();

	public abstract void setPvP(final boolean state);

	public abstract void cleanup();
}
