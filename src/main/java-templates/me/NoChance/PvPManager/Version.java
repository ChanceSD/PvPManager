package me.NoChance.PvPManager;
public final class Version {

    private static final String CONFIG_VERSION = "${config.version}";

    private Version() {
    }

    public static String getConfigVersion() {
        return CONFIG_VERSION;
    }

}
