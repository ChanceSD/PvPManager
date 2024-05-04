package me.chancesd.pvpmanager.setting;

public class ItemCooldown {

	private int combatCooldown;
	private int globalCooldown;

	public ItemCooldown(final int combatCooldown, final int globalCooldown) {
		this.combatCooldown = combatCooldown;
		this.globalCooldown = globalCooldown;
	}

	public int getCombatCooldown() {
		return combatCooldown;
	}

	public int getGlobalCooldown() {
		return globalCooldown;
	}

	public void setCombatCooldown(final int combatCooldown) {
		this.combatCooldown = combatCooldown;
	}

	public void setGlobalCooldown(final int globalCooldown) {
		this.globalCooldown = globalCooldown;
	}

}
