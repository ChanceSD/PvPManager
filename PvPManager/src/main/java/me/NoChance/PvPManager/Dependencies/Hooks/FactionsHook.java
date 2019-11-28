package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.entity.Player;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.MPlayer;

import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.PvPDependency;

public class FactionsHook extends BaseDependency implements PvPDependency {

	public FactionsHook(final Hook hook) {
		super(hook);
	}

	@Override
	public final boolean canAttack(final Player attacker, final Player defender) {
		final MPlayer fAttacker = MPlayer.get(attacker);
		final MPlayer fDefender = MPlayer.get(defender);
		final Rel rel = fAttacker.getRelationTo(fDefender);
		return rel.isLessThan(Rel.TRUCE) || rel == Rel.RECRUIT; // Two players without faction are RECRUIT to each other for some weird reason
	}

}
