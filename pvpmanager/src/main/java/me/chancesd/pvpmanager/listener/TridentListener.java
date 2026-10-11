package me.chancesd.pvpmanager.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRiptideEvent;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.ItemCooldown;
import me.chancesd.pvpmanager.setting.ItemKey;
import me.chancesd.pvpmanager.utils.CombatUtils;

public class TridentListener implements Listener {

	private static final ItemKey TRIDENT_KEY = ItemKey.wildcard(Material.TRIDENT);
	private final PlayerManager playerManager;

	public TridentListener(final PlayerManager playerManager) {
		this.playerManager = playerManager;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerRiptideMonitor(final PlayerRiptideEvent event) {
		final Player player = event.getPlayer();
		if (CombatUtils.isWorldExcluded(player.getWorld().getName()))
			return;

		final CombatPlayer combatPlayer = playerManager.get(player);
		final ItemCooldown cooldown = Conf.getItemCooldown(TRIDENT_KEY);
		if (cooldown != null) {
			combatPlayer.setItemCooldown(TRIDENT_KEY, cooldown);
		}

		if (Conf.TRIDENT_RENEW_TAG.asBool() && combatPlayer.isInCombat()) {
			playerManager.getPlugin().getEntityListener().refreshTag(combatPlayer);
		}
	}

}
