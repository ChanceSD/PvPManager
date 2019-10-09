package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.entity.Player;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.zcore.persist.MemoryFPlayer;
import com.massivecraft.factions.zcore.persist.MemoryFPlayers;

import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.PvPDependency;

public class SavageFactionsHook extends BaseDependency implements PvPDependency {

	private final MemoryFPlayers fPlayers;

	public SavageFactionsHook(final Hook hook) {
		super(hook);
		fPlayers = (MemoryFPlayers) FPlayers.getInstance();
	}

	@Override
	public final boolean canAttack(final Player attacker, final Player defender) {
		final MemoryFPlayer fAttacker = (MemoryFPlayer) fPlayers.getByPlayer(attacker);
		final MemoryFPlayer fDefender = (MemoryFPlayer) fPlayers.getByPlayer(defender);
		return fAttacker.getRelationTo(fDefender).isAtMost(Relation.NEUTRAL);
	}

	@Override
	public String getName() {
		return "SavageFactions";
	}

}
