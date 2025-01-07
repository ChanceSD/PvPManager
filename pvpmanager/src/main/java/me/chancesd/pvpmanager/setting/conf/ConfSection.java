package me.chancesd.pvpmanager.setting.conf;

public class ConfSection {

	public static final String YAML_SEPARATOR = ".";
	public static final String ENABLED = "Enabled";
	public static final String GENERAL = "General Settings";

	public static final String COMBAT_TAG = "Combat Tag";
	public static final String DISPLAY = COMBAT_TAG + YAML_SEPARATOR + "Display";
	public static final String NAMETAG = DISPLAY + YAML_SEPARATOR + "Nametags";
	public static final String ACTION_BAR = DISPLAY + YAML_SEPARATOR + "Action Bar";
	public static final String BOSS_BAR = DISPLAY + YAML_SEPARATOR + "Boss Bar";
	public static final String ACTIONS_BLOCKED = COMBAT_TAG + YAML_SEPARATOR + "Actions Blocked";

	public static final String COMBAT_LOG = "Combat Log Punishments";

	public static final String PVP_TOGGLE = "PvP Toggle";

	public static final String BORDER_HOPPING = "Anti Border Hopping";

	public static final String NEWBIE_PROTECTION = "Newbie Protection";

	public static final String ITEM_COOLDOWNS = "Item Cooldowns";

	public static final String PLAYER_KILLS = "Player Kills";
	public static final String ANTI_KILL_ABUSE = PLAYER_KILLS + YAML_SEPARATOR + "Anti Kill Abuse";

	public static final String OTHER_SETTINGS = "Other Settings";

	public static final String DISABLE_ON_HIT = "Disable On Hit";

	public static final String PLUGIN_HOOKS = "Plugin Hooks";

	private ConfSection() {
	}

}
