package me.chancesd.pvpmanager.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import io.papermc.paper.event.entity.EntityLungeEvent;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.sdutils.utils.TimeUtil;

public class LungeListener implements Listener {

	private final PlayerManager playerManager;

	public LungeListener(final PlayerManager playerManager) {
		this.playerManager = playerManager;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onLunge(final EntityLungeEvent event) {
		if (!(event.getEntity() instanceof final Player player))
			return;
		if (Conf.LUNGE_COOLDOWN.asInt() < 0 || CombatUtils.isWorldExcluded(player.getWorld().getName()))
			return;

		final CombatPlayer combatPlayer = playerManager.get(player);
		if (!combatPlayer.isInCombat() || !combatPlayer.hasLungeCooldown())
			return;

		event.setCancelled(true);
		combatPlayer.message(Lang.ITEM_COOLDOWN, TimeUtil.getDiffUntil(Lang.ITEM_COOLDOWN, combatPlayer.getLungeCooldownEnd()));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onLungeMonitor(final EntityLungeEvent event) {
		if (!(event.getEntity() instanceof final Player player))
			return;
		final int cooldown = Conf.LUNGE_COOLDOWN.asInt();
		if (cooldown < 0 || CombatUtils.isWorldExcluded(player.getWorld().getName()))
			return;

		final CombatPlayer combatPlayer = playerManager.get(player);
		if (combatPlayer.isInCombat())
			combatPlayer.setLungeCooldown(cooldown);
	}
}

