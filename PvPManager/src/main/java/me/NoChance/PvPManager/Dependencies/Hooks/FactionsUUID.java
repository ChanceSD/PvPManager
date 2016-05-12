package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.P;
import com.massivecraft.factions.struct.Relation;

import me.NoChance.PvPManager.Dependencies.PvPlugin;

public class FactionsUUID implements PvPlugin {

	private final P factions;

	public FactionsUUID() {
		factions = P.p;
	}

	@Override
	public final boolean canAttack(final Player attacker, final Player defender) {
		final FPlayer fAttacker = FPlayers.getInstance().getByPlayer(attacker);
		final FPlayer fDefender = FPlayers.getInstance().getByPlayer(defender);
		return !fAttacker.getRelationTo(fDefender).isAtLeast(Relation.TRUCE);
	}

	@Override
	public JavaPlugin getMainClass() {
		return factions;
	}

}
