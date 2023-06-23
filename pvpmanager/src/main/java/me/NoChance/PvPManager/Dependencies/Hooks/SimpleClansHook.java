package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.entity.Player;

import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.DependencyException;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.PvPDependency;
import me.NoChance.PvPManager.Player.CancelResult;
import me.NoChance.PvPManager.Dependencies.ForceToggleDependency;
import me.NoChance.PvPManager.Settings.Settings;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;

@SuppressWarnings("null")
public class SimpleClansHook extends BaseDependency implements PvPDependency, ForceToggleDependency {

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
	public boolean shouldDisable(final Player attacker, final Player defender, final CancelResult reason) {
		final ClanPlayer cAttacker = clanManager.getClanPlayer(attacker);
		final ClanPlayer cDefender = clanManager.getClanPlayer(defender);
		return cAttacker != null && cDefender != null && cAttacker.getClan().isWarring(cDefender.getClan());
	}

	@Override
	public boolean shouldDisableProtection() {
		return Settings.isSimpleClansNoPvPInWar();
	}

}
