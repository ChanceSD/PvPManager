package me.NoChance.PvPManager.Dependencies;

import org.bukkit.plugin.java.JavaPlugin;

import me.NoChance.PvPManager.Utils.Log;

public class BaseDependency implements Dependency {

	private final JavaPlugin plugin;
	private final Hook hook;

	public BaseDependency(final Hook hook) {
		this.hook = hook;
		this.plugin = (JavaPlugin) hook.getPlugin();
		Log.info(onEnableMessage());
	}

	@Override
	public String getName() {
		return getPlugin().getName();
	}

	@Override
	public Hook getHook() {
		return hook;
	}

	@Override
	public JavaPlugin getPlugin() {
		return plugin;
	}

}
