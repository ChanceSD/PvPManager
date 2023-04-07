package me.NoChance.PvPManager.Dependencies;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import me.chancesd.sdutils.utils.Log;
import me.NoChance.PvPManager.Dependencies.Interfaces.Dependency;

public class BaseDependency implements Dependency {

	private final JavaPlugin plugin;
	private final Hook hook;

	public BaseDependency(final Hook hook) {
		this.hook = hook;
		this.plugin = (JavaPlugin) hook.getPlugin();
		Log.infoColor(ChatColor.DARK_PURPLE + onEnableMessage());
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
