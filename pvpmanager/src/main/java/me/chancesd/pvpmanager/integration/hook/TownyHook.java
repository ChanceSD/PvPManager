package me.chancesd.pvpmanager.integration.hook;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;

import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.ForceToggleDependency;
import me.chancesd.pvpmanager.integration.type.RegionDependency;
import me.chancesd.pvpmanager.player.ProtectionType;

public class TownyHook extends BaseDependency implements ForceToggleDependency, RegionDependency {

	private final TownyAPI townyAPI;
	private final boolean pushbackPeaceful;
	private final BooleanDataField bdf = new BooleanDataField("siegewar_peaceSetting", false);

	public TownyHook(final Hook hook) {
		super(hook);
		townyAPI = TownyAPI.getInstance();
		pushbackPeaceful = getConfigBoolean("Pushback on Peaceful", false);
	}

	@SuppressWarnings("null") // already check if resident has town
	@Override
	public boolean shouldDisable(final Player player) {
		final Resident resident = townyAPI.getResident(player);
		return resident != null && resident.hasTown() && resident.getTownOrNull().hasActiveWar();
	}

	@Override
	public boolean shouldDisable(final Player attacker, final Player defender, final ProtectionType reason) {
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
		if (town.hasMeta(bdf.getKey()))
			return MetaDataUtil.getBoolean(town, bdf);
		return town.isNeutral();
	}

}
