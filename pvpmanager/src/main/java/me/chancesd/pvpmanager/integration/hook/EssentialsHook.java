package me.chancesd.pvpmanager.integration.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.earth2me.essentials.Essentials;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.AFKDependency;
import me.chancesd.pvpmanager.integration.type.GodDependency;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import net.essentialsx.api.v2.events.TeleportRequestResponseEvent;

public class EssentialsHook extends BaseDependency implements AFKDependency, GodDependency, Listener {

	private final Essentials ess;

	public EssentialsHook(final Hook hook) {
		super(hook);
		this.ess = (Essentials) hook.getPlugin();
		Bukkit.getPluginManager().registerEvents(this, PvPManager.getInstance());
	}

	@SuppressWarnings("deprecation")
	@EventHandler // For cases like Folia where teleport events aren't fired
	public void onTeleportRequestResponse(final TeleportRequestResponseEvent event) {
		if (!Conf.BLOCK_TELEPORT.asBool() || event.isDeny())
			return;

		final Player requester = event.getRequester().getBase();
		final Player requestee = event.getRequestee().getBase();

		final CombatPlayer combatRequester = CombatPlayer.get(requester);
		final CombatPlayer combatRequestee = CombatPlayer.get(requestee);

		final boolean requesterInCombat = combatRequester != null && combatRequester.isInCombat();
		final boolean requesteeInCombat = combatRequestee != null && combatRequestee.isInCombat();

		if (requesterInCombat || requesteeInCombat) {
			event.setCancelled(true);
			if (requesterInCombat) {
				combatRequester.message(Lang.TELEPORT_BLOCKED_IN_COMBAT);
			}
			if (requesteeInCombat) {
				combatRequestee.message(Lang.TELEPORT_BLOCKED_IN_COMBAT);
			}
		}
	}

	@Override
	public boolean hasGodMode(final Player player) {
		return ess.getUser(player).isGodModeEnabled();
	}

	@Override
	public void enableGodMode(final Player player) {
		ess.getUser(player).setGodModeEnabled(true);
	}

	@Override
	public void disableGodMode(final Player player) {
		ess.getUser(player).setGodModeEnabled(false);
	}

	@Override
	public boolean isAFK(final Player player) {
		return ess.getUser(player).isAfk();
	}

}
