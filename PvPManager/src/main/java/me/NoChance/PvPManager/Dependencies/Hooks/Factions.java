package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.entity.Player;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.MPlayer;

import me.NoChance.PvPManager.Dependencies.PvPlugin;


public class Factions implements PvPlugin {

	private final com.massivecraft.factions.Factions factions;

	public Factions() {
		factions = com.massivecraft.factions.Factions.get();
	}

	@Override
	public final boolean canAttack(final Player attacker, final Player defender) {
		final MPlayer fAttacker = MPlayer.get(attacker);
		final MPlayer fDefender = MPlayer.get(defender);
		return !fAttacker.getRelationTo(fDefender).isAtLeast(Rel.TRUCE);
	}

	@Override
	public Object getMainClass() {
		return factions;
	}

}
