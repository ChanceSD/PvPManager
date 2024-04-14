package me.NoChance.PvPManager.Player;

public enum ProtectionType {
	NEWBIE, PVPDISABLED, RESPAWN_PROTECTION, FAIL, FAIL_PLUGIN_HOOK, FAIL_OVERRIDE, WORLD_PROTECTION, AFK_PROTECTION, GLOBAL_PROTECTION;

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
