package me.chancesd.pvpmanager.player.world;

import me.chancesd.pvpmanager.storage.fields.WorldDataFields;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatWorld {

	public enum WorldOptionState {
		ON, OFF, NONE;

		public static WorldOptionState fromBoolean(final boolean state) {
			return state ? ON : OFF;
		}

		public static WorldOptionState getOpposite(WorldOptionState state) {
			if (state == ON)
				return OFF;
			if (state == OFF)
				return ON;
			else
				return NONE;
		}
	}
	private final UUID uuid;
	private final String name;
	private boolean pvpState;
	private WorldOptionState forcePVP;

	public CombatWorld(final UUID uuid, final String name, final boolean pvpState, final WorldOptionState forcePVP) {
		this.uuid = uuid;
		this.name = name;
		this.pvpState = pvpState;
		this.forcePVP = forcePVP;
	}

	public boolean isCombatAllowed() {
		return pvpState;
	}

	public WorldOptionState isPvPForced() {
		return forcePVP;
	}

	public void setCombatAllowed(final boolean state){
		this.pvpState = state;
	}

	public void setForcePVP(final WorldOptionState state){
		this.forcePVP = state;
	}

	public String getName() {
		return name;
	}

	public UUID getUUID() {
		return uuid;
	}

	public final Map<String, Object> getWorldData() {
		final Map<String, Object> userData = new HashMap<>();
		userData.put(WorldDataFields.UUID, getUUID().toString());
		userData.put(WorldDataFields.NAME, getName());
		userData.put(WorldDataFields.PVP, isCombatAllowed());
		userData.put(WorldDataFields.FORCEDPVP, isPvPForced().name());

		return userData;
	}
}
