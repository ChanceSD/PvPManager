package me.NoChance.PvPManager.Dependencies;

import org.bukkit.entity.Player;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.struct.Relation;

import me.NoChance.PvPManager.PvPlugin;

public class FactionsUUID implements PvPlugin {

	private final Factions factions;

	public FactionsUUID() {
		factions = Factions.get();
	}

	@Override
	public final boolean canAttack(final Player attacker, final Player defender) {
		final FPlayer fAttacker = FPlayers.getInstance().getByPlayer(attacker);
		final FPlayer fDefender = FPlayers.getInstance().getByPlayer(defender);
		return !fAttacker.getRelationTo(fDefender).isAtLeast(Relation.TRUCE);
	}

	@Override
	public Object getMainClass() {
		return factions;
	}

}
