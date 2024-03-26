package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.entity.Player;

import com.earth2me.essentials.Essentials;

import me.NoChance.PvPManager.Dependencies.AFKDependency;
import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.Interfaces.GodDependency;

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
