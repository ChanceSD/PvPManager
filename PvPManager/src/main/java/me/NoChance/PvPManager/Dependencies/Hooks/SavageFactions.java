package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;

import me.NoChance.PvPManager.Dependencies.PvPlugin;

public class SavageFactions implements PvPlugin {

	private final com.massivecraft.factions.SavageFactions factions;

	public SavageFactions() {
		factions = com.massivecraft.factions.SavageFactions.plugin;
	}

	@Override
	public final boolean canAttack(final Player attacker, final Player defender) {
		final FPlayer fAttacker = FPlayers.getInstance().getByPlayer(attacker);
		final FPlayer fDefender = FPlayers.getInstance().getByPlayer(defender);
		return fAttacker.getRelationTo(fDefender).value < 2; // Less than TRUCE
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
