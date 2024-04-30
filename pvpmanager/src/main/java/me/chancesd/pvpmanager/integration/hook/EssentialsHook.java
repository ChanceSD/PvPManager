package me.chancesd.pvpmanager.integration.hook;

import org.bukkit.entity.Player;

import com.earth2me.essentials.Essentials;

import me.chancesd.pvpmanager.integration.AFKDependency;
import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.GodDependency;

public class EssentialsHook extends BaseDependency implements AFKDependency, GodDependency {

	private final Essentials ess;

	public EssentialsHook(final Hook hook) {
		super(hook);
		this.ess = (Essentials) hook.getPlugin();
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
