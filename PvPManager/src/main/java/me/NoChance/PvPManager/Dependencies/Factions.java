package me.NoChance.PvPManager.Dependencies;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.MPlayer;
import me.NoChance.PvPManager.PvPlugin;
import org.bukkit.entity.Player;


public class Factions implements PvPlugin {

	@Override
	public final boolean canAttack(final Player attacker, final Player defender) {
		final MPlayer fAttacker = MPlayer.get(attacker);
		final MPlayer fDefender = MPlayer.get(defender);
		return !fAttacker.getRelationTo(fDefender).isAtLeast(Rel.TRUCE);
	}

}
