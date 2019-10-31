package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.zcore.persist.MemoryFPlayer;
import com.massivecraft.factions.zcore.persist.MemoryFPlayers;

import me.NoChance.PvPManager.Dependencies.PvPlugin;

public class SavageFactions implements PvPlugin {

	private final com.massivecraft.factions.SavageFactions factions;
	private final MemoryFPlayers fPlayers;

	public SavageFactions() {
		factions = com.massivecraft.factions.SavageFactions.plugin;
		fPlayers = (MemoryFPlayers) FPlayers.getInstance();
	}

	@Override
	public final boolean canAttack(final Player attacker, final Player defender) {
		final MemoryFPlayer fAttacker = (MemoryFPlayer) fPlayers.getByPlayer(attacker);
		final MemoryFPlayer fDefender = (MemoryFPlayer) fPlayers.getByPlayer(defender);
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
