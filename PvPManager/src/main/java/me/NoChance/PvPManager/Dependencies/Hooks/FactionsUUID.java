package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.Relation;

import me.NoChance.PvPManager.Dependencies.PvPlugin;

public class FactionsUUID implements PvPlugin {

	private final FactionsPlugin factions;

	public FactionsUUID() {
		factions = FactionsPlugin.getInstance();
	}

	@Override
	public final boolean canAttack(final Player attacker, final Player defender) {
		final FPlayer fAttacker = FPlayers.getInstance().getByPlayer(attacker);
		final FPlayer fDefender = FPlayers.getInstance().getByPlayer(defender);
		return fAttacker.getRelationTo(fDefender).isAtMost(Relation.NEUTRAL);
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
