package me.NoChance.PvPManager.Player;

public enum CancelResult {
	NEWBIE, PVPDISABLED, RESPAWN_PROTECTION, FAIL, FAIL_OVERRIDE;

	private boolean isAttacker;

	// If the attack was cancelled because of the attacker status or not
	public boolean attackerCaused() {
		return isAttacker;
	}

	public CancelResult setAttackerCaused(final boolean attacker) {
		isAttacker = attacker;
		return this;
	}
}
