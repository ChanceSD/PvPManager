package me.chancesd.pvpmanager.manager;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.WorldGuardDependency;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.UntagReason;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Conf.DropMode;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.utils.CombatUtils;

public class DeathHandler {

	private final PlayerManager playerManager;
	private final WorldGuardDependency wg;

	public DeathHandler(final PlayerManager playerManager) {
		this.playerManager = playerManager;
		this.wg = (WorldGuardDependency) playerManager.getPlugin().getDependencyManager().getDependency(Hook.WORLDGUARD);
	}

	public void processDeath(final PlayerDeathEvent event) {
		final Player player = event.getEntity();
		final CombatPlayer combatPlayer = playerManager.get(player);
		final Player killer = player.getKiller();

		if (combatPlayer.isInCombat()) {
			if (killer != null && !killer.equals(player)) {
				final CombatPlayer pKiller = playerManager.get(killer);
				handlePvPDeath(player, combatPlayer, killer, pKiller, event);
			}

			final Set<CombatPlayer> enemies = combatPlayer.getEnemies();
			if (Conf.UNTAG_ON_KILL.asBool()) {
				enemies.forEach(enemy -> enemy.removeEnemy(combatPlayer));
			}
			combatPlayer.untag(UntagReason.DEATH);
		} else if (combatPlayer.wasLastDeathPvP()) {
			combatPlayer.setLastDeathWasPvP(false);
		}

		// Let's process player's inventory/exp according to config file
		if (combatPlayer.hasPvPLogged()) {
			handleCombatLogDrops(event, player);
			return;
		}

		handlePlayerDrops(event, player, killer);
	}

	private void handlePvPDeath(final Player player, final CombatPlayer pvPlayer, final Player killer, final CombatPlayer pKiller,
			final PlayerDeathEvent event) {
		if (Conf.KILL_ABUSE_ENABLED.asBool() && !pKiller.hasPerm(Permissions.EXEMPT_KILL_ABUSE)) {
			pKiller.addVictim(player);
		}
		if (wg == null || !wg.containsRegionsAt(killer.getLocation(), Conf.KILLS_WG_EXCLUSIONS.asSet())) {
			if (Conf.MONEY_REWARD.asDouble() > 0) {
				pKiller.giveReward(pvPlayer);
			}
			if (Conf.MONEY_PENALTY.asDouble() > 0) {
				pvPlayer.applyPenalty();
			}
			if (pKiller.canExecuteKillCommand()) {
				CombatUtils.executeCommands(Conf.COMMANDS_ON_KILL.asList(), killer, killer.getName(), player.getName());
			}
			pvPlayer.setLastDeathWasPvP(true);
			if (Conf.EXP_STEAL.asDouble() > 0) {
				final int expWon = pKiller.giveExp(pvPlayer);
				event.setDroppedExp(0);
				event.setNewExp(player.getTotalExperience() - expWon);
				pvPlayer.message(Lang.EXP_STOLEN.msg(pKiller.getName(), expWon));
			}
		}
	}

	public void handleCombatLogDrops(final PlayerDeathEvent event, final Player player) {
		if (!Conf.DROP_EXP.asBool()) {
			keepExp(event);
		}
		if (!Conf.DROP_INVENTORY.asBool() && Conf.DROP_ARMOR.asBool()) {
			CombatUtils.fakeItemStackDrop(player, player.getInventory().getArmorContents());
			player.getInventory().setArmorContents(null);
		} else if (Conf.DROP_INVENTORY.asBool() && !Conf.DROP_ARMOR.asBool()) {
			CombatUtils.fakeItemStackDrop(player, player.getInventory().getContents());
			player.getInventory().clear();
		}
		if (!Conf.DROP_INVENTORY.asBool() || !Conf.DROP_ARMOR.asBool()) {
			keepInv(event);
		}
	}

	public void handlePlayerDrops(final PlayerDeathEvent event, final Player player, final Player killer) {
		if (player.equals(killer))
			return;
		final DropMode dropMode = Conf.PLAYER_DROP_MODE.asEnum(Conf.DropMode.class);
		if (dropMode == null) {
			Conf.PLAYER_DROP_MODE.set(DropMode.ALWAYS);
			return;
		}
		switch (dropMode) {
		case DROP:
			if (killer == null) {
				keepInv(event);
				keepExp(event);
			}
			break;
		case KEEP:
			if (killer != null) {
				keepInv(event);
				keepExp(event);
			}
			break;
		case TRANSFER:
			if (killer != null) {
				final ItemStack[] drops = event.getDrops().toArray(new ItemStack[event.getDrops().size()]);
				final HashMap<Integer, ItemStack> returned = killer.getInventory().addItem(drops);
				CombatUtils.fakeItemStackDrop(player, returned.values().toArray(new ItemStack[returned.values().size()]));
				event.getDrops().clear();
			}
			break;
		case CLEAR:
			event.getDrops().clear();
			event.setDroppedExp(0);
			break;
		default:
			break;
		}
	}

	private void keepInv(final PlayerDeathEvent event) {
		event.setKeepInventory(true);
		event.getDrops().clear();
	}

	private void keepExp(final PlayerDeathEvent event) {
		event.setKeepLevel(true);
		event.setDroppedExp(0);
	}

}
