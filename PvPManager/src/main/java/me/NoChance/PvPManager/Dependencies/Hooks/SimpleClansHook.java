package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.entity.Player;

import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.PvPDependency;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;

public class SimpleClansHook extends BaseDependency implements PvPDependency {

	private final ClanManager clanManager;

	public SimpleClansHook(final Hook hook) {
		super(hook);
		clanManager = ((SimpleClans) getPlugin()).getClanManager();
	}

	@Override
	public final boolean canAttack(final Player attacker, final Player defender) {
		final ClanPlayer cAttacker = clanManager.getClanPlayer(attacker);
		return cAttacker == null || !cAttacker.getClan().isMember(defender) && !cAttacker.isAlly(defender);
	}

}
