package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;

import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Player.CancelResult;
import me.NoChance.PvPManager.Dependencies.ForceToggleDependency;

public class TownyHook extends BaseDependency implements ForceToggleDependency {

	private final TownyAPI townyAPI;

	public TownyHook(final Hook hook) {
		super(hook);
		townyAPI = TownyAPI.getInstance();
	}

	@Override
	public boolean shouldDisable(final Player player) {
		final Resident resident = townyAPI.getResident(player);
		return resident != null && resident.hasTown() && resident.getTownOrNull().hasActiveWar();
	}

	@Override
	public boolean shouldDisable(final Player attacker, final Player defender, final CancelResult reason) {
		return shouldDisable(attacker) && shouldDisable(defender);
	}

}
