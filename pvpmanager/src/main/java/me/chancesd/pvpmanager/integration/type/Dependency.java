package me.chancesd.pvpmanager.integration.type;

import org.bukkit.plugin.java.JavaPlugin;

import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.setting.Conf;

public interface Dependency {

	public String getName();

	public Hook getHook();

	public JavaPlugin getPlugin();

	default String onEnableMessage() {
		return getName() + " Found! Hooked successfully";
	}

	default boolean getConfigBoolean(final String key, final boolean def) {
		return Conf.getPluginHooks().getBoolean(getName() + "." + key, def);
	}

}
