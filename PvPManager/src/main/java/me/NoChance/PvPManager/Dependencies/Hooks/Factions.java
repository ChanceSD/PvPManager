package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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
		final Rel rel = fAttacker.getRelationTo(fDefender);
		return !rel.isAtLeast(Rel.TRUCE) || rel == Rel.RECRUIT;
	}

	@Override
	public boolean canBeAttacked(final Player player, final Location l) {
		return false;
	}

	@Override
	public JavaPlugin getMainClass() {
		return factions;
	}

}
