package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;

import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.RegionDependency;
import me.NoChance.PvPManager.Player.CancelResult;
import me.NoChance.PvPManager.Dependencies.ForceToggleDependency;

public class TownyHook extends BaseDependency implements ForceToggleDependency, RegionDependency {

	private final TownyAPI townyAPI;
	private final boolean pushbackPeaceful;
	private final BooleanDataField peaceSetting = new BooleanDataField("siegewar_peaceSetting", false);
	private static BooleanDataField hasSiege = new BooleanDataField("siegewar_hasSiege", false);

	public TownyHook(final Hook hook) {
		super(hook);
		townyAPI = TownyAPI.getInstance();
		pushbackPeaceful = getConfigBoolean("Pushback on Peaceful", false);
	}

	@Override
	public boolean shouldDisable(final Player player) {
		final Resident resident = townyAPI.getResident(player);
		if (resident == null || !resident.hasTown()) {
			return false;
		}
		final Town town = resident.getTownOrNull();
		return town.hasActiveWar() || hasSiege(town);
	}

	@Override
	public boolean shouldDisable(final Player attacker, final Player defender, final CancelResult reason) {
		return shouldDisable(attacker) && shouldDisable(defender);
	}

	@Override
	public boolean canAttackAt(final Player player, final Location location) {
		if (!pushbackPeaceful)
			return townyAPI.isPVP(location);

		final Town town = townyAPI.getTown(location);
		return town == null || !isNeutral(town);
	}

	private boolean isNeutral(final Town town) {
		if (town.hasMeta(peaceSetting.getKey()))
			return MetaDataUtil.getBoolean(town, peaceSetting);
		return town.isNeutral();
	}

	public boolean hasSiege(final Town town) {
		if (town.hasMeta(hasSiege.getKey())) {
			return MetaDataUtil.getBoolean(town, hasSiege);
		}
		return false;
	}

}
