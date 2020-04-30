package me.NoChance.PvPManager.Dependencies.Hooks;

import org.bukkit.entity.Player;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.commandbook.GodComponent;

import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.GodDependency;
import me.NoChance.PvPManager.Dependencies.Hook;

public class CommandBookHook extends BaseDependency implements GodDependency {

	private final GodComponent gc;

	public CommandBookHook(final Hook hook) {
		super(hook);
		this.gc = (GodComponent) ((CommandBook) hook.getPlugin()).getComponentManager().getComponent("god");
	}

	@Override
	public boolean hasGodMode(final Player player) {
		return gc.hasGodMode(player);
	}

	@Override
	public void removeGodMode(final Player player) {
		gc.disableGodMode(player);
	}

}
