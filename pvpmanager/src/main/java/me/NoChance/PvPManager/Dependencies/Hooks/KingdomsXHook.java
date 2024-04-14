package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.entity.Player;
import org.kingdoms.constants.player.KingdomPlayer;
import org.kingdoms.main.Kingdoms;

import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.ForceToggleDependency;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Player.ProtectionResult;
import me.chancesd.sdutils.utils.Log;

public class KingdomsXHook extends BaseDependency implements ForceToggleDependency {

	public KingdomsXHook(final Hook hook) {
		super(hook);
		Log.debug(Kingdoms.get().toString());
	}

	@Override
	public boolean shouldDisable(final Player player) {
		final KingdomPlayer kingdomPlayer = KingdomPlayer.getKingdomPlayer(player);
		return kingdomPlayer.isInvading(); // hopefully this also returns true if they're being invaded
	}

	@Override
	public boolean shouldDisable(final Player attacker, final Player defender, final ProtectionResult reason) {
		return shouldDisable(attacker) && shouldDisable(defender);
	}

}
