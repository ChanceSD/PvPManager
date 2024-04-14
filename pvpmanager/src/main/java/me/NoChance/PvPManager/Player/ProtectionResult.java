package me.NoChance.PvPManager.Player;

public record ProtectionResult(ProtectionType type, boolean isAttacker) {

	public ProtectionResult(final ProtectionType type) {
		this(type, false);
	}

}
