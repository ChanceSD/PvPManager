package me.chancesd.pvpmanager.integration.hook;

import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.setting.Settings;
import me.chancesd.sdutils.utils.Log;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

public class GriefPreventionHook extends BaseDependency {

	public GriefPreventionHook(final Hook hook) {
		super(hook);
		final GriefPrevention gp = (GriefPrevention) getPlugin();
		if (gp.config_pvp_combatTimeoutSeconds > 0 || gp.config_pvp_punishLogout) {
			Log.warning("GriefPrevention has some combat features without showing any feedback messages. "
			        + "Make sure to set PunishLogout to false and set CombatTimeoutSeconds to 0 seconds in GP config. "
			        + "Conflicts with those features often get wrongly blamed on PvPManager due to the lack of GP feedback messages.");
			if (Settings.isKillOnLogout() && gp.config_pvp_punishLogout) {
				Log.severe("You have kill on logout enabled in PvPManager, so we automatically disabled the same feature on GriefPrevention. "
				        + "Disable this setting in GP or PvPManager to remove this error, this is necessary as it could cause issues.");
				gp.config_pvp_punishLogout = false;
			}
		}
	}

	@Override
	public String onEnableMessage() {
		return "GriefPrevention Found! Checking for conflicting settings";
	}

}
