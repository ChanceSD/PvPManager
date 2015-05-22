package me.NoChance.PvPManager.Dependencies;

import me.NoChance.PvPManager.PvPlugin;

import org.bukkit.entity.Player;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.MPlayer;


public class Factions implements PvPlugin {

	public final boolean canAttack(final Player attacker, final Player defender) {
		final MPlayer fAttacker = MPlayer.get(attacker);
		final MPlayer fDefender = MPlayer.get(defender);
		return !fAttacker.getRelationTo(fDefender).equals(Rel.MEMBER);
	}

}
