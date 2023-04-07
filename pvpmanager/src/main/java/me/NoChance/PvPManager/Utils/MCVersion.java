package me.NoChance.PvPManager.Utils;

import me.NoChance.PvPManager.Settings.Settings;

/**
 * Utility class to get and compare MC versions
 *
 * @author ChanceSD
 */
public enum MCVersion {
	OLD, V1_8, V1_9, V1_10, V1_11, V1_11_2, V1_12, V1_13, V1_13_1, V1_14, V1_15, V1_16, V1_16_5, V1_17, V1_18, V1_19;

	public static MCVersion getMCVersion(final String version) {
		if (CombatUtils.isVersionAtLeast(version, "1.19"))
			return MCVersion.V1_19;
		else if (CombatUtils.isVersionAtLeast(version, "1.18"))
			return MCVersion.V1_18;
		else if (CombatUtils.isVersionAtLeast(version, "1.17"))
			return MCVersion.V1_17;
		else if (CombatUtils.isVersionAtLeast(version, "1.16.5"))
			return MCVersion.V1_16_5;
		else if (CombatUtils.isVersionAtLeast(version, "1.16"))
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

	public static final boolean isAtLeast(final MCVersion version) {
		return Settings.getMinecraftVersion().ordinal() >= version.ordinal();
	}

	public static final boolean isLessThan(final MCVersion version) {
		return Settings.getMinecraftVersion().ordinal() < version.ordinal();
	}

}
