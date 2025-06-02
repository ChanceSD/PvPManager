package me.NoChance.PvPManager.Dependencies.Hooks;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
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
	private final StringDataField siegeAttackerUUID = new StringDataField("siegewar_attackerUUID", "");
	private final BooleanDataField hasSiege = new BooleanDataField("siegewar_hasSiege", false);

	public TownyHook(final Hook hook) {
		super(hook);
		townyAPI = TownyAPI.getInstance();
		pushbackPeaceful = getConfigBoolean("Pushback on Peaceful", false);
	}

	@Override
	public boolean shouldDisable(final Player player) {
		final Resident resident = townyAPI.getResident(player);
		if (resident == null) {
			return false;
		}
		final Town town = resident.getTownOrNull();
		return town != null && town.hasActiveWar();
	}

	@Override
	public boolean shouldDisable(final Player attacker, final Player defender, final CancelResult reason) {
		return shouldDisable(attacker) && shouldDisable(defender) || isAtWarWithNation(attacker, defender);
	}

	@Override
	public boolean canAttackAt(final Player player, final Location location) {
		if (!pushbackPeaceful)
			return townyAPI.isPVP(location);

		final Town town = townyAPI.getTown(location);
		return town == null || !isNeutral(town);
	}

	private boolean isAtWarWithNation(final Player attacker, final Player defender) {
		final Resident attackerResident = townyAPI.getResident(attacker);
		final Resident defenderResident = townyAPI.getResident(defender);
		if (attackerResident == null || defenderResident == null)
			return false;

		final Town attackerTown = attackerResident.getTownOrNull();
		final Town defenderTown = defenderResident.getTownOrNull();

		if (attackerTown == null || defenderTown == null)
			return false;

		if (hasSiege(attackerTown)) {
			return isFromAttackingNation(attackerTown, defenderResident);
		}

		if (hasSiege(defenderTown)) {
			return isFromAttackingNation(defenderTown, attackerResident);
		}
		return false;
	}

	private boolean isFromAttackingNation(final Town siegedTown, final Resident residentToCheck) {
		final Nation nationToCheck = residentToCheck.getNationOrNull();
		if (nationToCheck == null) {
			return false;
		}
		final String attackerUUIDString = getAttackerUUID(siegedTown);
		if (attackerUUIDString == null || attackerUUIDString.isEmpty()) {
			return false;
		}

		final UUID attackerUUID = UUID.fromString(attackerUUIDString);
		final Nation attackingNation = townyAPI.getNation(attackerUUID);
		return attackingNation != null && attackingNation.equals(nationToCheck);
	}

	private boolean isNeutral(final Town town) {
		if (town.hasMeta(peaceSetting.getKey()))
			return MetaDataUtil.getBoolean(town, peaceSetting);
		return town.isNeutral();
	}

	private boolean hasSiege(final Town town) {
		if (town.hasMeta(hasSiege.getKey())) {
			return MetaDataUtil.getBoolean(town, hasSiege);
		}
		return false;
	}

	private String getAttackerUUID(final Town town) {
		if (town.hasMeta(siegeAttackerUUID.getKey()))
			return MetaDataUtil.getString(town, siegeAttackerUUID);
		return null;
	}

}
