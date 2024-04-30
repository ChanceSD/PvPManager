package me.chancesd.pvpmanager.integration.hook;

import org.bukkit.entity.Player;

import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.DisguiseDependency;
import me.libraryaddict.disguise.DisguiseAPI;

public class LibsDisguisesHook extends BaseDependency implements DisguiseDependency {

	public LibsDisguisesHook(final Hook hook) {
		super(hook);
	}

	@Override
	public boolean isDisguised(final Player player) {
		return DisguiseAPI.isDisguised(player);
	}

	@Override
	public void unDisguise(final Player player) {
		DisguiseAPI.undisguiseToAll(player);
	}

	@Override
	public String onEnableMessage() {
		return "LibsDisguises Found! Using it to disable disguises";
	}

}
