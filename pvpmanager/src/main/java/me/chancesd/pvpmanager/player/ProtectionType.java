package me.chancesd.pvpmanager.player;

public enum ProtectionType {
	NEWBIE, PVPDISABLED, RESPAWN_PROTECTION, FAIL, FAIL_PLUGIN_HOOK, FAIL_OVERRIDE, WORLD_PROTECTION, AFK_PROTECTION, GLOBAL_PROTECTION;

	public boolean canAttack() {
		switch (this) {
		case FAIL_PLUGIN_HOOK, FAIL_OVERRIDE, FAIL:
			return true;
		default:
			return false;
		}
	}

}
