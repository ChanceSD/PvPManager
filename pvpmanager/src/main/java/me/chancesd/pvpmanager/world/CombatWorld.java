package me.chancesd.pvpmanager.world;

public class CombatWorld {

	private final boolean pvpState;
	private final boolean forcePVP;

	public CombatWorld(final boolean pvpState, final boolean forcePVP) {
		this.pvpState = pvpState;
		this.forcePVP = forcePVP;
	}

	public boolean isPvPEnabled() {
		return pvpState;
	}

	public boolean isPvPForced() {
		return forcePVP;
	}

}
