package me.NoChance.PvPManager.Dependencies;

import org.bukkit.plugin.java.JavaPlugin;

import me.NoChance.PvPManager.Settings.Settings;

public interface Dependency {

	public String getName();

	public Hook getHook();

	public JavaPlugin getPlugin();

	default String onEnableMessage() {
		return getName() + " Found! Hooked successfully";
	}

	default boolean getConfigBoolean(final String key, final boolean def) {
		return Settings.getPLUGINHOOKS().getBoolean(getName() + "." + key, def);
	}

}
