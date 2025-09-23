package me.chancesd.pvpmanager.integration.hook;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.DependencyException;
import me.chancesd.pvpmanager.integration.Hook;

public class PlaceHolderAPIHook extends BaseDependency {

	private final PlaceHolderAPI placeHolderAPI;

	public PlaceHolderAPIHook(final Hook hook) {
		super(hook);
		this.placeHolderAPI = new PlaceHolderAPI(PvPManager.getInstance());
		if (!placeHolderAPI.register() && !placeHolderAPI.isRegistered())
			throw new DependencyException("Error registering placeholder expansion, placeholders disabled!", hook);
	}

	public PlaceHolderAPI getPlaceHolder() {
		return placeHolderAPI;
	}

}
