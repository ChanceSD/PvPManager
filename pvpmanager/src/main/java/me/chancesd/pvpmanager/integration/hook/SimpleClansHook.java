package me.chancesd.pvpmanager.integration.hook;

import org.bukkit.entity.Player;

import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.DependencyException;
import me.chancesd.pvpmanager.integration.ForceToggleDependency;
import me.chancesd.pvpmanager.integration.GroupDependency;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.player.ProtectionType;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;

@SuppressWarnings("null") // getClan can't be null because clan player isn't null
public class SimpleClansHook extends BaseDependency implements GroupDependency, ForceToggleDependency {

	private final ClanManager clanManager;

	public SimpleClansHook(final Hook hook) {
		super(hook);
		clanManager = ((SimpleClans) getPlugin()).getClanManager();
		if (clanManager == null)
			throw new DependencyException("Failed to enable SimpleClans support, try updating SimpleClans or check for errors in SimpleClans startup", hook);
	}

	@Override
	public final boolean canAttack(final Player attacker, final Player defender) {
		final ClanPlayer cAttacker = clanManager.getClanPlayer(attacker);
		return cAttacker == null || !cAttacker.getClan().isMember(defender) && !cAttacker.isAlly(defender);
	}

	@Override
	public boolean shouldDisable(final Player player) {
		final ClanPlayer cPlayer = clanManager.getClanPlayer(player);
		return cPlayer != null && !cPlayer.getClan().getWarringClans().isEmpty();
	}

	@Override
	public boolean shouldDisable(final Player attacker, final Player defender, final ProtectionType reason) {
		final ClanPlayer cAttacker = clanManager.getClanPlayer(attacker);
		final ClanPlayer cDefender = clanManager.getClanPlayer(defender);
		return cAttacker != null && cDefender != null && cAttacker.getClan().isWarring(cDefender.getClan());
	}

}
