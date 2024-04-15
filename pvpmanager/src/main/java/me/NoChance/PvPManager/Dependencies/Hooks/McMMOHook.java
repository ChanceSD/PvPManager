package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.entity.Player;

import com.gmail.nossr50.api.PartyAPI;

import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.DependencyException;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.PvPDependency;

public class McMMOHook extends BaseDependency implements PvPDependency {

	public McMMOHook(final Hook hook) {
		super(hook);
		if (!PartyAPI.isPartySystemEnabled())
			throw new DependencyException("McMMO party system is disabled, hook disabled", hook);
	}

	@Override
	public boolean canAttack(final Player attacker, final Player defender) {
		return !PartyAPI.inSameParty(attacker, defender);
	}

}
