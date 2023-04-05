package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.entity.Player;

import com.gmail.nossr50.api.PartyAPI;

import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.PvPDependency;
import me.NoChance.PvPManager.Utils.Log;

public class McMMOHook extends BaseDependency implements PvPDependency {

	public McMMOHook(final Hook hook) {
		super(hook);
		Log.debug(PartyAPI.getParties().toString());
	}

	@Override
	public boolean canAttack(final Player attacker, final Player defender) {
		return !PartyAPI.inSameParty(attacker, defender);
	}

}
