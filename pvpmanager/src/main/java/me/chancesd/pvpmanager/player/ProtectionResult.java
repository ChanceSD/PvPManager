package me.chancesd.pvpmanager.player;

public record ProtectionResult(ProtectionType type, boolean isAttacker) {

	public ProtectionResult(final ProtectionType type) {
		this(type, false);
	}

	public boolean isProtected() {
		return !type.canAttack();
	}

	public boolean isVulnerable() {
		return type.canAttack();
	}

}
