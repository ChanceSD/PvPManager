package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.entity.Player;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.perms.Relation;

import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.PvPDependency;

public class FactionsUUIDHook extends BaseDependency implements PvPDependency {

	public FactionsUUIDHook(final Hook hook) {
		super(hook);
	}

	@Override
	public final boolean canAttack(final Player attacker, final Player defender) {
		final FPlayer fAttacker = FPlayers.getInstance().getByPlayer(attacker);
		final FPlayer fDefender = FPlayers.getInstance().getByPlayer(defender);
		return fAttacker.getRelationTo(fDefender).isAtMost(Relation.NEUTRAL);
	}

	@Override
	public String getName() {
		return "FactionsUUID";
	}

}
