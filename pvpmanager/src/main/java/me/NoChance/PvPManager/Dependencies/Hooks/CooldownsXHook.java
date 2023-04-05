package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.entity.Player;

import com.github.sirblobman.cooldowns.api.ICooldownsX;
import com.github.sirblobman.cooldowns.api.configuration.ICooldownSettings;
import com.github.sirblobman.cooldowns.api.data.ICooldownData;

import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.DependencyException;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Settings.Settings;

public class CooldownsXHook extends BaseDependency {

	private final ICooldownsX cooldownx;
	private final ICooldownSettings enderpearlSettings;

	public CooldownsXHook(final Hook hook) {
		super(hook);
		this.cooldownx = (ICooldownsX) hook.getPlugin();
		enderpearlSettings = cooldownx.getCooldownManager().getCooldownSettings(Settings.getCooldownsxEnderpearlID());
		if (enderpearlSettings == null)
			throw new DependencyException("Couldn't find the cooldown ID: " + Settings.getCooldownsxEnderpearlID() + " in CooldownsX", hook);
	}

	private ICooldownData getData(final Player player) {
		return cooldownx.getCooldownManager().getData(player);
	}

	public void setEnderpearlCooldown(final Player player, final int seconds) {
		getData(player).setCooldown(enderpearlSettings, System.currentTimeMillis() + seconds * 1000);
	}

	@Override
	public String onEnableMessage() {
		return getName() + " Found! Using different enderpearl cooldowns while in combat";
	}

}
