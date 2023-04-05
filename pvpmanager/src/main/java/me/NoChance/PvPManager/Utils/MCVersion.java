package me.NoChance.PvPManager.Utils;

/**
 * Utility class to get and compare MC versions
 *
 * @author ChanceSD
 */
public enum MCVersion {
	OLD, V1_8, V1_9, V1_10, V1_11, V1_11_2, V1_12, V1_13, V1_13_1, V1_14, V1_15, V1_16;

	public static MCVersion getMCVersion(final String version) {
		if (CombatUtils.isVersionAtLeast(version, "1.16"))
			return MCVersion.V1_16;
		else if (CombatUtils.isVersionAtLeast(version, "1.15"))
			return MCVersion.V1_15;
		else if (CombatUtils.isVersionAtLeast(version, "1.14"))
			return MCVersion.V1_14;
		else if (CombatUtils.isVersionAtLeast(version, "1.13.1"))
			return MCVersion.V1_13_1;
		else if (CombatUtils.isVersionAtLeast(version, "1.13"))
			return MCVersion.V1_13;
		else if (CombatUtils.isVersionAtLeast(version, "1.12"))
			return MCVersion.V1_12;
		else if (CombatUtils.isVersionAtLeast(version, "1.11.2"))
			return MCVersion.V1_11_2;
		else if (CombatUtils.isVersionAtLeast(version, "1.11"))
			return MCVersion.V1_11;
		else if (CombatUtils.isVersionAtLeast(version, "1.10"))
			return MCVersion.V1_10;
		else if (CombatUtils.isVersionAtLeast(version, "1.9"))
			return MCVersion.V1_9;
		else if (CombatUtils.isVersionAtLeast(version, "1.8"))
			return MCVersion.V1_8;
		return MCVersion.OLD;
	}
}
