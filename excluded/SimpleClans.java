package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.NoChance.PvPManager.Dependencies.PvPlugin;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;

public class SimpleClans implements PvPlugin {

	private final net.sacredlabyrinth.phaed.simpleclans.SimpleClans sc;
	private final ClanManager clanManager;

	public SimpleClans() {
		sc = net.sacredlabyrinth.phaed.simpleclans.SimpleClans.getInstance();
		clanManager = sc.getClanManager();
	}

	@Override
	public final boolean canAttack(final Player attacker, final Player defender) {
		final ClanPlayer cAttacker = clanManager.getClanPlayer(attacker);
		return cAttacker == null || !cAttacker.getClan().isMember(defender) && !cAttacker.isAlly(defender);
	}

	@Override
	public boolean canBeAttacked(final Player player, final Location l) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public JavaPlugin getMainClass() {
		return sc;
	}

}
