package me.chancesd.pvpmanager.player;

public class PlayerExemptions {
	private boolean canBypassTeleportBlock;

	public boolean canBypassTeleportBlock() {
		return canBypassTeleportBlock;
	}

	public void setCanBypassTeleportBlock(final boolean canBypassTeleportBlock) {
		this.canBypassTeleportBlock = canBypassTeleportBlock;
	}
}