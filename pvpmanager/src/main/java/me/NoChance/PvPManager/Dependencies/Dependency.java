package me.NoChance.PvPManager.Dependencies;

import org.bukkit.plugin.java.JavaPlugin;

public interface Dependency {

	public String getName();

	public Hook getHook();

	public JavaPlugin getPlugin();

	default String onEnableMessage() {
		return getName() + " Found! Hooked successfully";
	}

}
