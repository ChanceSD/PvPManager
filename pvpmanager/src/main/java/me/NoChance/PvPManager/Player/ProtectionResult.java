package me.NoChance.PvPManager.Player;

public enum ProtectionResult {
	NEWBIE, PVPDISABLED, RESPAWN_PROTECTION, FAIL, FAIL_PLUGIN_HOOK, FAIL_OVERRIDE, WORLD_PROTECTION, AFK_PROTECTION, GLOBAL_PROTECTION;

	private boolean isAttacker;

	// If the attack was cancelled because of the attacker status or not TODO move somewhere, not thread safe
	public boolean attackerCaused() {
		return isAttacker;
	}

	// Must not be set async
	public ProtectionResult setAttackerCaused(final boolean attacker) {
		isAttacker = attacker;
		return this;
	}

	public boolean canAttack() {
		switch (this) {
		case FAIL_PLUGIN_HOOK:
		case FAIL_OVERRIDE:
		case FAIL:
			return true;
		default:
			return false;
		}
	}

	public boolean isProtected() {
		return !canAttack();
	}
}
