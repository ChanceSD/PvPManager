package me.NoChance.PvPManager.Dependencies;

import me.NoChance.PvPManager.PvPlugin;

import org.bukkit.entity.Player;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;

public class FactionsUUID implements PvPlugin {

	@Override
	public final boolean canAttack(final Player attacker, final Player defender) {
		final FPlayer fAttacker = FPlayers.getInstance().getByPlayer(attacker);
		final FPlayer fDefender = FPlayers.getInstance().getByPlayer(defender);
		return !fAttacker.getRelationTo(fDefender).equals(Relation.MEMBER);
	}

}
