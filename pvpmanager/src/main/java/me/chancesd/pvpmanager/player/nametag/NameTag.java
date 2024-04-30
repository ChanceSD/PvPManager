package me.chancesd.pvpmanager.player.nametag;

import me.NoChance.PvPManager.PvPlayer;
import me.chancesd.pvpmanager.setting.Settings;
import me.chancesd.pvpmanager.utils.ChatUtils;

public abstract class NameTag {

	protected final PvPlayer pvPlayer;
	protected final String combatPrefix;
	protected final String combatSuffix;
	protected final String pvpOnPrefix;
	protected final String pvpOffPrefix;

	protected NameTag(final PvPlayer p) {
		this.pvPlayer = p;
		this.combatPrefix = ChatUtils.colorize(Settings.getNameTagPrefix());
		this.combatSuffix = ChatUtils.colorize(Settings.getNameTagSuffix());
		this.pvpOnPrefix = Settings.getToggleColorOn().equalsIgnoreCase("none") ? "" : ChatUtils.colorize(Settings.getToggleColorOn());
		this.pvpOffPrefix = Settings.getToggleColorOff().equalsIgnoreCase("none") ? "" : ChatUtils.colorize(Settings.getToggleColorOff());
	}

	public abstract void setInCombat();

	public abstract void restoreNametag();

	public abstract void setPvP(final boolean state);

	public abstract void cleanup();
}
